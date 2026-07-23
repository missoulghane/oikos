package com.architek.oikos.party.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

class PartyTest {

    private static Party newParty() {
        return Party.create(PartyId.newId(), "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), "0600000000");
    }

    @Test
    void create_builds_a_party_with_the_given_fields() {
        Party party = newParty();

        assertThat(party.getFullName()).isEqualTo("Jane Doe");
        assertThat(party.getPartyType()).isEqualTo(PartyType.INDIVIDUAL);
        assertThat(party.getEmail()).isEqualTo(EmailVO.of("jane@doe.com"));
        assertThat(party.getPhone()).isEqualTo("0600000000");
    }

    @Test
    void create_allows_a_null_phone() {
        Party party = Party.create(PartyId.newId(), "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), null);

        assertThat(party.getPhone()).isNull();
    }

    @Test
    void create_rejects_a_blank_full_name() {
        assertThatThrownBy(() -> Party.create(PartyId.newId(), " ", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void withPartyInfo_returns_a_new_instance_with_updated_fields_and_same_identity() {
        Party party = newParty();

        Party updated = party.withPartyInfo("Janet Smith", PartyType.COMPANY, EmailVO.of("janet@smith.com"), "0700000000");

        assertThat(updated.getFullName()).isEqualTo("Janet Smith");
        assertThat(updated.getPartyType()).isEqualTo(PartyType.COMPANY);
        assertThat(updated.getEmail()).isEqualTo(EmailVO.of("janet@smith.com"));
        assertThat(updated.getPhone()).isEqualTo("0700000000");
        assertThat(updated).isEqualTo(party);
    }

    @Test
    void equality_is_based_on_identity_not_on_field_values() {
        PartyId id = PartyId.newId();
        Party a = Party.create(id, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), null);
        Party b = Party.create(id, "Janet Smith", PartyType.COMPANY, EmailVO.of("janet@smith.com"), "0700000000");

        assertThat(a).isEqualTo(b);
    }
}
