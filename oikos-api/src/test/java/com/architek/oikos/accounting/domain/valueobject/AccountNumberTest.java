package com.architek.oikos.accounting.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AccountNumberTest {

    @Test
    void must_be_exactly_8_digits() {
        assertThatThrownBy(() -> AccountNumber.of("1234567")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AccountNumber.of("123456789")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AccountNumber.of("3411500X")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void for_sequence_pads_the_increment_to_fill_the_remaining_digits() {
        assertThat(AccountNumber.forSequence("516100", 1).value()).isEqualTo("51610001");
        assertThat(AccountNumber.forSequence("516100", 42).value()).isEqualTo("51610042");
        assertThat(AccountNumber.forSequence("341150", 7).value()).isEqualTo("34115007");
    }

    @Test
    void for_sequence_rejects_an_increment_that_does_not_fit() {
        assertThatThrownBy(() -> AccountNumber.forSequence("516100", 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AccountNumber.forSequence("516100", 100)).isInstanceOf(IllegalArgumentException.class);
    }
}
