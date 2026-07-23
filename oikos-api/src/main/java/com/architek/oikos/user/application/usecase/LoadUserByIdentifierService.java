package com.architek.oikos.user.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.in.LoadUserByIdentifierUseCase;
import com.architek.oikos.user.application.port.out.PartyDirectoryPort;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

/**
 * Resolves a login identifier in a fixed priority order: 1) the account's own login,
 * 2) the linked party's email, 3) the linked party's phone. The identifier is not
 * validated as any particular format upfront - each step simply fails to match rather
 * than erroring, except the email step, which is skipped if the identifier is not a
 * syntactically valid email (a raw string like a phone or login could otherwise never
 * reach EmailVO.of unharmed).
 */
@Component
public class LoadUserByIdentifierService implements LoadUserByIdentifierUseCase {

    private final UserRepository userRepository;
    private final PartyDirectoryPort partyDirectoryPort;

    public LoadUserByIdentifierService(UserRepository userRepository, PartyDirectoryPort partyDirectoryPort) {
        this.userRepository = userRepository;
        this.partyDirectoryPort = partyDirectoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> loadByIdentifier(String identifier) {
        Optional<User> byLogin = userRepository.findByLogin(identifier);
        if (byLogin.isPresent()) {
            return byLogin;
        }

        Optional<EntityId> partyId = tryParseEmail(identifier)
                .flatMap(partyDirectoryPort::findIdByEmail)
                .or(() -> partyDirectoryPort.findIdByPhone(identifier));

        return partyId.flatMap(userRepository::findByPartyId);
    }

    private static Optional<EmailVO> tryParseEmail(String identifier) {
        try {
            return Optional.of(EmailVO.of(identifier));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
