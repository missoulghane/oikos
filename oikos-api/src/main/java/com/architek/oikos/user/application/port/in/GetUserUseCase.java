package com.architek.oikos.user.application.port.in;

import java.util.Optional;

import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.application.query.GetUserQuery;

public interface GetUserUseCase {

    UserView getUser(GetUserQuery query);

    /**
     * La même lecture, pour les appelants que l'absence de compte n'arrête pas
     * (un compte supprimé dont il reste une trace ailleurs). Elle existe parce
     * qu'un appelant ne peut pas rattraper {@link #getUser}'s
     * UserNotFoundException : la lever franchit le proxy transactionnel de
     * GetUserService, qui marque au passage la transaction rollback-only, et le
     * commit de l'appelant échoue alors quel que soit son catch.
     */
    Optional<UserView> findUser(GetUserQuery query);
}
