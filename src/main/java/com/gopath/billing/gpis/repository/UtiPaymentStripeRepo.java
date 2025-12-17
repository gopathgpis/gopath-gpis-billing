package com.gopath.billing.gpis.repository;

import com.gopath.uti.portal.model.UtiPaymentStripe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UtiPaymentStripeRepo extends JpaRepository<UtiPaymentStripe, String> {

    @Query(value = "SELECT * FROM uti_payment_stripe WHERE order_id = ?1 AND is_shipping = ?2", nativeQuery = true)
    Optional<List<UtiPaymentStripe>> findByOrderIdAndIsShipping(String orderId, String isShipping);
}