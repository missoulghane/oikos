package com.architek.oikos.invitation.application.port.out;

/**
 * The label of a board seat as every screen shows it (board_role catalog,
 * owned by property) - so the invitation email names the seat with the same
 * words as "Ajouter un membre".
 */
public interface BoardRoleLabelPort {

    /** Falls back to the code itself when the catalog has no row for it. */
    String labelOf(String boardRoleCode);
}
