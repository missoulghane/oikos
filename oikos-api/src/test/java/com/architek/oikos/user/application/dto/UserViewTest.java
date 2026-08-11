package com.architek.oikos.user.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.domain.model.PropertyRole;
import com.architek.oikos.user.domain.model.PropertyRoleGrant;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * A caller can hold more than one grant on the same property - e.g. a board
 * member who is also a unit owner (see dev.sql CASE 2/4/6/7) - so
 * UserView.of's collision resolution must always keep the most privileged
 * grant, not whichever one a HashSet happens to iterate last.
 */
class UserViewTest {

    private static User userWithGrants(PropertyRole... rolesOnSameProperty) {
        EntityId propertyId = EntityId.newId();
        Set<PropertyRoleGrant> grants = Arrays.stream(rolesOnSameProperty)
                .map(role -> new PropertyRoleGrant(EntityId.newId(), propertyId, role))
                .collect(Collectors.toSet());
        return User.reconstruct(UserId.newId(), EmailVO.of("jane@doe.com"), "Jane Doe", HashedPassword.of("hashed"),
                Set.of(Role.ROLE_USER), Set.of(), grants, true, true);
    }

    @Test
    void keeps_board_member_over_owner_when_both_are_granted_on_the_same_property() {
        User user = userWithGrants(PropertyRole.PROPERTY_OWNER, PropertyRole.PROPERTY_BOARD_MEMBER);

        UserView view = UserView.of(user);

        assertThat(view.roleByProperty()).containsValue(PropertyRole.PROPERTY_BOARD_MEMBER);
    }

    @Test
    void keeps_board_admin_over_owner_when_both_are_granted_on_the_same_property() {
        User user = userWithGrants(PropertyRole.PROPERTY_OWNER, PropertyRole.PROPERTY_BOARD_ADMIN);

        UserView view = UserView.of(user);

        assertThat(view.roleByProperty()).containsValue(PropertyRole.PROPERTY_BOARD_ADMIN);
    }

    @Test
    void keeps_admin_tier_over_member_tier_when_both_are_granted_on_the_same_property() {
        User user = userWithGrants(PropertyRole.PROPERTY_BOARD_MEMBER, PropertyRole.PROPERTY_BOARD_ADMIN);

        UserView view = UserView.of(user);

        assertThat(view.roleByProperty()).containsValue(PropertyRole.PROPERTY_BOARD_ADMIN);
    }
}
