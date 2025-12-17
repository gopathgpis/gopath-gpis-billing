package com.gopath.billing.gpis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gopath.domain.persistent.UtiCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UtiCaseRepo extends JpaRepository<UtiCase, String> {

    @Query(value = "SELECT * FROM uti_case WHERE uti_portal_order_id = ?1", nativeQuery = true)
    List<UtiCase> findAllByUtiPortalOrderId(String utiPortalOrderId);

    @Modifying // Required for DML operations (UPDATE, DELETE)
    @Transactional // Ensures the update is within a transaction
    @Query(value = "UPDATE uti_case SET sftp_bill_status = ?1 WHERE id = ?2", nativeQuery = true)
    void updateSftpBillStatusById(String status, String id);
}