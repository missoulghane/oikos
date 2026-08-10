package com.architek.oikos.user.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.valueobject.UserId;
import com.architek.oikos.user.domain.valueobject.UserSearchCriteria;
import com.architek.oikos.user.infrastructure.mapper.UserPersistenceMapperImpl;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserRepositoryAdapter.class, UserPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class UserRepositoryAdapterDataJpaTest {

    @Autowired
    private UserRepositoryAdapter adapter;

    private static User newUser() {
        return User.register(UserId.newId(), EmailVO.of("jpa-test@oikos.com"), "Jane Doe", HashedPassword.of("hashed"));
    }

    @Test
    void saves_and_finds_a_user_by_id() {
        User user = newUser();

        adapter.save(user);

        assertThat(adapter.findById(user.getId())).isPresent()
                .get().extracting(u -> u.getEmail().value()).isEqualTo("jpa-test@oikos.com");
    }

    @Test
    void findByEmail_and_existsByEmail_reflect_persisted_state() {
        assertThat(adapter.existsByEmail("nobody@oikos.com")).isFalse();

        adapter.save(User.register(UserId.newId(), EmailVO.of("nobody@oikos.com"), "Jane Doe", HashedPassword.of("hashed")));

        assertThat(adapter.existsByEmail("nobody@oikos.com")).isTrue();
        assertThat(adapter.findByEmail("nobody@oikos.com")).isPresent();
    }

    @Test
    void updating_a_user_persists_changes_without_creating_a_duplicate() {
        User user = newUser();
        adapter.save(user);

        adapter.save(user.verify().withFullName("Updated Name"));

        User reloaded = adapter.findById(user.getId()).orElseThrow();
        assertThat(reloaded.isVerified()).isTrue();
        assertThat(reloaded.getFullName()).isEqualTo("Updated Name");
    }

    @Test
    void findAll_paginates_results() {
        for (int i = 0; i < 3; i++) {
            adapter.save(User.register(UserId.newId(), EmailVO.of("user" + i + "@oikos.com"), "Jane Doe",
                    HashedPassword.of("hashed")));
        }

        var page = adapter.findAll(PageRequest.of(0, 2), UserSearchCriteria.empty());

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(3);
    }

    @Test
    void findAll_filters_by_search_text_on_email() {
        adapter.save(User.register(UserId.newId(), EmailVO.of("alice@oikos.com"), "Alice", HashedPassword.of("hashed")));
        adapter.save(User.register(UserId.newId(), EmailVO.of("bob@oikos.com"), "Bob", HashedPassword.of("hashed")));

        var page = adapter.findAll(PageRequest.of(0, 20), new UserSearchCriteria("alice", null, null));

        assertThat(page.content()).extracting(u -> u.getEmail().value()).containsExactly("alice@oikos.com");
    }

    @Test
    void findAll_filters_by_role() {
        adapter.save(newUser());

        var matching = adapter.findAll(PageRequest.of(0, 20), new UserSearchCriteria(null, Role.ROLE_USER, null));
        var nonMatching = adapter.findAll(PageRequest.of(0, 20), new UserSearchCriteria(null, Role.ROLE_ADMIN, null));

        assertThat(matching.totalElements()).isEqualTo(1);
        assertThat(nonMatching.totalElements()).isZero();
    }

    @Test
    void findAll_filters_by_enabled_status() {
        User user = newUser();
        adapter.save(user.deactivate());

        var disabled = adapter.findAll(PageRequest.of(0, 20), new UserSearchCriteria(null, null, false));
        var enabled = adapter.findAll(PageRequest.of(0, 20), new UserSearchCriteria(null, null, true));

        assertThat(disabled.totalElements()).isEqualTo(1);
        assertThat(enabled.totalElements()).isZero();
    }

    @Test
    void findAll_combines_multiple_filters() {
        adapter.save(newUser());

        var page = adapter.findAll(PageRequest.of(0, 20), new UserSearchCriteria("jpa-test", Role.ROLE_USER, true));

        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    void findByLinkedPartyIds_returns_only_users_linked_to_one_of_the_requested_parties() {
        EntityId requestedParty = EntityId.newId();
        User linked = User.register(UserId.newId(), EmailVO.of("linked@oikos.com"), "Linked User", HashedPassword.of("hashed"))
                .withLinkedParty(requestedParty);
        adapter.save(linked);
        adapter.save(User.register(UserId.newId(), EmailVO.of("other@oikos.com"), "Other User", HashedPassword.of("hashed"))
                .withLinkedParty(EntityId.newId()));

        var found = adapter.findByLinkedPartyIds(java.util.List.of(requestedParty));

        assertThat(found).extracting(User::getId).containsExactly(linked.getId());
    }

    @Test
    void deleteById_removes_the_user() {
        User user = newUser();
        adapter.save(user);

        adapter.deleteById(user.getId());

        assertThat(adapter.findById(user.getId())).isEmpty();
    }
}
