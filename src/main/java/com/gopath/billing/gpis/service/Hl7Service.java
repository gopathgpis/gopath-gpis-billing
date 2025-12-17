package com.gopath.billing.gpis.service;

import cn.hutool.core.util.StrUtil;
import com.gopath.billing.gpis.dao.GpisBillingDao;
import com.gopath.domain.constant.GoPathEnum;
import com.gopath.domain.persistent.*;
import com.gopath.uti.portal.model.UtiPortalCustomer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.gopath.billing.gpis.util.TimeZoneUtils.formateByInstant;

@Service
@Slf4j(topic = "c.g.b.g.service.Hl7Service")
public class Hl7Service {

    private static final char CARRIAGE_RETURN = 13;

    @Autowired
    GpisBillingDao gpisBillingDao;

    public String getHL7String(UtiCase utiCase, BillingDetail billingDetail, String timeZone) throws Exception {
        StringBuffer hl7Str = new StringBuffer();

        var customer = gpisBillingDao.getUtiPortalCustomer(utiCase.getUtiPortalOrderId());
        var patient = gpisBillingDao.getPatientById(utiCase.getPatientId());

        String gopathId = utiCase.getGopathId();

        String mshStr = connectHl7MSH(timeZone, gopathId);
        log.info("MSH: {}", mshStr);
        String evnStr = connectHl7EVN(timeZone);
        log.info("EVN: {}", evnStr);
        String pidStr = getPIDString(customer, patient);
        log.info("PID: {}", pidStr);
        String pv1Str = getPV1String(utiCase);
        log.info("PV1: {}", pv1Str);
        String obxStr = getObxString(utiCase, timeZone);
        log.info("Obx: {}", obxStr);
        String in1Str = getIn1String(utiCase, customer);
        log.info("In1: {}", in1Str);
        String ft1Str = getFt1String(utiCase, customer, timeZone);
        log.info("Ft1: {}", ft1Str);

        hl7Str.append(mshStr).append(CARRIAGE_RETURN)
                .append(evnStr).append(CARRIAGE_RETURN)
                .append(pidStr).append(CARRIAGE_RETURN)
                .append(pv1Str).append(CARRIAGE_RETURN)
                .append(obxStr).append(CARRIAGE_RETURN)
                .append(in1Str).append(CARRIAGE_RETURN)
                .append(ft1Str).append(CARRIAGE_RETURN);

        return hl7Str.toString();
    }

    public String getFt1String(UtiCase utiCase, UtiPortalCustomer customer, String timeZone) throws Exception {
        // financial transactions
        String TransactionDate = formateByInstant(utiCase.getCollectionDate(), timeZone, "yyyyMMddHHmmss");
        SysUser signoutUser = gpisBillingDao.getUserById(utiCase.getPathologistId());

        //FT1-16 and FT1-37 is the Rendering Physician Facility Name

        return connectHl7FT1(TransactionDate, "", "1", "",
                utiCase.getFacilityName(), utiCase.getIcdCode(), "", signoutUser.getUserCode(),
                signoutUser.getLastName(), signoutUser.getFirstName(), utiCase.getGopathId(), "87999",
                utiCase.getFacilityName(), 1);
    }

    public String getIn1String(UtiCase utiCase, UtiPortalCustomer customer) {
        return connectHl7IN1(utiCase.getInsurer(), "", "", "",
                "", "", utiCase.getGroupNo(), utiCase.getInsurerLastName(), utiCase.getInsurerFirstName(),
                utiCase.getInsurerMiddleName(), utiCase.getInsurerRelationship(), utiCase.getInsurerBirth(),
                utiCase.getInsurerCode());
    }

    private String getObxString(UtiCase utiCase, String timeZone) {
        if (utiCase.getCollectionDate() == null) {
            utiCase.setCollectionDate(utiCase.getCreateTime());
        }

        String resultStatus = "F";

        if (GoPathEnum.CASE_STATUS_PRELIMINARY.getValue().equals(utiCase.getStatus())) {
            resultStatus = "P";
        } else if (GoPathEnum.CASE_STATUS_CANCELED.getValue().equals(utiCase.getStatus())) {
            resultStatus = "X";
        } else if (StringUtils.equals(utiCase.getCorrectFlag(), "1")) {
            resultStatus = "C";
        }

        String observeDate = formateByInstant(utiCase.getCollectionDate(), timeZone, "yyyyMMddHHmmss");
        return connectHl7OBX(resultStatus, observeDate);
    }

    private String getPV1String(UtiCase utiCase) throws Exception {

        String facilityId = utiCase.getFacilityId();
        String facilityCode = gpisBillingDao.getFacilityCode(facilityId);

        String physicianCode = utiCase.getPhysicianNpi();

        Physician physician = gpisBillingDao.getPhysicianById(utiCase.getPhysicianId());
        String physicianLastName = "", physicianFirstName = "";

        if (physician != null) {
            physicianLastName = physician.getLastName();
            physicianFirstName = physician.getFirstName();
        }

        return connectHl7PV1("", physicianCode, physicianLastName, physicianFirstName,
                facilityCode, "GoPath Pathology Associates", "");
    }

    private String getPIDString(UtiPortalCustomer customer, Patient patient) {
        return connectHl7PID(
                patient.getPatientCode(),
                customer.getLastName(),
                customer.getFirstName(),
                patient.getMiddleName(),
                customer.getDateOfBirth(),
                patient.getGender(),
                customer.getBillingAddr1(),
                customer.getBillingCity(),
                customer.getBillingState(),
                customer.getBillingZipCode(),
                "USA",
                customer.getPhone(),
                patient.getSocialSec()
        );
    }

