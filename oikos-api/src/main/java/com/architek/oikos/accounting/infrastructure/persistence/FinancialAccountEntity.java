package com.architek.oikos.accounting.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountStatus;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountType;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

@Entity
@Table(name = "financial_account")
@Getter
@Setter
@NoArgsConstructor
public class FinancialAccountEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialAccountType type;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialAccountStatus status;
}
