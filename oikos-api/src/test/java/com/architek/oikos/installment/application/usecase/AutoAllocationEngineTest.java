package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.architek.oikos.installment.application.port.out.AccountMovement;
import com.architek.oikos.installment.application.port.out.AccountMovementsPort;
import com.architek.oikos.installment.domain.model.Allocation;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.AllocationRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.AllocationId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Covers the four illustrated scenarios of the cotisation/accounting spec:
 * advance payment (§4), partial payment (§5), one payment split across
 * several installments (§6), and one installment settled by several
 * payments (§7). Uses an in-memory AllocationRepository fake rather than
 * Mockito stubs, since the engine's correctness depends on sums that
 * accumulate across successive allocate() calls within a test.
 */
class AutoAllocationEngineTest {

    private final EntityId accountId = EntityId.newId();
    private final AccountMovementsPort accountMovementsPort = mock(AccountMovementsPort.class);
    private final InstallmentRepository installmentRepository = mock(InstallmentRepository.class);
    private final InMemoryAllocationRepository allocationRepository = new InMemoryAllocationRepository();
    private final AutoAllocationEngine engine =
            new AutoAllocationEngine(accountMovementsPort, installmentRepository, allocationRepository);

    private final List<AccountMovement> movements = new ArrayList<>();
    private final List<Installment> installments = new ArrayList<>();

    private AccountMovement creditOf(String amount, Instant occurredOn) {
        AccountMovement movement = new AccountMovement(EntityId.newId(), occurredOn, new BigDecimal(amount));
        movements.add(movement);
        when(accountMovementsPort.listCreditMovements(accountId)).thenReturn(movements);
        return movement;
    }

    private Installment installmentOf(String amount, LocalDate dueDate) {
        Installment installment = Installment.create(InstallmentId.newId(), accountId, EntityId.newId(), dueDate,
                Amount.of(new BigDecimal(amount)));
        installments.add(installment);
        when(installmentRepository.findAllByAccountId(accountId)).thenReturn(installments);
        return installment;
    }

    @Test
    void advance_payment_is_allocated_once_a_matching_installment_is_created() {
        creditOf("1000", Instant.parse("2027-01-01T00:00:00Z"));
        engine.allocate(accountId); // no installment yet: nothing to do

        assertThat(allocationRepository.all()).isEmpty();

        Installment installment = installmentOf("250", LocalDate.of(2027, 1, 1));
        engine.allocate(accountId);

        assertThat(allocationRepository.all()).hasSize(1);
        Allocation allocation = allocationRepository.all().get(0);
        assertThat(allocation.getInstallmentId()).isEqualTo(installment.getId());
        assertThat(allocation.getAllocatedAmount().value()).isEqualByComparingTo("250");
        assertThat(allocationRepository.sumAllocatedByMovementId(movements.get(0).id())).isEqualByComparingTo("250");
    }

    @Test
    void a_payment_smaller_than_the_installment_creates_a_single_partial_allocation() {
        installmentOf("250", LocalDate.of(2027, 1, 1));
        creditOf("100", Instant.parse("2027-01-01T00:00:00Z"));

        engine.allocate(accountId);

        assertThat(allocationRepository.all()).hasSize(1);
        assertThat(allocationRepository.all().get(0).getAllocatedAmount().value()).isEqualByComparingTo("100");
    }

    @Test
    void a_single_payment_is_split_fifo_across_several_installments() {
        Installment january = installmentOf("250", LocalDate.of(2027, 1, 1));
        Installment april = installmentOf("250", LocalDate.of(2027, 4, 1));
        Installment july = installmentOf("250", LocalDate.of(2027, 7, 1));
        Installment october = installmentOf("250", LocalDate.of(2027, 10, 1));
        creditOf("1000", Instant.parse("2027-01-01T00:00:00Z"));

        engine.allocate(accountId);

        assertThat(allocationRepository.all()).hasSize(4);
        assertThat(allocationRepository.sumAllocatedByInstallmentId(january.getId())).isEqualByComparingTo("250");
        assertThat(allocationRepository.sumAllocatedByInstallmentId(april.getId())).isEqualByComparingTo("250");
        assertThat(allocationRepository.sumAllocatedByInstallmentId(july.getId())).isEqualByComparingTo("250");
        assertThat(allocationRepository.sumAllocatedByInstallmentId(october.getId())).isEqualByComparingTo("250");
    }

    @Test
    void an_installment_can_be_settled_by_several_successive_payments() {
        Installment installment = installmentOf("600", LocalDate.of(2027, 1, 1));

        creditOf("300", Instant.parse("2027-01-01T00:00:00Z"));
        engine.allocate(accountId);
        creditOf("100", Instant.parse("2027-01-02T00:00:00Z").plus(1, ChronoUnit.SECONDS));
        engine.allocate(accountId);
        creditOf("200", Instant.parse("2027-01-03T00:00:00Z").plus(2, ChronoUnit.SECONDS));
        engine.allocate(accountId);

        assertThat(allocationRepository.all()).hasSize(3);
        assertThat(allocationRepository.sumAllocatedByInstallmentId(installment.getId())).isEqualByComparingTo("600");
        List<BigDecimal> amounts = allocationRepository.all().stream()
                .map(allocation -> allocation.getAllocatedAmount().value())
                .collect(Collectors.toList());
        assertThat(amounts).containsExactlyInAnyOrder(new BigDecimal("300"), new BigDecimal("100"), new BigDecimal("200"));
    }

    /**
     * Minimal in-memory fake: the engine's FIFO correctness depends on sums
     * that must reflect allocations saved by earlier calls within the same
     * test, which plain Mockito stubbing cannot express without becoming
     * unreadable.
     */
    private static final class InMemoryAllocationRepository implements AllocationRepository {

        private final List<Allocation> allocations = new ArrayList<>();

        List<Allocation> all() {
            return allocations;
        }

        @Override
        public Allocation save(Allocation allocation) {
            allocations.add(allocation);
            return allocation;
        }

        @Override
        public java.util.Optional<Allocation> findById(AllocationId id) {
            return allocations.stream().filter(allocation -> allocation.getId().equals(id)).findFirst();
        }

        @Override
        public void deleteById(AllocationId id) {
            allocations.removeIf(allocation -> allocation.getId().equals(id));
        }

        @Override
        public List<Allocation> findAllByInstallmentId(InstallmentId installmentId) {
            return allocations.stream().filter(allocation -> allocation.getInstallmentId().equals(installmentId)).toList();
        }

        @Override
        public List<Allocation> findAllByMovementId(EntityId movementId) {
            return allocations.stream().filter(allocation -> allocation.getMovementId().equals(movementId)).toList();
        }

        @Override
        public BigDecimal sumAllocatedByMovementId(EntityId movementId) {
            return findAllByMovementId(movementId).stream()
                    .map(allocation -> allocation.getAllocatedAmount().value())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        @Override
        public BigDecimal sumAllocatedByInstallmentId(InstallmentId installmentId) {
            return findAllByInstallmentId(installmentId).stream()
                    .map(allocation -> allocation.getAllocatedAmount().value())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }
}
