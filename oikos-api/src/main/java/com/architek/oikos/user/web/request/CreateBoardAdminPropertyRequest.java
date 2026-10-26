package com.architek.oikos.user.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.architek.oikos.user.application.port.out.BoardSeat;

/** Step 2 of the volunteer-syndic wizard - see CreateBoardAdminPropertyService. */
public record CreateBoardAdminPropertyRequest(
        @NotBlank @Size(max = 100) String propertyName,
        @NotBlank @Size(max = 250) String propertyAddress,
        @Size(max = 100) String propertyCity,
        @NotNull BoardSeat boardRole) {
}
