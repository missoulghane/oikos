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

}
