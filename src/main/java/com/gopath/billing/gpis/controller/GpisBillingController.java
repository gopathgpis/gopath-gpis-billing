package com.gopath.billing.gpis.controller;

import com.gopath.billing.gpis.dto.EndpointResponse;
import com.gopath.billing.gpis.service.UtiBillingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/gpis-billing")
@Slf4j(topic = "c.g.b.g.controller.GpisBillingController")
public class GpisBillingController {

    @Autowired
    UtiBillingService utiBillingService;

    @PostMapping("/do-billing-uti")
    @ResponseBody
    public ResponseEntity<EndpointResponse<String>> doBillingUti(@RequestParam("utiPortalOrderId") String utiPortalOrderId) throws Exception {
        log.info("Creating billing details: {}", utiPortalOrderId);

        EndpointResponse<String> endpointResponse = new EndpointResponse<>();

        try {
            endpointResponse
                    .setCode(HttpStatus.OK.value())
                    .setMessage("Successful!")
                    .setResult(utiBillingService.doBillingUti(utiPortalOrderId));
        }
        catch (Exception e) {
            endpointResponse
                    .setCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setMessage("Error: " + e.getMessage())
                    .setResult(null);
        }


        return ResponseEntity.ok(endpointResponse);
    }

    @PostMapping("/send-kareo-uti")
    @ResponseBody
    public ResponseEntity<EndpointResponse<String>> sendKareoUti(@RequestParam("utiPortalOrderId") String utiPortalOrderId) throws Exception {
        log.info("Sending billing to Kareo: {}", utiPortalOrderId);

        EndpointResponse<String> endpointResponse = new EndpointResponse<>();

        try {
            endpointResponse
                    .setCode(HttpStatus.OK.value())
                    .setMessage("Successful!")
                    .setResult(utiBillingService.exportBilling(utiPortalOrderId));
        }
        catch (Exception e) {
            endpointResponse
                    .setCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setMessage("Error: " + e.getMessage())
                    .setResult(null);
        }

        return ResponseEntity.ok(endpointResponse);
    }
}