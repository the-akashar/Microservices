package com.eazybytes.loans.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Data;

import java.time.LocalDateTime;


@MappedSuperclass
@Data
public class BaseEntity {
    private LocalDateTime createdAt;

    private String createdBy;

    private String  updatedAt;

    private String updatedBy;
}
