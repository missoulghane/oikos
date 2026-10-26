package com.architek.oikos.user.application.command;

import com.architek.oikos.user.application.port.out.BoardSeat;
import com.architek.oikos.user.domain.valueobject.UserId;

/** Step 2 of the volunteer-syndic wizard: the property its account administers. */
public record CreateBoardAdminPropertyCommand(UserId userId, String propertyName, String propertyAddress,
                                               String propertyCity, BoardSeat boardSeat) {
}
