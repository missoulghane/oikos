package com.architek.oikos.property.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.property.application.dto.UnitTypePriceView;
import com.architek.oikos.property.application.port.in.ListUnitTypePricesByPropertyUseCase;
import com.architek.oikos.property.application.port.in.RemoveUnitTypePriceUseCase;
import com.architek.oikos.property.application.port.in.SetUnitTypePriceUseCase;
import com.architek.oikos.property.domain.valueobject.Price;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.domain.valueobject.UnitTypePricingId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = UnitTypePricingController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class UnitTypePricingControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private SetUnitTypePriceUseCase setUnitTypePriceUseCase;

    @MockitoBean
    private ListUnitTypePricesByPropertyUseCase listUnitTypePricesByPropertyUseCase;

    @MockitoBean
    private RemoveUnitTypePriceUseCase removeUnitTypePriceUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/properties/" + PropertyId.newId() + "/unit-type-prices"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_can_list_unit_type_prices_of_a_property() throws Exception {
        when(listUnitTypePricesByPropertyUseCase.listUnitTypePrices(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/properties/" + PropertyId.newId() + "/unit-type-prices")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_set_a_unit_type_price() throws Exception {
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        when(setUnitTypePriceUseCase.set(any())).thenReturn(new UnitTypePriceView(
                UnitTypePricingId.newId(), propertyId, unitTypeId, "Appartement", Price.of(new BigDecimal("300.00"))));

        mockMvc.perform(put("/api/v1/properties/" + propertyId + "/unit-type-prices/" + unitTypeId)
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"price":300.00}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_remove_a_unit_type_price() throws Exception {
        mockMvc.perform(delete("/api/v1/properties/" + PropertyId.newId() + "/unit-type-prices/"
                        + UnitTypeDefinitionId.newId())
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isNoContent());
    }
}
