package org.jorion.trainingtool.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import org.junit.jupiter.api.Disabled;

import static com.tngtech.archunit.library.DependencyRules.NO_CLASSES_SHOULD_DEPEND_UPPER_PACKAGES;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "org.jorion.trainingtool")
class CyclicDependenciesTest {

    @Disabled
    void classesShouldBeFreeOfCycles(JavaClasses javaClasses) {

        slices().matching("org.jorion.trainingtool.(*)..")
                .should().beFreeOfCycles()
                .check(javaClasses);
    }

    @ArchTest
    void classesShouldNotDependOnUpperPackages(JavaClasses javaClasses) {

        NO_CLASSES_SHOULD_DEPEND_UPPER_PACKAGES.check(javaClasses);
    }

}
