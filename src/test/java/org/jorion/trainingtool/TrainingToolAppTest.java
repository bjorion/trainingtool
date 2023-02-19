package org.jorion.trainingtool;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("local")
@SpringBootTest(classes = TrainingToolApp.class)
public class TrainingToolAppTest {

    /**
     * Fail if the Spring configuration does not load correctly for the given active profile.
     */
    @Test
    void contextLoads(ApplicationContext context) {
        assertNotNull(context);
    }
}
