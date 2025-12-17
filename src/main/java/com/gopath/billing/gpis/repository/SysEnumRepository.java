package com.gopath.billing.gpis.repository;

import com.gopath.billing.gpis.entity.SysEnum;
import org.springframework.stereotype.Repository;



@Repository
public interface SysEnumRepository extends BaseRepository<SysEnum, String> {
    SysEnum findBySysEnumCode(String code);

}
