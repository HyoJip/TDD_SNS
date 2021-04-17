package com.hoaxify.hoxaxify.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hoaxify.hoxaxify.model.FileAttachment;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

	List<FileAttachment> findByDateBeforeAndHoaxIsNull(Date date);
}
