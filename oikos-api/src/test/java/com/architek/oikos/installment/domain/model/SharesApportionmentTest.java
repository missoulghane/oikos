package com.architek.oikos.installment.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.architek.oikos.installment.domain.model.SharesApportionment.Allocation;
import com.architek.oikos.installment.domain.model.SharesApportionment.Share;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class SharesApportionmentTest {

    @Test
    void I9_evenly_dividing_shares_produce_no_rounding_at_all() {
        EntityId unit1 = EntityId.newId();
        EntityId unit2 = EntityId.newId();
        EntityId unit3 = EntityId.newId();

        List<Allocation> allocations = SharesApportionment.apportion(new BigDecimal("1000.00"), List.of(
                new Share(unit1, new BigDecimal("333")),
                new Share(unit2, new BigDecimal("333")),
                new Share(unit3, new BigDecimal("334"))));

        Map<EntityId, BigDecimal> byUnit = allocations.stream()
                .collect(Collectors.toMap(Allocation::unitId, Allocation::amount));
        assertThat(byUnit.get(unit1)).isEqualByComparingTo("333.00");
        assertThat(byUnit.get(unit2)).isEqualByComparingTo("333.00");
        assertThat(byUnit.get(unit3)).isEqualByComparingTo("334.00");
    }

    @Test
    void I9_the_sum_of_allocations_always_equals_the_total_amount_exactly() {
        List<Share> shares = List.of(
                new Share(EntityId.newId(), BigDecimal.ONE),
                new Share(EntityId.newId(), BigDecimal.ONE),
                new Share(EntityId.newId(), BigDecimal.ONE));

        List<Allocation> allocations = SharesApportionment.apportion(new BigDecimal("100.00"), shares);

        BigDecimal sum = allocations.stream().map(Allocation::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(sum).isEqualByComparingTo("100.00");
    }

    @Test
    void I9_the_rounding_cent_goes_to_the_unit_with_the_largest_remainder_ties_broken_by_unit_id() {
        EntityId first = EntityId.of("00000000-0000-0000-0000-000000000001");
        EntityId second = EntityId.of("00000000-0000-0000-0000-000000000002");
        EntityId third = EntityId.of("00000000-0000-0000-0000-000000000003");

        // 100.00 / 3 equal shares -> 33.333... each; the extra cent must go to
        // "first" (the smallest unit id) since all three remainders tie.
        List<Allocation> allocations = SharesApportionment.apportion(new BigDecimal("100.00"), List.of(
                new Share(third, BigDecimal.ONE),
                new Share(first, BigDecimal.ONE),
                new Share(second, BigDecimal.ONE)));

        Map<EntityId, BigDecimal> byUnit = allocations.stream()
                .collect(Collectors.toMap(Allocation::unitId, Allocation::amount));
        assertThat(byUnit.get(first)).isEqualByComparingTo("33.34");
        assertThat(byUnit.get(second)).isEqualByComparingTo("33.33");
        assertThat(byUnit.get(third)).isEqualByComparingTo("33.33");
    }

    @Test
    void total_shares_must_be_positive() {
        assertThatThrownBy(() -> SharesApportionment.apportion(new BigDecimal("100.00"),
                List.of(new Share(EntityId.newId(), BigDecimal.ZERO))))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
