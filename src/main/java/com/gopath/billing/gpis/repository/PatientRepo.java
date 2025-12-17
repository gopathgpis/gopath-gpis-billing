package com.gopath.billing.gpis.repository;

import com.gopath.domain.persistent.Patient;
import com.gopath.domain.persistent.UtiCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface PatientRepo extends JpaRepository<Patient, String> {

}