package com.architek.oikos.installment.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.installment.infrastructure.mapper.InstallmentPersistenceMapperImpl;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({InstallmentRepositoryAdapter.class, InstallmentPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class InstallmentRepositoryAdapterFindAllByInstallmentCallIdDataJpaTest {

    @Autowired
    private InstallmentRepositoryAdapter adapter;

    @Test
    void restricts_results_to_the_given_installment_call_id() {
        InstallmentCallId callA = InstallmentCallId.newId();
        InstallmentCallId callB = InstallmentCallId.newId();

        Installment fromCallA = Installment.create(InstallmentId.newId(), EntityId.newId(),
                LocalDate.of(2026, 2, 5), Amount.of(new BigDecimal("300")), callA);
        adapter.save(fromCallA);
        adapter.save(Installment.create(InstallmentId.newId(), EntityId.newId(),
                LocalDate.of(2026, 2, 5), Amount.of(new BigDecimal("100")), callB));
        adapter.save(Installment.create(InstallmentId.newId(), EntityId.newId(),
                LocalDate.of(2026, 2, 5), Amount.of(new BigDecimal("999"))));

        var result = adapter.findAllByInstallmentCallId(callA);

        assertThat(result).extracting(Installment::getId).containsExactly(fromCallA.getId());
    }

    @Test
    void deletes_only_the_installments_of_the_given_installment_call_id() {
        InstallmentCallId callA = InstallmentCallId.newId();
        InstallmentCallId callB = InstallmentCallId.newId();

        Installment fromCallA = Installment.create(InstallmentId.newId(), EntityId.newId(),
                LocalDate.of(2026, 2, 5), Amount.of(new BigDecimal("300")), callA);
        adapter.save(fromCallA);
        Installment fromCallB = Installment.create(InstallmentId.newId(), EntityId.newId(),
                LocalDate.of(2026, 2, 5), Amount.of(new BigDecimal("100")), callB);
        adapter.save(fromCallB);

        adapter.deleteAllByInstallmentCallId(callA);

        assertThat(adapter.findAllByInstallmentCallId(callA)).isEmpty();
        assertThat(adapter.findAllByInstallmentCallId(callB)).extracting(Installment::getId).containsExactly(fromCallB.getId());
    }
}
