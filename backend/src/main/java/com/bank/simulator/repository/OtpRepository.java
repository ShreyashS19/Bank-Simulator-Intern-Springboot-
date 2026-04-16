package com.bank.simulator.repository;

import com.bank.simulator.entity.OtpEntity;
import com.bank.simulator.entity.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, Long> {

    /**
     * Fetch the most recent unused OTP for the given email + purpose.
     * Used when validating a submitted OTP.
     */
    Optional<OtpEntity> findTopByEmailAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(
            String email, OtpPurpose purpose);

    /**
     * Layer-1 rate limit: count how many OTPs were generated for this
     * email+purpose after the given time threshold (e.g. now minus 1 hour).
     */
    long countByEmailAndPurposeAndCreatedAtAfter(
            String email, OtpPurpose purpose, LocalDateTime threshold);

    /**
     * Invalidate (mark used) all existing unused OTPs for email+purpose
     * before generating a new one — ensures one active OTP per context.
     */
    @Modifying
    @Transactional
    @Query("UPDATE OtpEntity o SET o.isUsed = true " +
           "WHERE o.email = :email AND o.purpose = :purpose AND o.isUsed = false")
    void invalidateExistingOtps(@Param("email") String email,
                                @Param("purpose") OtpPurpose purpose);

    /**
     * Scheduled cleanup: remove all expired OTP records to keep the table lean.
     * Called by @Scheduled in OtpCleanupService.
     */
    @Modifying
    @Transactional
    void deleteByExpiryTimeBefore(LocalDateTime now);
}
