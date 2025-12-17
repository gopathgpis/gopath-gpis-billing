package com.gopath.billing.gpis.repository;

import com.gopath.domain.persistent.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OrganizationRepo extends JpaRepository<Organization, String> {
}