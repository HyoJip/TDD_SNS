package com.hoaxify.hoxaxify.model.vm;

import java.util.Date;

import com.hoaxify.hoxaxify.model.FileAttachment;
import com.hoaxify.hoxaxify.model.Hoax;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HoaxVM {
	
	private long id;
	private String content;
	private Date timestamp;
	private UserVM user;
	private FileAttachmentVM attachment;
	
	public HoaxVM(Hoax hoax) {
		id = hoax.getId();
		content = hoax.getContent();
		timestamp = hoax.getTimestamp();
		user = new UserVM(hoax.getUser());
		
		FileAttachment fileAttachment = hoax.getAttachment();
		if (fileAttachment != null) {
			attachment = new FileAttachmentVM(fileAttachment);			
		}
	}
}
