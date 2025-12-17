package com.gopath.billing.gpis.dao;

import com.gopath.billing.gpis.entity.SftpExport;
import com.gopath.billing.gpis.entity.SysEnum;
import com.gopath.billing.gpis.entity.SysEnumData;
import com.gopath.billing.gpis.repository.*;
import com.gopath.domain.persistent.*;
import com.gopath.uti.portal.model.UtiPortalCustomer;
import com.stripe.model.issuing.Cardholder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Component
@Slf4j(topic = "c.g.b.g.dao.GpisBillingDao")
public class GpisBillingDao {

    @Autowired
    UtiCaseRepo utiCaseRepo;

    @Autowired
    BillingDetailRepo billingDetailRepo;

    @Autowired
    OrganizationRepo organizationRepo;

    @Autowired
    PhysicianRepo physicianRepo;

    @Autowired
    SysUserRepo sysUserRepo;

    @Autowired
    PatientRepo patientRepo;

    @Autowired
    UtiPaymentStripeRepo utiPaymentStripeRepo;

    @Autowired
    UtiPortalCustomerRepo utiPortalCustomerRepo;

    @Autowired
    SftpExportRepository sftpExportRepository;

    @Autowired
    SysEnumRepository sysEnumRepository;

    @Autowired
    SysEnumDataRepository sysEnumDataRepository;


    public UtiCase findByUtiPortalOrderId(String utiPortalOrderId) throws Exception {
        List<UtiCase> utiCaseList = utiCaseRepo.findAllByUtiPortalOrderId(utiPortalOrderId);

        if (utiCaseList.size() == 0) {
            log.error("Record with the given uti_portal_order_id not found: {}", utiPortalOrderId);
            throw new IllegalArgumentException("Record with the given uti_portal_order_id not found: " + utiPortalOrderId);
        }

        if (utiCaseList.size() > 1) {
            log.error("Multiple records with the given uti_portal_order_id were found: {} {}", utiPortalOrderId, utiCaseList.size());
            throw new IllegalArgumentException("Multiple records with the given uti_portal_order_id were found: " + utiPortalOrderId + " " + utiCaseList.size());
        }

        return utiCaseList.get(0);
    }

    public BillingDetail createOrUpdateBillingDetailRecord(UtiCase utiCase) throws Exception {
        BillingDetail billingDetail = getBillingDetailByCaseIdForUpsert(utiCase.getId());

        billingDetail
                .setCreateTime(System.currentTimeMillis())
                .setIsDelete("0")

                .setCaseId(utiCase.getId())
                .setCptCode("87999")
                .setFinalCptCode("87999")
                .setGopathId(utiCase.getGopathId())
                .setCollectionDate(utiCase.getCollectionDate())
                .setFacilityId(utiCase.getFacilityId())
                .setFacilityName(utiCase.getFacilityName())
                .setGender(utiCase.getGender())
                .setPathologist(utiCase.getPathologist())
                .setPathologistId(utiCase.getPathologistId())
                .setPatientId(utiCase.getPatientId())
                .setPatientName(utiCase.getFirstName() + " " + utiCase.getLastName())
                .setReceivedDate(utiCase.getReceivedDate())
                .setSignoutDate(utiCase.getSignoutTime())
                .setSampleCount(1)
                .setShowCollectionDate(getDateFromLong(utiCase.getCollectionDate()))
                .setShowBirthday(utiCase.getDateOfBirth())
                .setAttendingPhysician(utiCase.getPhysician())
                .setAttendingPhysicianId(utiCase.getPhysicianId())
                .setBusinessType("uti")
                .setTissueType("Urine")

                .setProcedureName("UTI Panel")
                .setApType("") // set to empty, not null
                .setEditCptCode(null)
                .setEditHistologyCptCode(null)
                .setEditIcdCode(null)

                .setBillingType("1") // bill insurance
                .setCorrectAddendumFlag("")
                .setSample("A")
                .setSendUser("auto")
                .setSendUserId("")
        ;

        // to be updated after sending Kareo

        if ( billingDetail.getId() == null) {
            billingDetail
                    .setBatchId(utiCase.getId() + "_" + System.currentTimeMillis())
                    .setBillingStatus("New")
                    .setSendKareoDate(0L)
                    .setSftpBillStatus("0")
            ;
        }

        billingDetailRepo.save(billingDetail);

        return billingDetail;
    }

