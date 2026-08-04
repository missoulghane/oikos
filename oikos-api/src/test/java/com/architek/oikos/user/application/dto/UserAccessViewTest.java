package com.architek.oikos.user.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.architek.oikos.user.domain.model.PropertyRole;

class UserAccessViewTest {

    private static UserAccessView viewWithRoles(String propertyId, PropertyRole... roles) {
        return new UserAccessView(Set.of(), Map.of(propertyId, Set.of(roles)), Map.of(), Set.of(), Set.of());
    }

    @Test
    void a_bare_property_owner_grant_does_not_manage_the_property() {
        UserAccessView view = viewWithRoles("prop-1", PropertyRole.PROPERTY_OWNER);

        assertThat(view.managesProperty("prop-1")).isFalse();
    }

    @Test
    void a_board_admin_grant_manages_the_property() {
        UserAccessView view = viewWithRoles("prop-1", PropertyRole.PROPERTY_BOARD_ADMIN);

        assertThat(view.managesProperty("prop-1")).isTrue();
    }

    @Test
    void a_manager_member_grant_manages_the_property() {
        UserAccessView view = viewWithRoles("prop-1", PropertyRole.PROPERTY_MANAGER_MEMBER);

        assertThat(view.managesProperty("prop-1")).isTrue();
    }

    @Test
    void holding_both_owner_and_a_staff_role_on_the_same_property_still_manages_it() {
        UserAccessView view = viewWithRoles("prop-1", PropertyRole.PROPERTY_OWNER, PropertyRole.PROPERTY_BOARD_MEMBER);

        assertThat(view.managesProperty("prop-1")).isTrue();
    }

    @Test
    void no_grant_at_all_on_the_property_does_not_manage_it() {
        UserAccessView view = new UserAccessView(Set.of(), Map.of(), Map.of(), Set.of(), Set.of());

        assertThat(view.managesProperty("prop-1")).isFalse();
    }

    @Test
    void a_platform_admin_manages_every_property_regardless_of_grants() {
        UserAccessView view = new UserAccessView(Set.of("ROLE_ADMIN"), Map.of(), Map.of(), Set.of(), Set.of());

        assertThat(view.managesProperty("prop-1")).isTrue();
    }

    @Test
    void owner_grant_on_a_different_property_does_not_leak_into_this_one() {
        UserAccessView view = viewWithRoles("prop-1", PropertyRole.PROPERTY_BOARD_ADMIN);

        assertThat(view.managesProperty("prop-2")).isFalse();
    }
}
