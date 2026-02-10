package com.sandy.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Table(name="jr_ish_ELIGIBILITY_DETERMINATION")
@Data
@Entity
public class EligibilityDetailsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer edTraceId;
    private Integer caseNo;
    @Column(length=30)
    private String holderName;
    private Long holderSSN;
    @Column(length = 30)
    private String planName;
    @Column(length = 30)
    private String planStatus;
    private LocalDate planStartDate;
    private LocalDate planEndDate;
    private Double benefitAmt;
    @Column(length = 30)
    private String denialReason;

}
