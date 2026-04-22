package com.bank.simulator.repository;

import com.bank.simulator.entity.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<LoanEntity, Long> {

    List<LoanEntity> findByAccountNumberOrderByApplicationDateDesc(String accountNumber);

    Optional<LoanEntity> findByLoanId(String loanId);

    Optional<LoanEntity> findByReferenceNumber(String referenceNumber);

    List<LoanEntity> findAllByOrderByApplicationDateDesc();

    Long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(l.loanAmount), 0) FROM LoanEntity l WHERE l.status = :status")
    BigDecimal sumLoanAmountByStatus(@Param("status") String status);

    @Query("SELECT COALESCE(AVG(l.eligibilityScore), 0.0) FROM LoanEntity l")
    Double avgEligibilityScore();
}
