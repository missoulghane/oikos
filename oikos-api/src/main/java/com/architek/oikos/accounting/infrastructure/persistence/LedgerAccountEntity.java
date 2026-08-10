package com.architek.oikos.accounting.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

@Entity
@Table(name = "ledger_account")
@Getter
@Setter
@NoArgsConstructor
public class LedgerAccountEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "property_id")
    private UUID propertyId;

    @Column(name = "unit_id")
    private UUID unitId;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String label;

    @Column(name = "account_class", nullable = false)
    private Integer accountClass;

    @Column(nullable = false)
    private String nature;

    @Column(nullable = false)
    private boolean collective;

    private String role;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal balance;
}
