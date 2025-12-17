package com.gopath.billing.gpis.repository;

import com.gopath.uti.portal.model.UtiPortalCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface UtiPortalCustomerRepo extends JpaRepository<UtiPortalCustomer, String> {


}