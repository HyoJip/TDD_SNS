package com.hoaxify.hoxaxify.model.vm;

import com.hoaxify.hoxaxify.model.FileAttachment;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileAttachmentVM {

	private String name;
	private String fileType;
	
	public FileAttachmentVM(FileAttachment fileAttachment) {
		name = fileAttachment.getName();
		fileType = fileAttachment.getFileType();
	}
}
