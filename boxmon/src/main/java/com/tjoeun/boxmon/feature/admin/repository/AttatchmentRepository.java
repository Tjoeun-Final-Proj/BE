package com.tjoeun.boxmon.feature.admin.repository;

import com.tjoeun.boxmon.feature.admin.domain.Contact;
import com.tjoeun.boxmon.feature.admin.domain.ContactAttatchment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttatchmentRepository extends JpaRepository<ContactAttatchment, Long> {
    List<ContactAttatchment> findByContactId(Contact contactId);
    List<ContactAttatchment> findByContactIdIn(List<Contact> contacts);
}