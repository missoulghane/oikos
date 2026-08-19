package com.architek.oikos.shared.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PhoneNumberVOTest {

    @Test
    void strips_separators_and_the_leading_plus() {
        assertThat(PhoneNumberVO.of("+212 6-12.34 56 78").value()).isEqualTo("212612345678");
    }

    @Test
    void strips_the_leading_double_zero_too() {
        assertThat(PhoneNumberVO.of("00212612345678").value()).isEqualTo("212612345678");
    }

    @Test
    void two_numbers_written_differently_are_equal() {
        assertThat(PhoneNumberVO.of("+212 612 345 678")).isEqualTo(PhoneNumberVO.of("00212612345678"));
    }

    @Test
    void rejects_a_national_number_that_no_country_code_identifies() {
        assertThatThrownBy(() -> PhoneNumberVO.of("0612345678")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_a_number_that_is_not_only_digits() {
        assertThatThrownBy(() -> PhoneNumberVO.of("+212 61 ABC 678")).isInstanceOf(IllegalArgumentException.class);
    }
}
