package com.sandy.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "jr_ish_CO_TRIGGERS")
@Data
public class CoTriggersEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer triggerId;
    private Integer caseNo;
    @Lob
    private byte[] coNoticePdf;
    @Column(length=30)
    private String triggerStatus = "pending";

}
