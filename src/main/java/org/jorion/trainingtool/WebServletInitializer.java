package org.jorion.trainingtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.WebApplicationInitializer;

/**
 * Extends {@link SpringBootServletInitializer} that implements {@link WebApplicationInitializer}. This class is called
 * by the server (Tomcat) when the application is initialized. Its goal is to invoke {@link TrainingToolApp}.
 */
public class WebServletInitializer extends SpringBootServletInitializer
{
    // --- Constants ---
    private static final Logger LOG = LoggerFactory.getLogger(WebServletInitializer.class);

    // --- Methods ---
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
    {
        LOG.info("Start the application as a web archive");
        return application.sources(TrainingToolApp.class);
    }
}
