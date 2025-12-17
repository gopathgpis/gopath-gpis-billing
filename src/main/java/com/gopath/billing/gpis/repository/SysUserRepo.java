package com.gopath.billing.gpis.repository;

import com.gopath.domain.persistent.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SysUserRepo extends JpaRepository<SysUser, String> {

}