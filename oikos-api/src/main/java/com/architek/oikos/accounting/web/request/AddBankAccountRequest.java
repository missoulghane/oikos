package com.architek.oikos.accounting.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddBankAccountRequest(@NotBlank @Size(max = 200) String label) {
}
