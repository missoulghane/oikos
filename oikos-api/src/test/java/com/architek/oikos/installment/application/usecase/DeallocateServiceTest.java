package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.installment.application.command.DeallocateCommand;
import com.architek.oikos.installment.domain.exception.AllocationNotFoundException;
import com.architek.oikos.installment.domain.model.Allocation;
import com.architek.oikos.installment.domain.repository.AllocationRepository;
import com.architek.oikos.installment.domain.valueobject.AllocationId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class DeallocateServiceTest {

    @Mock
    private AllocationRepository allocationRepository;

    private DeallocateService newService() {
        return new DeallocateService(allocationRepository);
    }

    @Test
    void deallocating_an_existing_allocation_deletes_it_without_touching_movements() {
        Allocation allocation = Allocation.create(AllocationId.newId(), EntityId.newId(), InstallmentId.newId(),
                Amount.of(BigDecimal.TEN));
        when(allocationRepository.findById(allocation.getId())).thenReturn(Optional.of(allocation));

        newService().deallocate(new DeallocateCommand(allocation.getId()));

        verify(allocationRepository).deleteById(allocation.getId());
    }

    @Test
    void deallocating_an_unknown_allocation_is_rejected() {
        AllocationId id = AllocationId.newId();
        when(allocationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().deallocate(new DeallocateCommand(id)))
                .isInstanceOf(AllocationNotFoundException.class);
    }
}
