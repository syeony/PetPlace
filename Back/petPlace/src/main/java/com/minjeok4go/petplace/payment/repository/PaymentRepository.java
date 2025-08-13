package com.minjeok4go.petplace.payment.repository;


import com.minjeok4go.petplace.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByMerchantUid(String merchantUid);

    Optional<Payment> findByImpUid(String impUid);

    Optional<Payment> findByReservationId(Long reservationId);

    // 개발용: 특정 상태와 merchantUid 패턴으로 최근 결제 찾기
    Optional<Payment> findTopByStatusAndMerchantUidContainingOrderByCreatedAtDesc(
            Payment.PaymentStatus status, String merchantUidPattern);
}
