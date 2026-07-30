package com.architek.oikos.accounting.web.request;

import java.util.List;

/** null or empty unitIds means: every unit of the property with something pending. */
public record ValidateBulkLettrageRequest(List<String> unitIds) {
}