    public UtiPortalCustomer getUtiPortalCustomer(String utiPortalOrderId) throws Exception {

        var optionalUtiPaymentStripe = utiPaymentStripeRepo.findByOrderIdAndIsShipping(utiPortalOrderId, "0");

        if (optionalUtiPaymentStripe.isEmpty()) {
            log.error("No stripe payment found with given order id: {}", utiPortalOrderId);
            throw new Exception("No stripe payment found with given order id: " + utiPortalOrderId);
        } else if (optionalUtiPaymentStripe.get().size() > 1) {
            log.error("More than 1 stripe payment found with given order id: {}", utiPortalOrderId);
            throw new Exception("More than 1 stripe payment found with given order id: " + utiPortalOrderId);
        }

        String portalCustomerId = optionalUtiPaymentStripe.get().get(0).getPortalCustomerId();
        var optionalPortalCustomer = utiPortalCustomerRepo.findById(portalCustomerId);

        if (optionalPortalCustomer.isEmpty()) {
            log.error("No portal customer found with given customer id: {} {}", portalCustomerId, utiPortalOrderId);
            throw new Exception("No portal customer found with given customer id: " + portalCustomerId + " " + utiPortalOrderId);
        }

        return optionalPortalCustomer.get();
    }

    public String getFacilityCode(String facilityId) throws Exception {
        Optional<Organization> optionalOrganization = organizationRepo.findById(facilityId);

        if (optionalOrganization.isEmpty()) {
            log.error("Facility not found by the id of: {}", facilityId);
            throw new Exception("Facility not found by the id of: " + facilityId);
        }

        return optionalOrganization.get().getIntCode() + "";
    }

    public Physician getPhysicianById(String physicianId) throws Exception {
        Optional<Physician> optionalPhysician = physicianRepo.findById(physicianId);

        if (optionalPhysician.isEmpty()) {
            log.info("Physician not found by the id of: {}", physicianId);
            return null;
        }

        return optionalPhysician.get();
    }

    public SysUser getUserById(String pathologistId) throws Exception {
        Optional<SysUser> optionalSysUser = sysUserRepo.findById(pathologistId);

        if (optionalSysUser.isEmpty()) {
            log.error("User not found by the id of: {}", pathologistId);
            throw new Exception("User not found by the id of: " + pathologistId);
        }

        return optionalSysUser.get();
    }

    public Patient getPatientById(String id) throws Exception {
        Optional<Patient> optionalPatient = patientRepo.findById(id);

        if (optionalPatient.isEmpty()) {
            log.error("Patient not found by the id of: {}", id);
            throw new Exception("Patient not found by the id of: " + id);
        }

        return optionalPatient.isEmpty() ? null : optionalPatient.get();
    }

    public BillingDetail getBillingDetailByCaseIdForUpsert(String caseId) throws Exception {
        Optional<List<BillingDetail>> optionalBillingDetailList = billingDetailRepo.findAllByCaseId(caseId);

        if (optionalBillingDetailList.isEmpty() || optionalBillingDetailList.get().size() == 0) {
            return new BillingDetail();
        }

        if (optionalBillingDetailList.get().size() > 1) {
            log.error("Multiple Billing Detail records for the case were found: {}", caseId);
            throw new Exception("Multiple Billing Detail records for the case were found: " + caseId);
        }

        return optionalBillingDetailList.get().get(0);
    }

    public BillingDetail getBillingDetailByCaseId(String caseId) throws Exception {
        Optional<List<BillingDetail>> optionalBillingDetailList = billingDetailRepo.findAllByCaseId(caseId);

        if (optionalBillingDetailList.isEmpty() || optionalBillingDetailList.get().size() == 0) {
            log.error("Billing Detail record for the case was not found: {}", caseId);
            throw new Exception("Billing Detail record for the case was not found: " + caseId);
        }

        if (optionalBillingDetailList.get().size() > 1) {
            log.error("Multiple Billing Detail records for the case were found: {}", caseId);
            throw new Exception("Multiple Billing Detail records for the case were found: " + caseId);
        }

        return optionalBillingDetailList.get().get(0);
    }

    public List<SysEnumData> loadEnumDataByEnumCode(String sysEnumCode) throws Exception {
        SysEnum sysEnum = sysEnumRepository.findBySysEnumCode(sysEnumCode);
        List<SysEnumData> sysEnumDataLst = sysEnumDataRepository.findBySysEnumId(sysEnum.getId());
        return sysEnumDataLst;
    }

    public void updateUtiCaseAndBillingDetail(String status, String caseId) throws Exception {
        utiCaseRepo.updateSftpBillStatusById(status, caseId);
        billingDetailRepo.updateBillingDetailByCaseId("sent", "1", new Date().getTime()+"", caseId);
    }

    public void saveSftpExportRecord(SftpExport sftpExport) throws Exception {
        sftpExportRepository.save(sftpExport);
    }

    private String getDateFromLong(long dateTimeInMillis) {
        return new SimpleDateFormat("MM/dd/yyyy").format(new Date(dateTimeInMillis));
    }
}