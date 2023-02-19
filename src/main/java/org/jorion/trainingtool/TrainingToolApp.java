package org.jorion.trainingtool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot Entry Point for Training Tool App.
 * <p>
 * The optional {@code $TRAININGTOOL_HOME/trainingtool.properties} is a file that contains properties than will override those in
 * application.properties. It is useful when this application runs on an external Tomcat.
 * <p>
 * If you run Tomcat as a Service, consult the README file.
 */
@Slf4j
@SpringBootApplication
@EnableJpaRepositories(basePackageClasses = org.jorion.trainingtool.repository.INoOpRepository.class)
@EntityScan(basePackageClasses = org.jorion.trainingtool.entity.NoOp.class)
@PropertySources({
        @PropertySource("classpath:META-INF/build-info.properties"),
        @PropertySource(value = "file:${TRAININGTOOL_HOME}/trainingtool.properties", ignoreResourceNotFound = true)
})
public class TrainingToolApp {

    public static void main(String[] args) {

        log.debug("Start the application with arg {}", new Object[]{args});
        SpringApplication.run(TrainingToolApp.class, args);
    }

}
