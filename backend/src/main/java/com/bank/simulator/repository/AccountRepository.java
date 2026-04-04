package com.bank.simulator.repository;

import com.bank.simulator.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    List<AccountEntity> findAllByOrderByCreatedDesc();
}
