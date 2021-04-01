package ua.j2ee.nefodov.rateaggregator.service;

import org.springframework.scheduling.annotation.Async;
import ua.j2ee.nefodov.rateaggregator.model.Rate;

import java.time.LocalDate;
import java.util.Currency;
import java.util.concurrent.Future;

public interface RateService {

    @Async
    Future<Rate> getRate(LocalDate date, Currency currency);

    @Async
    Future<Rate> getBestRateOnPeriod(LocalDate start, LocalDate end, Currency currency);
}
