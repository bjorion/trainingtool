package org.jorion.trainingtool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.WebApplicationInitializer;

/**
 * Extends {@link SpringBootServletInitializer} that implements {@link WebApplicationInitializer}. This class is called
 * by the server (Tomcat) when the application is initialized. Its goal is to invoke {@link TrainingToolApp}.
 */
@Slf4j
public class WebServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {

        log.info("Start the application as a web archive");
        return application.sources(TrainingToolApp.class);
    }
}
