package ua.j2ee.nefodov.rateaggregator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ua.j2ee.nefodov.rateaggregator.model.NbuDto;
import ua.j2ee.nefodov.rateaggregator.model.Rate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
public class NbuService implements RateService {

    private static final Logger logger = LoggerFactory.getLogger(NbuService.class);

    private static final String STRING_URL = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    private static final DateTimeFormatter NBU_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final LocalDate MIN_DATE = LocalDate.of(1996, 1, 6);

    @Async
    @Override
    public Future<Rate> getRate(LocalDate date, Currency currency) {
        Rate rate = new Rate("National bank of Ukraine",
                date.format(DateTimeFormatter.ISO_DATE), currency.getCurrencyCode());

        if (validateDate(date)) {
            RestTemplate restTemplate = new RestTemplate();
            NbuDto[] response = restTemplate.getForObject(STRING_URL +
                    "&date=" + date.format(NBU_FORMAT) +
                    "&valcode=" + currency.getCurrencyCode(), NbuDto[].class);

            if (response == null) {
                logger.warn("Unexpected response from NBU: null response for date " +
                        date.format(DateTimeFormatter.ISO_DATE) +
                        " and currency code " + currency.getCurrencyCode());
                rate.setSellRate(0);
                rate.setPurchaseRate(0);
            } else if (response.length == 1) {
                NbuDto nbuDto = response[0];
                rate.setSellRate(nbuDto.getRate());
                rate.setPurchaseRate(nbuDto.getRate());
            } else {
                logger.warn("Incorrect response type for date " + date +
                        " and currency" + currency.getCurrencyCode());
                rate.setSellRate(0);
                rate.setPurchaseRate(0);
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

        long days = ChronoUnit.DAYS.between(start, end);

        if (days > 0 && days < 31 && validateDate(start) && validateDate(end) && !start.isAfter(end)) {
            List<Future<Rate>> futures = new ArrayList<>((int) days);
            while (!start.isAfter(end)) {
                Future<Rate> future = getRate(start, currency);
                futures.add(future);
                start = start.plusDays(1);
            }

            double maxSell = 0, maxBuy = 0;
            LocalDate sellDate = start, buyDate = start;

            for (Future<Rate> future : futures) {
                Rate rate;
                try {
                    rate = future.get();
                    if (maxSell == 0 || maxSell < rate.getSellRate()) {
                        maxSell = rate.getSellRate();
                        sellDate = LocalDate.parse(rate.getDate(), DateTimeFormatter.ISO_DATE);
                    }

                    if (maxBuy == 0 || maxBuy > rate.getPurchaseRate()) {
                        maxBuy = rate.getPurchaseRate();
                        buyDate = LocalDate.parse(rate.getDate(), DateTimeFormatter.ISO_DATE);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    logger.warn("Exception in rates on period in NBU service");
                    return new AsyncResult<>(new Rate("National Bank of Ukraine", "No date",
                            currency.getCurrencyCode(), 0, 0));
                }
            }

            Rate bestRate = new Rate("National Bank of Ukraine",
                    "sell date: " + sellDate + ", purchase date: " + buyDate, currency.getCurrencyCode(),
                    maxSell, maxBuy);
            return new AsyncResult<>(bestRate);
        } else {
            throw new IllegalArgumentException("Invalid period");
        }
    }

    private boolean validateDate(LocalDate date) {
        return date.isAfter(MIN_DATE) && !date.isAfter(LocalDate.now());
    }
}
