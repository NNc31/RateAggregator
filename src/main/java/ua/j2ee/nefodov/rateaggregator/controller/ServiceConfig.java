package ua.j2ee.nefodov.rateaggregator.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.j2ee.nefodov.rateaggregator.service.MonoService;
import ua.j2ee.nefodov.rateaggregator.service.NbuService;
import ua.j2ee.nefodov.rateaggregator.service.PrivatService;
import ua.j2ee.nefodov.rateaggregator.service.RateService;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ServiceConfig {

    @Bean
    public List<RateService> servicesBean() {
        return Arrays.asList(nbuBean(), privatBean(), monoBean());
    }

    @Bean
    public RateService nbuBean() {
        return new NbuService();
    }

    @Bean
    public RateService privatBean() {
        return new PrivatService();
    }

    @Bean
    public RateService monoBean() {
        return new MonoService();
    }
}
