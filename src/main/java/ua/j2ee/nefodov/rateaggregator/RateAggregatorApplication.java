package ua.j2ee.nefodov.rateaggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = {"ua.j2ee.nefodov.rateaggregator.controller",
        "ua.j2ee.nefodov.rateaggregator.service",
        "ua.j2ee.nefodov.rateaggregator.model"})
@EnableAsync
@EnableCaching
public class RateAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(RateAggregatorApplication.class, args);
    }

}
