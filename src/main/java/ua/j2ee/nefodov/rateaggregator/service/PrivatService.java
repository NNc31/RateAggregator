package ua.j2ee.nefodov.rateaggregator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import ua.j2ee.nefodov.rateaggregator.model.PrivatDto;
import ua.j2ee.nefodov.rateaggregator.model.Rate;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.concurrent.Future;

@Service
public class PrivatService implements RateService {

    private static final Logger logger = LoggerFactory.getLogger(PrivatService.class);

    private static final String STRING_URL = "https://api.privatbank.ua/p24api/exchange_rates?json&date=";
    private static final DateTimeFormatter PRIVAT_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final LocalDate MIN_DATE = LocalDate.of(2012, 1, 30);

    @Async
    @Override
    public Future<Rate> getRate(LocalDate date, Currency currency) {
        PrivatDto privatDto;
        Rate rate = new Rate("PrivatBank",
                date.format(DateTimeFormatter.ISO_DATE), currency.getCurrencyCode());

        if (validateDate(date)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                privatDto = mapper.readValue(new URL(STRING_URL + date.format(PRIVAT_FORMAT)), PrivatDto.class);
            } catch (IOException e) {
                logger.warn("IOException for date " + date);
                privatDto = null;
            }

            if (privatDto == null || privatDto.getExchangeRate().isEmpty()) {
                rate.setSellRate(0);
                rate.setPurchaseRate(0);
            } else {
                PrivatDto.PrivatRate privatRate = privatDto.getExchangeRate().stream().
                        filter(currentRate -> currency.getCurrencyCode().equals(currentRate.getCurrency())).
                        findAny().orElse(null);
                if (privatRate == null) {
                    rate.setSellRate(0);
                    rate.setPurchaseRate(0);
                } else {
                    if (privatRate.getSaleRate() != 0) rate.setSellRate(privatRate.getSaleRate());
                    else rate.setSellRate(privatRate.getSaleRateNB());

                    if (privatRate.getPurchaseRate() != 0) rate.setPurchaseRate(privatRate.getPurchaseRate());
                    else rate.setPurchaseRate(privatRate.getPurchaseRateNB());
                }
            }
        } else {
            logger.info("Incorrect date");
            rate.setSellRate(0);
            rate.setPurchaseRate(0);
        }

        return new AsyncResult<>(rate);
    }

    @Async
    public Future<Rate> getBestRateOnPeriod(LocalDate start, LocalDate end, Currency currency) {
        throw new UnsupportedOperationException();
    }

    private boolean validateDate(LocalDate date) {
        return date.isAfter(MIN_DATE) && !date.isAfter(LocalDate.now());
    }
}
