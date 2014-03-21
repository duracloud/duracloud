package org.duracloud.audit;


/**
 * An intereface defining a log item.
 * 
 * @author Daniel Bernstein
 * 
 */
public interface AuditLogItem {
    /**
     * 
     * @return
     */
    public String getAccount();

    /**
     * 
     * @return
     */
    public String getStoreId();

    /**
     * 
     * @return
     */
    public String getSpaceId();

    /**
     * 
     * @return
     */
    public String getContentId();

    /**
     * 
     * @return
     */
    public String getContentMd5();

    /**
     * 
     * @return
     */
    public String getAction();

    /**
     * 
     * @return
     */
    public String getUsername();

    /**
     * 
     * @return
     */
    public long getTimestamp();
}
