package com.architek.oikos.party.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.party.domain.valueobject.PartySearchCriteria;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.party.infrastructure.mapper.PartyPersistenceMapperImpl;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PartyRepositoryAdapter.class, PartyPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class PartyRepositoryAdapterDataJpaTest {

    @Autowired
    private PartyRepositoryAdapter adapter;

    private final EntityId propertyId = EntityId.newId();

    private Party newParty() {
        return Party.create(PartyId.newId(), propertyId, "Jane Doe", PartyType.INDIVIDUAL,
                EmailVO.of("jpa-test@oikos.com"), "0600000000");
    }

    @Test
    void saves_and_finds_a_party_by_id() {
        Party party = newParty();

        adapter.save(party);

        assertThat(adapter.findById(party.getId())).isPresent()
                .get().extracting(Party::getFullName).isEqualTo("Jane Doe");
    }

    @Test
    void existsByPropertyIdAndEmail_reflects_persisted_state() {
        assertThat(adapter.existsByPropertyIdAndEmail(propertyId, EmailVO.of("nobody@oikos.com"))).isFalse();

        adapter.save(Party.create(PartyId.newId(), propertyId, "A B", PartyType.INDIVIDUAL,
                EmailVO.of("nobody@oikos.com"), null));

        assertThat(adapter.existsByPropertyIdAndEmail(propertyId, EmailVO.of("nobody@oikos.com"))).isTrue();
    }

    @Test
    void existsByPropertyIdAndPhone_reflects_persisted_state() {
        assertThat(adapter.existsByPropertyIdAndPhone(propertyId, "0611223344")).isFalse();

        adapter.save(Party.create(PartyId.newId(), propertyId, "A B", PartyType.INDIVIDUAL,
                EmailVO.of("phone-test@oikos.com"), "0611223344"));

        assertThat(adapter.existsByPropertyIdAndPhone(propertyId, "0611223344")).isTrue();
    }

    @Test
    void findByPropertyIdAndEmail_returns_the_matching_party() {
        Party party = newParty();
        adapter.save(party);

        assertThat(adapter.findByPropertyIdAndEmail(propertyId, EmailVO.of("jpa-test@oikos.com"))).isPresent()
                .get().extracting(Party::getId).isEqualTo(party.getId());
        assertThat(adapter.findByPropertyIdAndEmail(propertyId, EmailVO.of("nobody@oikos.com"))).isEmpty();
    }

    @Test
    void findByPropertyIdAndPhone_returns_the_matching_party() {
        Party party = newParty();
        adapter.save(party);

        assertThat(adapter.findByPropertyIdAndPhone(propertyId, "0600000000")).isPresent()
                .get().extracting(Party::getId).isEqualTo(party.getId());
        assertThat(adapter.findByPropertyIdAndPhone(propertyId, "0000000000")).isEmpty();
    }

    @Test
    void updating_a_party_persists_changes_without_creating_a_duplicate() {
        Party party = newParty();
        adapter.save(party);

        adapter.save(party.withPartyInfo("Janet Smith", PartyType.COMPANY, party.getEmail(), "0700000000"));

        Party reloaded = adapter.findById(party.getId()).orElseThrow();
        assertThat(reloaded.getFullName()).isEqualTo("Janet Smith");
        assertThat(reloaded.getPartyType()).isEqualTo(PartyType.COMPANY);
        assertThat(reloaded.getPhone()).isEqualTo("0700000000");
    }

    @Test
    void findAll_paginates_results() {
        for (int i = 0; i < 3; i++) {
            adapter.save(Party.create(PartyId.newId(), propertyId, "Doe " + i, PartyType.INDIVIDUAL,
                    EmailVO.of("user" + i + "@oikos.com"), null));
        }

        var page = adapter.findAll(PageRequest.of(0, 2), PartySearchCriteria.of(propertyId));

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(3);
    }

    @Test
    void findAll_filters_by_search_text() {
        adapter.save(Party.create(PartyId.newId(), propertyId, "Alice Martin", PartyType.INDIVIDUAL,
                EmailVO.of("alice@oikos.com"), null));
        adapter.save(Party.create(PartyId.newId(), propertyId, "Bob Durand", PartyType.INDIVIDUAL,
                EmailVO.of("bob@oikos.com"), null));

        var page = adapter.findAll(PageRequest.of(0, 20), new PartySearchCriteria(propertyId, "alice"));

        assertThat(page.content()).extracting(Party::getFullName).containsExactly("Alice Martin");
    }

    @Test
    void deleteById_removes_the_party() {
        Party party = newParty();
        adapter.save(party);

        adapter.deleteById(party.getId());

        assertThat(adapter.findById(party.getId())).isEmpty();
    }
}
