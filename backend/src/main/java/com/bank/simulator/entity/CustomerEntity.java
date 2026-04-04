package com.bank.simulator.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers", uniqueConstraints = {
    @UniqueConstraint(columnNames = "aadhar_number"),
    @UniqueConstraint(columnNames = "phone_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "phone_number", nullable = false, unique = true, length = 10)
    private String phoneNumber;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @JsonIgnore
    @Column(name = "customer_pin", nullable = false, length = 6)
    private String customerPin;

    @Column(name = "aadhar_number", nullable = false, unique = true, length = 12)
    private String aadharNumber;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "Inactive";

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<AccountEntity> accounts = new ArrayList<>();
}
