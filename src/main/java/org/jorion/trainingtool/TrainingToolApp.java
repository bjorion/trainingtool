package org.jorion.trainingtool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Slf4j
@SpringBootApplication
@EnableJpaRepositories
@EntityScan
@PropertySources({
        @PropertySource("classpath:META-INF/build-info.properties")
})
public class TrainingToolApp {

    static void main(String[] args) {

        SpringApplication.run(TrainingToolApp.class, args);
    }

}
