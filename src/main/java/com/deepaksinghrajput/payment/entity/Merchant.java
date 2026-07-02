package com.deepaksinghrajput.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "merchants")
@Getter
@Setter
@NoArgsConstructor
public class Merchant extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String merchantCode;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean kycVerified = false;

    @Column(nullable = false)
    private boolean active = true;
}
