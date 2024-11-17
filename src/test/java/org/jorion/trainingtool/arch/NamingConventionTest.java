package org.jorion.trainingtool.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "org.jorion.trainingtool")
class NamingConventionTest {

    @ArchTest
    void classesAnnotatedWithController(JavaClasses javaClasses) {

        classes().that().areAnnotatedWith(Controller.class)
                .should().haveSimpleNameEndingWith("Controller")
                .check(javaClasses);
    }

    @ArchTest
    void classesAnnotatedWithService(JavaClasses javaClasses) {

        classes().that().areAnnotatedWith(Service.class)
                .should().haveSimpleNameEndingWith("Service")
                .check(javaClasses);
    }


}
