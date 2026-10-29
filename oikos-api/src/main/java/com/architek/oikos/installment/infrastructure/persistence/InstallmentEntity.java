package com.architek.oikos.installment.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.Formula;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

@Entity
@Table(name = "installment")
@Getter
@Setter
@NoArgsConstructor
public class InstallmentEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "installment_call_id")
    private UUID installmentCallId;

    @Column(name = "outstanding_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal outstandingAmount;

    // Sort keys for the installments table, computed by the database so the
    // listing is ordered before pagination. Read only, never written.

    /**
     * The lot number, read from the unit table: the only column of this listing
     * that lives in another module's table. A read-only sort key, not a
     * dependency on property's model - nothing here maps or writes a unit.
     */
    @Formula("(select u.unit_number from unit u where u.id = unit_id)")
    private String unitNumber;

    /** Ordered on before unitNumber, so that lot 2 comes before lot 10. */
    @Formula("(select length(u.unit_number) from unit u where u.id = unit_id)")
    private Integer unitNumberLength;

    /** The month the fund call covers - what the "Appel de fonds" column shows. */
    @Formula("(select c.period from installment_call c where c.id = installment_call_id)")
    private LocalDate callPeriod;

    /** NOT_SETTLED, PARTIALLY_SETTLED, SETTLED as 0, 1, 2 - same rule as Installment.status(). */
    @Formula("(case when outstanding_amount = 0 then 2 when outstanding_amount < amount then 1 else 0 end)")
    private Integer statusRank;
}
