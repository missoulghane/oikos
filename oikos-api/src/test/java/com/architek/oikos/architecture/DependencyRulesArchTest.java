package com.architek.oikos.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
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

    /**
     * messaging reads property/party/user data only through their public port-in
     * use cases, DTOs, id value objects and exceptions (see
     * MessagingPropertyMemberDirectoryAdapter/MessagingUserAccessAdapter/
     * MessagingPartyAccountDirectoryAdapter) - never their domain aggregates,
     * repository or infrastructure internals directly (rule 4/6). Scoped to
     * messaging only, same convention already followed by accounting for its
     * own cross-module dependencies.
     * user.domain.model.Permission is deliberately not in this list: it is the
     * shared RBAC catalog (see docs/ARCHITECTURE.md), already referenced
     * directly cross-module the same way by PropertyAccessEvaluator itself
     * (auth.hasPermission(...)) - narrowing to Permission specifically (rather
     * than excluding user.domain.model.. wholesale) still catches a real leak
     * of User/PropertyRoleGrant/Role.
     */
    /**
     * document reads property data only through its public port-in use cases
     * (GetPropertyUseCase/GetUnitUseCase, see DocumentOwnerExistenceAdapter) -
     * never property's domain model, repository or infrastructure internals
     * directly (rule 4/6), same convention as accounting.
     */
    @ArchTest
    static final ArchRule document_must_not_depend_on_other_modules_internals = noClasses()
            .that().resideInAPackage("com.architek.oikos.document..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.architek.oikos.property.domain.model..", "com.architek.oikos.property.domain.repository..",
                    "com.architek.oikos.property.infrastructure..")
            .because("cross-feature access must go through a port-in use case, never another module's domain model/repository/infrastructure directly (rule 4/6)");

    private static final DescribedPredicate<JavaClass> OTHER_MODULES_INTERNALS_EXCEPT_PERMISSION_CATALOG =
            new DescribedPredicate<>("reside in property/party/user internals, except the shared Permission RBAC catalog") {
                @Override
                public boolean test(JavaClass javaClass) {
                    String packageName = javaClass.getPackageName();
                    boolean isSharedPermissionCatalog = packageName.equals("com.architek.oikos.user.domain.model")
                            && javaClass.getSimpleName().equals("Permission");
                    boolean isRestrictedPackage = packageName.startsWith("com.architek.oikos.property.domain.model")
                            || packageName.startsWith("com.architek.oikos.property.domain.repository")
                            || packageName.startsWith("com.architek.oikos.property.infrastructure")
                            || packageName.startsWith("com.architek.oikos.user.domain.model")
                            || packageName.startsWith("com.architek.oikos.user.domain.repository")
                            || packageName.startsWith("com.architek.oikos.user.infrastructure")
                            || packageName.startsWith("com.architek.oikos.party.domain.model")
                            || packageName.startsWith("com.architek.oikos.party.domain.repository")
                            || packageName.startsWith("com.architek.oikos.party.infrastructure");
                    return isRestrictedPackage && !isSharedPermissionCatalog;
                }
            };

    @ArchTest
    static final ArchRule messaging_must_not_depend_on_other_modules_internals = noClasses()
            .that().resideInAPackage("com.architek.oikos.messaging..")
            .should().dependOnClassesThat(OTHER_MODULES_INTERNALS_EXCEPT_PERMISSION_CATALOG)
            .because("cross-feature access must go through a port-in use case, never another module's domain model/repository/infrastructure directly (rule 4/6) - Permission is the shared RBAC catalog, exempted");

}
