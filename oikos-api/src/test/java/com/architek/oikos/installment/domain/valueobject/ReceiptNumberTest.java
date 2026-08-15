package com.architek.oikos.installment.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ReceiptNumberTest {

    @Test
    void formats_padded_to_four_digits() {
        assertThat(new ReceiptNumber(2026, 42).format()).isEqualTo("REC-2026-0042");
        assertThat(new ReceiptNumber(2026, 1).format()).isEqualTo("REC-2026-0001");
    }

    @Test
    void keeps_counting_past_the_padding_width_rather_than_wrapping() {
        assertThat(new ReceiptNumber(2026, 12345).format()).isEqualTo("REC-2026-12345");
    }

    @Test
    void round_trips_through_parse() {
        ReceiptNumber number = new ReceiptNumber(2026, 42);

        assertThat(ReceiptNumber.parse(number.format())).isEqualTo(number);
    }

    @Test
    void rejects_a_value_that_is_not_a_receipt_number() {
        assertThatThrownBy(() -> ReceiptNumber.parse("INV-2026-0042")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ReceiptNumber.parse("REC-2026")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_a_sequence_that_never_started() {
        assertThatThrownBy(() -> new ReceiptNumber(2026, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
