package com.gopath.billing.gpis.entity;

import java.io.Serializable;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 枚举数据实体类
 * `*
 */
@Data
@Entity
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "sys_enum_data")
public class SysEnumData extends BaseEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;


    @Column(name = "sys_enum_id", nullable = true, length = 40)
    private String sysEnumId = "";                       //枚举类别ID

    @Column(name = "sys_enum_data_key", nullable = true, length = 40)
    private String sysEnumDataKey = "";                  //枚举数据的KEY   用于显示

    @Column(name = "sys_enum_data_value", nullable = true, length = 200)
    private String sysEnumDataValue = "";                //枚举数据的VALUE

    @Column(name = "sys_enum_data_key_en", nullable = true, length = 40)
    private String sysEnumDataKeyEn = "";                  //枚举数据的KEY   用于显示英文

    @Column(name = "display_order", nullable = true, length = 10)
    private Integer displayOrder = 0;                   //显示顺序

    @Column(name = "is_default_value", nullable = true, length = 2)
    private String isDefaultValue = "";                 //是否默认显示        1为显示      0为不显示

    @Column(name = "enum_icon", nullable = true, length = 40)
    private String enumIcon = "";

    @Column(name = "enum_condition", nullable = true, length = 100)
    private String enumCondition = ""; //preauth state中标注 finilized状态

    @Column(name = "enum_status", nullable = true, length = 100)
    private String enumStatus= ""; //preauth state，标注active状态

    @Column(name = "other_condition", nullable = true, length = 100)
    private String otherCondition= ""; //preauth state，标注完成状态状态
    public SysEnumData(String sysEnumId, String sysEnumDataKey, String sysEnumDataValue, String sysEnumDataKeyEn, Integer displayOrder, String isDefaultValue, String enumIcon) {
        this.sysEnumId = sysEnumId;
        this.sysEnumDataKey = sysEnumDataKey;
        this.sysEnumDataValue = sysEnumDataValue;
        this.sysEnumDataKeyEn = sysEnumDataKeyEn;
        this.displayOrder = displayOrder;
        this.isDefaultValue = isDefaultValue;
        this.enumIcon = enumIcon;
    }

    public SysEnumData(Object[] result) {
        this.sysEnumDataValue = result[0].toString();
        this.enumCondition = result[1].toString();
        this.otherCondition = result[2].toString();
        this.sysEnumDataKeyEn = result[4].toString();
    }
}

