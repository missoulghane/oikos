package com.architek.oikos.property.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssignPropertyManagerRequest(@NotBlank @Email @Size(max = 150) String email) {
}
