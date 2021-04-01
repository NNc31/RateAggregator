package ua.j2ee.nefodov.rateaggregator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.j2ee.nefodov.rateaggregator.model.Rate;
import ua.j2ee.nefodov.rateaggregator.service.FileCreatorService;
import ua.j2ee.nefodov.rateaggregator.service.RateService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequestMapping(path = "/api")
public class AggregatorController {

    private static final Logger logger = LoggerFactory.getLogger(AggregatorController.class);

    @Autowired
    private List<RateService> services;

    @Autowired
    private FileCreatorService fileCreator;

    @GetMapping(path = "/rate")
    @Cacheable("rates")
    public Callable<ResponseEntity<?>> getExchangeRate (
            @RequestParam(name = "date") LocalDate date,
            @RequestParam(name = "code") Currency currency,
            @RequestParam(name = "xml", required = false) String xml,
            @RequestParam(name = "file", required = false) String file) {

        logger.info("Rate request on " + date.format(DateTimeFormatter.ISO_DATE) +
                " for " + currency.getCurrencyCode());
        List<Future<Rate>> futures = new ArrayList<>();
        for (RateService service : services) {
            futures.add(service.getRate(date, currency));
        }

        return () -> {
            List<Rate> rates = new ArrayList<>();
            for (Future<Rate> future : futures) {
                if (future.get() != null) rates.add(future.get());
            }

            logger.debug("Sending response");
            if(file != null) {
                logger.info("Saving to file");
                byte[] bytes = fileCreator.fileToBytes(fileCreator.createFile(rates));
                return ResponseEntity.ok().headers(getHeaders(xml, file, currency)).body(bytes);
            } else {
                return ResponseEntity.ok().headers(getHeaders(xml, null, currency)).body(rates);
            }
        };
    }

    @GetMapping(path = "/bestrate")
    @Cacheable("bestRates")
    public Callable<ResponseEntity<?>> getBestRate(
            @RequestParam(name = "start") LocalDate startDate,
            @RequestParam(name = "end") LocalDate endDate,
            @RequestParam(name = "code") Currency currency,
            @RequestParam(name = "xml", required = false) String xml,
            @RequestParam(name = "file", required = false) String file) {

        logger.info("Best rate request from " + startDate + " to" + endDate + " for " + currency.getCurrencyCode());

        List<Future<Rate>> futures = new ArrayList<>(services.size());
        for (RateService service : services) {
            futures.add(service.getBestRateOnPeriod(startDate, endDate, currency));
        }

        return () -> {
            List<Rate> bestRates = new ArrayList<>();
            for (Future<Rate> future : futures) {
                try {
                    bestRates.add(future.get());
                } catch (ExecutionException e) {
                    logger.info("Execution exception with cause: " + e.getCause().toString());
                }

            }

            logger.debug("Sending response");
            if(file != null) {
                logger.info("Saving to file");

                byte[] bytes = fileCreator.fileToBytes(fileCreator.createFile(bestRates));
                return ResponseEntity.ok().headers(getHeaders(xml, file, currency)).body(bytes);
            } else {
                return ResponseEntity.ok().headers(getHeaders(xml, null, currency)).body(bestRates);
            }
        };
    }

    private HttpHeaders getHeaders(String xml, String file, Currency currency) {
        logger.debug("Setting response type");
        HttpHeaders headers = new HttpHeaders();
        if (file != null) {
            headers.add("Content-Disposition", "attachment; filename=" + currency.getCurrencyCode() + "_rate.docx");
        } else if (xml != null) {
            headers.setContentType(MediaType.APPLICATION_XML);
        } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return headers;
    }
}
