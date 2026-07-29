package com.architek.oikos.party.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.party.application.query.ListPartiesQuery;
import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.repository.PartyRepository;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.party.domain.valueobject.PartySearchCriteria;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListPartiesServiceTest {

    @Mock
    private PartyRepository partyRepository;

    private ListPartiesService newService() {
        return new ListPartiesService(partyRepository);
    }

    @Test
    void listing_parties_maps_the_repository_page_to_views() {
        EntityId propertyId = EntityId.newId();
        Party party = Party.create(PartyId.newId(), propertyId, "Jane Doe", PartyType.INDIVIDUAL,
                EmailVO.of("jane@doe.com"), null);
        when(partyRepository.findAll(PageRequest.defaultRequest(), PartySearchCriteria.of(propertyId)))
                .thenReturn(Page.of(List.of(party), 0, 20, 1));

        var query = new ListPartiesQuery(PageRequest.defaultRequest(), PartySearchCriteria.of(propertyId));
        var page = newService().listParties(query);

        assertThat(page.content()).extracting(view -> view.fullName()).containsExactly("Jane Doe");
        assertThat(page.totalElements()).isEqualTo(1);
    }
}
