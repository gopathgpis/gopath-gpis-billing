package com.gopath.billing.gpis.repository;

import com.gopath.domain.persistent.Physician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhysicianRepo extends JpaRepository<Physician, String> {
}