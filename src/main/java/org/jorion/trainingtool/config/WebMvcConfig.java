package org.jorion.trainingtool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * Customize the Java-based configuration for Spring MVC.
 */
@EnableWebMvc
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        registry.addViewController("/done").setViewName("done");
        registry.addViewController("/access-denied").setViewName("error/403");
    }

    /**
     * Because of the annotation {@code @EnableWebMvc}, we need to manually specify some links.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        final long ageInDays = 1000;

        registry.addResourceHandler("/styles/**", "/js/**")
                .addResourceLocations("classpath:/static/styles/", "classpath:/static/js/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS));
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/")
                .setCacheControl(CacheControl.maxAge(ageInDays, TimeUnit.DAYS));
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(ageInDays, TimeUnit.DAYS));
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("/webjars/");
    }

    /**
     * Specify the message properties file (under src/main/resources).
     */
    @Bean
    @Description("Spring Message Resolver")
    public ResourceBundleMessageSource messageSource() {

        var messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        return messageSource;
    }

    /**
     * Add formatters for Thymeleaf.
     */
    @Override
    public void addFormatters(FormatterRegistry formatterRegistry) {
        // formatterRegistry.addFormatter(...);
    }

}
