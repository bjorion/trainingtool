package org.jorion.trainingtool;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@ActiveProfiles("local")
@SpringBootTest(classes = TrainingToolApp.class)
public class TrainingToolAppTest {

    /**
     * Fail if the Spring configuration does not load correctly for the given active profile.
     */
    @Test
    void contextLoads(ApplicationContext context) {

        assertNotNull(context);

        var modules = ApplicationModules.of(TrainingToolApp.class);
        new Documenter(modules)
                .writeIndividualModulesAsPlantUml()
                .writeModulesAsPlantUml()
                //.writeModuleCanvases()
        ;
        // modules.verify();
    }

    @SuppressWarnings("unused")
    void listModules() {

        var modules = ApplicationModules.of(TrainingToolApp.class);
        for (var m : modules) {
            log.info("Name [{}], Base Package [{}]", m.getIdentifier(), m.getBasePackage());
        }
    }
}
