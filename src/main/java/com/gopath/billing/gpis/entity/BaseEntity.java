package com.gopath.billing.gpis.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;



/**
 * @author gavin
 * 主键 uuid 随机字符串
 */
@Data
@MappedSuperclass
@EntityListeners(EntityListener.class)
public abstract class BaseEntity<ID extends Serializable> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(nullable = false, length = 32)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    private String id;

    @Column(name = "name", nullable = true, length = 200)
    private String name;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private Long createTime;

    @Column(name = "is_delete", columnDefinition = "varchar(10) default 0")
    private String isDelete = "0";


    @LastModifiedDate
    @Column(name = "last_modified_time")
    private Long lastModifiedTime;

    @LastModifiedBy
    @Column(name = "last_modified_by", nullable = true, length = 200)
    private String lastModifiedBy;

    @Column(name = "last_modified_by_id", nullable = true, length = 40)
    private String lastModifiedById;

}