    private String connectHl7PID(String patientId, String lastName, String firstName, String middleName,
                                 String showBirthday, String sex, String address, String city, String state,
                                 String zipcode, String country, String phoneNumber, String ssn) {

        String dateOfBirth = dateOfBirthFormat(showBirthday);
        String gender = StringUtils.isNotBlank(sex) ? sex.substring(0, 1) : "U";
        country = StrUtil.emptyToDefault(ssn, "US");
        ssn = StrUtil.emptyToDefault(ssn, "000-00-0000");
        phoneNumber = StrUtil.emptyToDefault(phoneNumber, "");

        return "PID|1|" + patientId + "|||" + lastName + "^" + firstName + "^" + middleName + "||" + dateOfBirth + "|" +
                gender + "|||" + address + "^^" + city + "^" + state + "^" + zipcode + "^" + country + "||" +
                phoneNumber + "||||||" + ssn + "|";
    }

    private String connectHl7PV1(String servicePlace, String idNumber, String lastName, String firstName,
                                 String facilityNumber, String facility, String admitDate) {
        String visitNumber = "";

        // O'Carroll^Daniel, 这样发过去是空值，要把O' 去掉。像这样：Carroll^Daniel， Kareo就可以找到这个医生。
        return "PV1|1|W|" + servicePlace + "|||||" + idNumber + "^" + removeApostrophe(lastName) + "^" + firstName
                + "^^^^^^^^|||||||||||" + visitNumber + "||||||||||||||||||||" + facilityNumber + "^" + facility
                + "|||||" + admitDate + "|";
    }

    public String connectHl7IN1(String insuranceCompany, String insuranceAddress, String insuranceCity,
                                String insuranceState, String zipCode, String country, String groupNumber,
                                String lastName, String firstName, String middleName, String insuranceRelation,
                                String inrancePersonalBirth, String insuranceId) {

        String insuranceBirth = "";

        if (StringUtils.isNotEmpty(inrancePersonalBirth)) {
            insuranceBirth = dateOfBirthFormat(inrancePersonalBirth);
        }

        country = StrUtil.emptyToDefault(country, "USA");

        if ( "".equals(insuranceAddress)) {
            country = "";
        }

        return "IN1|1||8|" + insuranceCompany + "|" + insuranceAddress + "^^" + insuranceCity + "^" + insuranceState + "^"
                + zipCode + "^" + country + "|||" + groupNumber + "||||||||" + lastName + "^" + firstName + "^" + middleName
                + "|" + insuranceRelation + "|" + insuranceBirth + "|||||||||||||||||||||||||||||||" + insuranceId + "|";
    }

    private String connectHl7OBX(String resultStatus, String observationDateTime) {
        return "OBX|1||||||||||" + resultStatus + "|||" + observationDateTime + "|";
    }

    private String connectHl7FT1(String transactionDate, String transactionType, String transactionQuantity,
                                 String transactionAmount, String patientFacility, String icdCode, String icdCodeNumber,
                                 String submitCode, String lastName, String firstName, String gopathId,
                                 String procedureCode, String financialTransaction, int index) {

        transactionType = StrUtil.emptyToDefault(transactionType, "CG");
        transactionQuantity = StrUtil.emptyToDefault(transactionQuantity, "1");
        transactionAmount = StrUtil.emptyToDefault(transactionAmount, "ea");
        icdCodeNumber = StrUtil.emptyToDefault(icdCodeNumber, "1");
        procedureCode = StrUtil.emptyToDefault(procedureCode, "");

        return "FT1|" + index + "|||" + transactionDate + "||" + transactionType + "||||" + transactionQuantity + "||" +
                transactionAmount + "||||" + patientFacility + "|||" + icdCode + "^" + icdCodeNumber + "|" + submitCode
                + "^" + lastName + "^" + firstName + "^|||" + gopathId + "||" + procedureCode + "||||||||||||"
                + financialTransaction + "|";
    }

    private String connectHl7MSH(String timeZone, String gopathId) {
        String messageDate = formateByInstant(new Date().getTime(), timeZone, "yyyyMMddHHmmss");
        String messageControlId = gopathId;
        return "MSH|^~\\&|SPI|GoPath Labs|Billing|GoPath Billing|" + messageDate + "||DFT^P03|" + messageControlId + "|P|2.3|";
    }

    private String connectHl7EVN(String timeZone) {
        String EventTypeCode = "P03"; //事件类型编码
        //输入交易记录的日期/时间
        String recordDate = formateByInstant(new Date().getTime(), timeZone, "yyyyMMddHHmmss");
        String EVN = "EVN|P03|" + recordDate + "|";
        return EVN;
    }

    private String removeApostrophe(String orig) {
        if (StringUtils.isNotBlank(orig) && StringUtils.contains(orig, "'")) {
            int apostropheIndex = orig.indexOf("'");
            orig = orig.substring(apostropheIndex + 1);
            log.info("Removed ' from orig: {}", orig);
        }

        return orig;
    }

    private static String dateOfBirthFormat(String showBirthday) {
        String dateOfBirth = "";

        if (StringUtils.isNotBlank(showBirthday) && !StringUtils.equals(showBirthday, "Invalid Date")) {
            String[] barr = StringUtils.split(showBirthday, "/");
            dateOfBirth += barr[2] + barr[0] + barr[1];
        }

        return dateOfBirth;
    }
}