package com.architek.oikos.user.web.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyAccountRequest(@NotBlank String token) {
}
