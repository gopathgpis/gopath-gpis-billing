package com.gopath.billing.gpis.entity;

import java.io.Serializable;
import java.util.List;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 枚举分类实体
 *
 * @author Nick
 * @date 2014-8-25
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "sys_enum")
public class SysEnum extends BaseEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "sys_enum_group_code", nullable = true, length = 40)
    private String sysEnumGroupCode = "";         //枚举所属模块编码

    @Column(name = "sys_enum_code", nullable = true, length = 100)
    private String sysEnumCode = "";              //枚举编码

    @Transient
    private List<SysEnumData> datas;

    @Transient
    private String enumData; //json字符串

}
