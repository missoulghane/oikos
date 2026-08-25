package com.architek.oikos.invitation.application.port.in;

import com.architek.oikos.invitation.application.command.EnableInvitationCommand;

/**
 * Le pendant de DisableInvitationUseCase, et la seule façon de remettre en
 * service le lien public d'une copropriété : il ne se recrée jamais (voir
 * PublicInvitationAlreadyExistsException).
 */
public interface EnableInvitationUseCase {

    void enable(EnableInvitationCommand command);
}
