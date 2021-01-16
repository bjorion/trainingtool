package org.jorion.trainingtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot Entry Point for Training Tool App.
 * <p>
 * The {@code $TRAININGTOOL_HOME/trainingtool.properties} is a file that contains properties than will override those in
 * application.properties. It is useful when this application runs on an externat Tomcat.
 * <p>
 * If you run Tomcat as a Service, consult the README file.
 */
// @formatter:off
@SpringBootApplication
@EnableJpaRepositories(basePackages = "org.jorion.trainingtool.repositories")
@EntityScan(basePackages = "org.jorion.trainingtool.entities")
@PropertySources({ 
    @PropertySource("classpath:META-INF/build-info.properties"),
    @PropertySource(value = "file:${TRAININGTOOL_HOME}/trainingtool.properties", ignoreResourceNotFound = true) 
})
//@formatter:on
public class TrainingToolApp
{
    // --- Constants ---
    private static final Logger LOG = LoggerFactory.getLogger(TrainingToolApp.class);

    // --- Methods ---
    /**
     * The application main class. It will be called by Spring Boot.
     */
    public static void main(String[] args)
    {
        LOG.debug("Start the application with arg {}", new Object[] { args });
        SpringApplication.run(TrainingToolApp.class, args);
    }

}
