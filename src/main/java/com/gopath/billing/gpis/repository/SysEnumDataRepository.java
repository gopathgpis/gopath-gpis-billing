package com.gopath.billing.gpis.repository;

import java.util.List;


import com.gopath.billing.gpis.entity.SysEnumData;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;




@Repository
public interface SysEnumDataRepository extends BaseRepository<SysEnumData, String> {
    List<SysEnumData> findBySysEnumId(String sysEnumId);
}
