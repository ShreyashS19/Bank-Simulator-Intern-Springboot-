package com.bank.simulator.repository;

import com.bank.simulator.entity.AccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    List<AccountEntity> findAllByOrderByCreatedDesc();

    @Query("SELECT a FROM AccountEntity a JOIN FETCH a.customer ORDER BY a.created DESC")
    List<AccountEntity> findAllWithCustomer();

    @Query(
            value = "SELECT a FROM AccountEntity a JOIN FETCH a.customer ORDER BY a.created DESC",
            countQuery = "SELECT COUNT(a) FROM AccountEntity a"
    )
    Page<AccountEntity> findAllWithCustomer(Pageable pageable);

    @Query("SELECT a FROM AccountEntity a JOIN FETCH a.customer WHERE a.accountNumber = :accountNumber")
    Optional<AccountEntity> findByAccountNumberWithCustomer(@Param("accountNumber") String accountNumber);
    
    @Query("SELECT c.email FROM AccountEntity a JOIN a.customer c WHERE a.accountNumber = :accountNumber")
    Optional<String> findCustomerEmailByAccountNumber(@Param("accountNumber") String accountNumber);
}
