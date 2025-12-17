package com.gopath.billing.gpis.entity;

// import com.gopath.billing.gpis.util.ShiroUtil;
import com.gopath.domain.persistent.SysUser;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class EntityListener {

    /**
     * 需要更新update时间的
     */
    private static final Set<Class<?>> SKIP_AUDITING_CLASSES = new HashSet<>(Arrays.asList(
    ));

    /**
     * 保存前处理
     */
    @PrePersist
    public void prePersist(BaseEntity<?> entity) {
        entity.setIsDelete("0");
        entity.setCreateTime(new Date().getTime());
    }

    @PreUpdate
    public void preUpdate(BaseEntity<?> entity) {
        if(!shouldAuditing(entity)){
            return;
        }
        //获取当前用户
        SysUser currUser = null;
        try {
            currUser = null; // ShiroUtil.getCurrentUser();
        } catch (Exception e) {
            // 日志可选
            // log.warn("当前用户未登录，跳过审计字段填充", e);
        }
        String currId = "";
        String currName = "";
        if(currUser != null){
            currId = currUser.getId();
            // currName = currUser.getName();
        }
        entity.setLastModifiedTime(new Date().getTime());
        entity.setLastModifiedBy(currName);
        entity.setLastModifiedById(currId);
    }

    /**
     * 判断是否跳过审计字段的更新
     * @param entity 实体类对象
     * @return 是否跳过
     */
    private boolean shouldAuditing(BaseEntity<?> entity) {
        return SKIP_AUDITING_CLASSES.contains(entity.getClass());
    }

}

