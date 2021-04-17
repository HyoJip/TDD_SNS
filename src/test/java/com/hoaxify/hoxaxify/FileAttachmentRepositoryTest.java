package com.hoaxify.hoxaxify;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.hoaxify.hoxaxify.model.FileAttachment;
import com.hoaxify.hoxaxify.model.Hoax;
import com.hoaxify.hoxaxify.repository.FileAttachmentRepository;

@DataJpaTest
@ActiveProfiles("test")
public class FileAttachmentRepositoryTest {

	@Autowired
	TestEntityManager testEntityManager;
	
	@Autowired
	FileAttachmentRepository fileAttachmentRepository;
	
	@BeforeEach
	public void cleanup() {
		fileAttachmentRepository.deleteAll();
	}
	
	@Test @DisplayName("[첨부파일 조회] 모두 피드 없으면서 1시간 지난 피드 파일, 모두 조회")
	public void findByDateBeforeAndHoaxIsNull_whenAttachmentsDateOlderThanOneHour_returnsAll() {
		testEntityManager.persist(getOneHourOlderFileAttachment());
		testEntityManager.persist(getOneHourOlderFileAttachment());
		testEntityManager.persist(getOneHourOlderFileAttachment());
		
		Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
		List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);
		assertThat(attachments.size()).isEqualTo(3);
	}
	@Test @DisplayName("[첨부파일 조회] 피드 있고 1시간 지난 피드 파일, None 조회")
	public void findByDateBeforeAndHoaxIsNull_whenAttachmentsDateOlderThanOneHourButHaveHoax_returnsNone() {
		Hoax hoax1 = testEntityManager.persist(TestUtil.createValidHoax()); 
		Hoax hoax2 = testEntityManager.persist(TestUtil.createValidHoax()); 
		Hoax hoax3 = testEntityManager.persist(TestUtil.createValidHoax()); 
		
		testEntityManager.persist(getOneHourOlderFileAttachment(hoax1));
		testEntityManager.persist(getOneHourOlderFileAttachment(hoax2));
		testEntityManager.persist(getOneHourOlderFileAttachment(hoax3));
		
		Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
		List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);
		assertThat(attachments.size()).isEqualTo(0);
	}
	@Test @DisplayName("[첨부파일 조회] 피드 없고 1시간 이내 피드 파일, None 조회")
	public void findByDateBeforeAndHoaxIsNull_whenAttachmentsDateWithinOneHour_returnsNone() {
		testEntityManager.persist(getOneHourWithinFileAttachment());
		testEntityManager.persist(getOneHourWithinFileAttachment());
		testEntityManager.persist(getOneHourWithinFileAttachment());
		
		Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
		List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);
		assertThat(attachments.size()).isEqualTo(0);
	}
	@Test @DisplayName("[첨부파일 조회] 몇개는 피드 없고 몇개는 1시간 이내고 몇개는 1시간 넘었고, 1시간 지났고 피드 없는 첨부파일만 조회")
	public void findByDateBeforeAndHoaxIsNull_whenSomeHaveNoHoaxSomeDateOlderThanAHourSomeDateWithinAHour_returnsOnlyNoHoaxAndOlderThanAHour() {
		Hoax hoax = testEntityManager.persist(TestUtil.createValidHoax());
		testEntityManager.persist(getOneHourOlderFileAttachment());
		testEntityManager.persist(getOneHourOlderFileAttachment(hoax));
		testEntityManager.persist(getOneHourWithinFileAttachment());
		
		Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
		List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);
		assertThat(attachments.size()).isEqualTo(1);
	}

	
	
	private FileAttachment getOneHourOlderFileAttachment(Hoax hoax) {
		FileAttachment attachment = getOneHourOlderFileAttachment();
		attachment.setHoax(hoax);
		return attachment;
	}
	private FileAttachment getOneHourOlderFileAttachment() {
		Date date = new Date(System.currentTimeMillis() - (60*60*1000) - 1);
		FileAttachment attachment = new FileAttachment();
		attachment.setDate(date);
		return attachment;
	}
	private FileAttachment getOneHourWithinFileAttachment() {
		Date date = new Date(System.currentTimeMillis() - 1);
		FileAttachment attachment = new FileAttachment();
		attachment.setDate(date);
		return attachment;
	}
}
