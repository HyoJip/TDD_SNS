package com.hoaxify.hoxaxify.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.hoaxify.hoxaxify.model.Hoax;
import com.hoaxify.hoxaxify.model.User;

public interface HoaxRepository extends JpaRepository<Hoax, Long>, JpaSpecificationExecutor<Hoax>{

	Page<Hoax> findByUser(User user, Pageable pageable);

}
