package com.architek.oikos.user.application.port.out;

/**
 * User's own view of the property to provision for a newly registered
 * property manager, decoupled from the property feature's own command types
 * (rule 6: cross-feature access only through ports).
 */
public record PropertyProvisioningDetails(String name, String address, String city) {
}
