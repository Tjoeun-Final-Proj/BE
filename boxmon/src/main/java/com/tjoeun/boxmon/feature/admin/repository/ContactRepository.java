package com.tjoeun.boxmon.feature.admin.repository;

import com.tjoeun.boxmon.feature.admin.domain.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {

}
