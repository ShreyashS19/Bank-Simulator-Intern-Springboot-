package com.bank.simulator.repository;

import com.bank.simulator.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    Optional<CustomerEntity> findByAadharNumber(String aadharNumber);

    Optional<CustomerEntity> findByEmail(String email);

    boolean existsByAadharNumber(String aadharNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
