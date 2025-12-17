package com.gopath.billing.gpis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gopath.domain.persistent.BillingDetail;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillingDetailRepo extends JpaRepository<BillingDetail, String> {

    Optional<List<BillingDetail>> findAllByCaseId(String caseId);

    @Modifying // Required for DML operations (UPDATE, DELETE)
    @Transactional // Ensures the update is within a transaction
    @Query(value = "UPDATE billing_detail SET billing_status = ?1, sftp_bill_status = ?2, send_kareo_date = ?3  WHERE case_id = ?4", nativeQuery = true)
    void updateBillingDetailByCaseId(String billingStatus, String sftpBillStatus, String kareoDate, String caseId);



}