/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.mill.manifest.jpa;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;

import org.duracloud.common.collection.StreamingIterator;
import org.duracloud.error.NotFoundException;
import org.duracloud.mill.db.model.ManifestItem;
import org.duracloud.mill.db.repo.JpaManifestItemRepo;
import org.duracloud.mill.db.repo.MillJpaRepoConfig;
import org.duracloud.mill.db.util.JpaIteratorSource;
import org.duracloud.mill.manifest.ManifestItemWriteException;
import org.duracloud.mill.manifest.ManifestStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class JpaManifestStore implements
                             ManifestStore {
    private static Logger log = LoggerFactory.getLogger(JpaManifestStore.class);
    private JpaManifestItemRepo manifestItemRepo;

    @Autowired
    public JpaManifestStore(JpaManifestItemRepo manifestItemRepo) {
        this.manifestItemRepo = manifestItemRepo;
    }

    @Override
    @Transactional(value = MillJpaRepoConfig.TRANSACTION_MANAGER_BEAN, propagation = Propagation.REQUIRES_NEW)
    public void addUpdate(String account,
                      String storeId,
                      String spaceId,
                      String contentId,
                      String contentChecksum,
                      String contentMimetype,
                      String contentSize,
                      Date eventTimestamp) throws ManifestItemWriteException {

        if(log.isDebugEnabled()){
            log.debug("preparing to write account={}, " +
                    "storeId={}, " +
                    "spaceId={}, " +
                    "contentId={}, " +
                    "contentChecksum={}, " +
                    "contentMimetype={}, " +
                    "contentSize={}, " +
                    "eventTimestamp={}",
              account,
              storeId,
              spaceId,
              contentId,
              contentChecksum,
              contentMimetype,
              contentSize,
              eventTimestamp);
        }
        
        try {
            
            boolean save = false;
            
            ManifestItem item = this.manifestItemRepo
                    .findByAccountAndStoreIdAndSpaceIdAndContentId(account,
                                                                   storeId,
                                                                   spaceId,
                                                                   contentId);
            String action = "added";
            
            if(item != null){
                if(eventOutOfOrder(item, eventTimestamp)){
                    return;
                }

                //flip deleted flag if set to deleted
                if(item.isDeleted()){
                    item.setDeleted(false);
                    save = true;
                }
                String oldChecksum = item.getContentChecksum();
                if(!oldChecksum.equals(contentChecksum)){
                    log.info("content checksum changed from {} to {}", oldChecksum, contentChecksum);
                    save = true;
                }

                String oldMimetype = item.getContentMimetype();
                if(!oldMimetype.equals(contentMimetype)){
                    log.info("content mimetype changed from {} to {}", oldMimetype, contentMimetype);
                    save = true;
                }

                String oldSize = item.getContentSize();
                if(!oldSize.equals(contentSize)){
                    log.info("content size changed from {} to {}", oldMimetype, contentSize);
                    save = true;
                }

                action = "updated";

            }else{

                item = new ManifestItem();
                item.setAccount(account);
                item.setStoreId(storeId);
                item.setSpaceId(spaceId);
                item.setContentId(contentId);
                save = true;
            }

            if(save){
                item.setContentChecksum(contentChecksum);
                item.setContentMimetype(contentMimetype);
                item.setContentSize(contentSize);
                item.setModified(eventTimestamp);
                ManifestItem result = this.manifestItemRepo.saveAndFlush(item);
                log.info("successfully {} {} to the jpa repo.", action, result);
            }else{
                log.info("no update necessary since no changes were detected for {}", item);
            }

        } catch (Exception ex) {
            String message = "failed to write item: " + ex.getMessage();
            log.error(message);
            throw new ManifestItemWriteException(message, ex);
        }
    }
    
    /* (non-Javadoc)
     * @see org.duracloud.mill.manifest.ManifestStore#flagAsDeleted(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(value = MillJpaRepoConfig.TRANSACTION_MANAGER_BEAN, propagation = Propagation.REQUIRES_NEW)
    public void flagAsDeleted(String account,
                              String storeId,
                              String spaceId,
                              String contentId,
                              Date eventTimestamp) throws ManifestItemWriteException {
        try {
            
            ManifestItem item = this.manifestItemRepo
                    .findByAccountAndStoreIdAndSpaceIdAndContentId(account,
                                                                   storeId,
                                                                   spaceId,
                                                                   contentId);
            
            if(item != null){
                if(eventOutOfOrder(item, eventTimestamp)){
                    return;
                }
                
                if(item.isDeleted()){
                    log.warn("item {}/{}/{}/{} has already been deleted - " +
                    		"there appears to have been a duplicate event " +
                    		"or possibly a missed content add event - ignoring...", 
                             account,
                             storeId,
                             spaceId,
                             contentId);
                    
                }
            }else{
                log.warn("no manifest item {}/{}/{}/{} : nothing to delete - adding deleted manifest entry...", 
                          account,
                          storeId,
                          spaceId,
                          contentId);
                
                item = new ManifestItem();
                item.setAccount(account);
                item.setStoreId(storeId);
                item.setSpaceId(spaceId);
                item.setContentId(contentId);
                item.setContentMimetype("unknown");
                item.setContentSize("0");
                item.setContentChecksum("unknown");
            }

            item.setDeleted(true);
            item.setModified(eventTimestamp);
            ManifestItem result = this.manifestItemRepo.saveAndFlush(item);
            log.info("successfully processed flag as deleted: {}", result);

        } catch (Exception ex) {
            String message = "failed to flag item as deleted item: " + ex.getMessage();
            log.error(message);
            throw new ManifestItemWriteException(message, ex);
        }        
    }

    /**
     * @param item
     * @param eventTimestamp
     * @return
     */
    private boolean eventOutOfOrder(ManifestItem item, Date eventTimestamp) {
        Date itemTimestamp = item.getModified();
        if(eventTimestamp.before(itemTimestamp)){
            log.warn("The current database item is more " +
                     "current that the event: item last modified: " +
                     "{}, event timestamp: {}. Likely cause: events " +
                     "were delivered out of order. Ignoring...", 
                     itemTimestamp, 
                     eventTimestamp);
            return true;
        }else{
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true, value=MillJpaRepoConfig.TRANSACTION_MANAGER_BEAN)
    public Iterator<ManifestItem> getItems(final String account,
                                           final String storeId,
                                           final String spaceId) {
        JpaIteratorSource<JpaManifestItemRepo, ManifestItem> source = 
            new JpaIteratorSource<JpaManifestItemRepo, ManifestItem>(this.manifestItemRepo) {
                @Override
                protected Page<ManifestItem> getNextPage(Pageable pageable,
                                                         JpaManifestItemRepo repo) {
                    return manifestItemRepo
                            .findByAccountAndStoreIdAndSpaceIdAndDeletedFalseOrderByContentIdAsc(account,
                                                                                       storeId,
                                                                                       spaceId,
                                                                                       pageable);
                }
            };
        
        return (Iterator) new StreamingIterator<ManifestItem>(source);
    }

    @Override
    public ManifestItem
            getItem(final String account,
                    final String storeId,
                    final String spaceId,
                    final String contentId) throws NotFoundException {
        ManifestItem item = this.manifestItemRepo
                .findByAccountAndStoreIdAndSpaceIdAndContentId(account,
                                                               storeId,
                                                               spaceId,
                                                               contentId);
        if (item == null) {
            throw new NotFoundException(MessageFormat.format("No ManifestItem could be found matching the specified params: " +
            		                                     "account={0}, storeId={1}, spaceId={2}, contentId={3}",
                                                             account,
                                                             storeId,
                                                             spaceId,
                                                             contentId));
        }

        return item;
    }

    
    /* (non-Javadoc)
     * @see org.duracloud.mill.manifest.ManifestStore#purgeDeletedItemsBefore(java.util.Date)
     */
    @Override
    @Transactional(value = MillJpaRepoConfig.TRANSACTION_MANAGER_BEAN, propagation = Propagation.REQUIRES_NEW)
    public Long purgeDeletedItemsBefore(Date expiration) {
       return this.manifestItemRepo.deleteByDeletedTrueAndModifiedBefore(expiration);
    }

    @Override
    @Transactional(value = MillJpaRepoConfig.TRANSACTION_MANAGER_BEAN, propagation = Propagation.REQUIRES_NEW)
    public void updateMissingFromStorageProviderFlag(String account,
                                                     String storeId,
                                                     String spaceId,
                                                     String contentId,
                                                     boolean flag) throws ManifestItemWriteException {
        
        try {
            
            ManifestItem item = this.manifestItemRepo
                    .findByAccountAndStoreIdAndSpaceIdAndContentId(account,
                                                                   storeId,
                                                                   spaceId,
                                                                   contentId);
            
            if(item != null){
                
                item.setMissingFromStorageProvider(flag);
                ManifestItem result = this.manifestItemRepo.saveAndFlush(item);
                log.info("successfully processed update MissingFromStorageProvider flag: {}", result);

            }else{
                String message = MessageFormat.format("no manifest item found:  {0}/{1}/{2}/{3}: something's amiss.", 
                                                           account,
                                                           storeId,
                                                           spaceId,
                                                           contentId);
                throw new NotFoundException(message);
            }

        } catch (Exception ex) {
            String message = "failed to update manifest item with : " + ex.getMessage();
            log.error(message);
            throw new ManifestItemWriteException(message, ex);
        }        
    }

    @Override
    @Transactional(value = MillJpaRepoConfig.TRANSACTION_MANAGER_BEAN, propagation = Propagation.REQUIRES_NEW)
    public void delete(String account, String storeId, String spaceId)
        throws ManifestItemWriteException {
        this.manifestItemRepo.deleteByAccountAndStoreIdAndSpaceId(account,storeId, spaceId);
    }

}
