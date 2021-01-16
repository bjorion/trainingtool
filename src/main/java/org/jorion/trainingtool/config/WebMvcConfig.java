package org.jorion.trainingtool.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Customize the Java-based configuration for Spring MVC.
 */
@EnableWebMvc
@Configuration
public class WebMvcConfig implements WebMvcConfigurer
{
    // --- Constants ---
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(WebMvcConfig.class);

    // --- Methods ---
    /**
     * {@inheritDoc}
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry)
    {
        registry.addViewController("/done").setViewName("done");
        registry.addViewController("/access-denied").setViewName("access-denied");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Because of the annotation {@code @EnableWebMvc}, we need to manually specify some links.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        // @formatter:off
        registry.addResourceHandler("/styles/**", "/js/**", "/img/**")
                .addResourceLocations("classpath:/static/styles/", "classpath:/static/js/", "classpath:/static/img/");
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("/webjars/");
        // @formatter:on
    }

    /**
     * Specify the message properties file (under src/main/resources).
     */
    @Bean
    @Description("Spring Message Resolver")
    public ResourceBundleMessageSource messageSource()
    {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        return messageSource;
    }

    /**
     * Add formatters for Thymeleaf.
     */
    @Override
    public void addFormatters(FormatterRegistry formatterRegistry)
    {
        // formatterRegistry.addFormatter(...);
    }

}
