package com.architek.oikos.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces architecture rule 1 (docs/ARCHITECTURE.md): domain depends only on the
 * JDK and other domain code, and the web -&gt; application -&gt; domain &lt;- infrastructure
 * dependency direction is respected. Lombok's SOURCE-retention annotations leave no
 * trace in bytecode, so the Lombok-in-domain/application ban is checked separately
 * by LombokUsageSourceTest via a source scan.
 */
@AnalyzeClasses(packages = "com.architek.oikos", importOptions = ImportOption.DoNotIncludeTests.class)
class DependencyRulesArchTest {

    @ArchTest
    static final ArchRule domain_must_not_depend_on_forbidden_frameworks = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "tools.jackson..", "com.fasterxml.jackson..", "lombok..")
            .because("domain must depend only on the JDK and other domain code (rule 1)");

    @ArchTest
    static final ArchRule domain_and_application_must_not_depend_on_web_or_infrastructure = noClasses()
            .that().resideInAnyPackage("..domain..", "..application..")
            .should().dependOnClassesThat().resideInAnyPackage("..web..", "..infrastructure..")
            .because("dependencies must flow web -> application -> domain <- infrastructure (rule 1)");

    /**
     * accounting reads property/installment data only through their public port-in
     * use cases, DTOs, id value objects and exceptions (see
     * AccountingPropertyDirectoryAdapter/AccountingUnitDirectoryAdapter) - never
     * their domain model, repository or infrastructure internals directly (rule
     * 4/6). Scoped to accounting only (the newest module), mirroring the same
     * convention installment already follows for property.
     */
    @ArchTest
    static final ArchRule accounting_must_not_depend_on_other_modules_internals = noClasses()
            .that().resideInAPackage("com.architek.oikos.accounting..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.architek.oikos.property.domain.model..", "com.architek.oikos.property.domain.repository..",
                    "com.architek.oikos.property.infrastructure..",
                    "com.architek.oikos.installment.domain.model..", "com.architek.oikos.installment.domain.repository..",
                    "com.architek.oikos.installment.infrastructure..")
            .because("cross-feature access must go through a port-in use case, never another module's domain model/repository/infrastructure directly (rule 4/6)");

}
