/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.duracloud.client.ContentStore;
import org.duracloud.controller.UploadTask;
import org.duracloud.duradmin.domain.ContentItem;
import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daniel Bernstein
 */
public class ContentItemUploadTask implements UploadTask, Comparable, ProgressListener{
		Logger log = LoggerFactory.getLogger(ContentItemUploadTask.class);
		private ContentItem contentItem;
		private ContentStore contentStore;
		private long totalBytes = 0;
		private long bytesRead = 0;

		private UploadTask.State state = null;
		private InputStream stream = null;
		public ContentItemUploadTask(ContentItem contentItem, ContentStore contentStore, InputStream stream, String username) throws Exception{
			this.stream = stream;
			this.contentItem = contentItem;
			this.contentStore = contentStore;
			this.state = State.INITIALIZED;
			this.username = username;
			this.totalBytes = -1;
			log.info("new task created for {} by {}", contentItem, username);
		}

		public void execute() throws ContentStoreException, IOException, FileUploadException, Exception{
			try{
				log.info("executing file upload: {}" , contentItem);
				this.startDate = new Date();
				state = State.RUNNING;
				contentStore.addContent(contentItem.getSpaceId(),
	                    contentItem.getContentId(),
	                    this.stream,
	                    -1,
	                    contentItem.getContentMimetype(),
	                    null,
	                    null);
				state = State.SUCCESS;				
				log.info("file upload completed successfully: {}" , contentItem);
			}catch(Exception ex){

				log.error("failed to upload content item: {}, bytesRead={}, totalBytes={}, state={}, message: {}", 
						new Object[]{contentItem, this.bytesRead, this.totalBytes, this.state.name(), ex.getMessage()});

				if(this.state == State.CANCELLED){
					log.debug("This upload was previously cancelled - the closing of the input stream is the likely cause of the exception");
				}else{
					ex.printStackTrace();
					state = State.FAILURE;
					throw ex;
				}
			}
		}
		
		public void update(long pBytesRead, long pContentLength,
				int pItems) {
			bytesRead = pBytesRead;
			totalBytes = pContentLength;
			log.debug("updating progress: bytesRead = {}, totalBytes = {}", bytesRead, totalBytes);
		}
	
		
		public String getId() {
			return this.contentItem.getStoreId()+"/"+
					this.contentItem.getSpaceId()+"/"+
						this.contentItem.getContentId();
		}
		
		public void cancel(){
			if(state == State.RUNNING){
				state = State.CANCELLED;
				try {
					stream.close();
				} catch (IOException e) {
					log.error("failed to close item input stream of content item in upload process.", e);
				}
			}
		}
		
		public State getState(){
			return this.state;
		}
		
		public Map<String,String> getProperties(){
			Map<String,String> map = new HashMap<String,String>();
			map.put("bytesRead", String.valueOf(this.bytesRead));
			map.put("totalBytes", String.valueOf(this.totalBytes));
			map.put("state", String.valueOf(this.state.toString().toLowerCase()));
			map.put("contentId", this.contentItem.getContentId());
			map.put("spaceId", this.contentItem.getSpaceId());
			map.put("storeId", this.contentItem.getStoreId());
			return map;
		}


		private Date startDate = null;

		@Override
		public Date getStartDate() {
			return this.startDate;
		}
		


		@Override
		public int compareTo(Object o) {
			ContentItemUploadTask other = (ContentItemUploadTask)o;
			return this.getStartDate().compareTo(other.getStartDate());
		}
		
		public String toString(){
			return "{startDate: " + startDate + ", bytesRead: " + this.bytesRead + 
						", totalBytes: " + totalBytes +
								", storeId: " + contentItem.getStoreId() + 
								", spaceId: " + contentItem.getSpaceId() + 
								", contentId: " + contentItem.getContentId() + 
								
								"}";
		}

        private String username;

		@Override
        public String getUsername() {
            return this.username;
        }

}
