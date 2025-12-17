package com.gopath.billing.gpis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "{ com.gopath.domain.persistent, com.gopath.uti.portal.model, com.gopath.billing.gpis.entity }")
public class GopathGpisBillingApplication {

	public static void main(String[] args) {
		SpringApplication.run(GopathGpisBillingApplication.class, args);
	}

}
