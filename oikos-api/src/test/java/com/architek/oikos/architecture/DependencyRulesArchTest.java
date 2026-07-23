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

    @ArchTest
    static final ArchRule accounting_must_not_depend_on_installments_domain_model_repositories_or_infrastructure = noClasses()
            .that().resideInAPackage("com.architek.oikos.accounting..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.architek.oikos.installment.domain.model..",
                    "com.architek.oikos.installment.domain.repository..",
                    "com.architek.oikos.installment.infrastructure..")
            .because("accounting reaches installment only through its port-in use cases and DTOs (application.port.in/dto/command/query), never its domain model, repositories, or infrastructure directly");

    @ArchTest
    static final ArchRule installment_must_not_depend_on_accountings_domain_model_repositories_or_infrastructure = noClasses()
            .that().resideInAPackage("com.architek.oikos.installment..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.architek.oikos.accounting.domain.model..",
                    "com.architek.oikos.accounting.domain.repository..",
                    "com.architek.oikos.accounting.infrastructure..")
            .because("installment reaches accounting only through its port-in use cases and DTOs (application.port.in/dto/command/query), never its domain model, repositories, or infrastructure directly");

    @ArchTest
    static final ArchRule property_must_not_depend_on_accountings_domain_model_repositories_or_infrastructure = noClasses()
            .that().resideInAPackage("com.architek.oikos.property..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.architek.oikos.accounting.domain.model..",
                    "com.architek.oikos.accounting.domain.repository..",
                    "com.architek.oikos.accounting.infrastructure..")
            .because("property reaches accounting only through its port-in use cases and DTOs (application.port.in/dto/command/query), never its domain model, repositories, or infrastructure directly");

    @ArchTest
    static final ArchRule accounting_must_not_depend_on_propertys_domain_model_repositories_or_infrastructure = noClasses()
            .that().resideInAPackage("com.architek.oikos.accounting..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.architek.oikos.property.domain.model..",
                    "com.architek.oikos.property.domain.repository..",
                    "com.architek.oikos.property.infrastructure..")
            .because("accounting reaches property only through its port-in use cases and DTOs (application.port.in/dto/command/query), never its domain model, repositories, or infrastructure directly");
}
