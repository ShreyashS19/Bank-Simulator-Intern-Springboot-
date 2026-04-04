package com.bank.simulator.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts", uniqueConstraints = {
    @UniqueConstraint(columnNames = "account_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private CustomerEntity customer;

    @Column(name = "account_number", nullable = false, unique = true, length = 30)
    private String accountNumber;

    @Column(name = "aadhar_number", nullable = false, length = 12)
    private String aadharNumber;

    @Column(name = "ifsc_code", nullable = false, length = 11)
    private String ifscCode;

    @Column(name = "phone_number_linked", nullable = false, length = 10)
    private String phoneNumberLinked;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.valueOf(600.00);

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "name_on_account", nullable = false, length = 100)
    private String nameOnAccount;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    private LocalDateTime modified;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<TransactionEntity> transactions = new ArrayList<>();
}
