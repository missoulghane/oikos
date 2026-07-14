package com.architek.oikos.user.application.query;

import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.user.domain.valueobject.UserSearchCriteria;

public record ListUsersQuery(PageRequest pageRequest, UserSearchCriteria criteria) {
}
