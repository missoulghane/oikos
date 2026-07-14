package com.architek.oikos.shared.application.port.out;

import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

/**
 * Outbound port hiding the concrete password hashing algorithm from the domain/application layers.
 */
public interface PasswordEncoderPort {

    HashedPassword encode(RawPassword rawPassword);

    boolean matches(RawPassword rawPassword, HashedPassword hashedPassword);
}
