package ua.j2ee.nefodov.rateaggregator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.j2ee.nefodov.rateaggregator.model.CommonDto;
import ua.j2ee.nefodov.rateaggregator.model.DateParser;
import ua.j2ee.nefodov.rateaggregator.model.WordCreator;
import ua.j2ee.nefodov.rateaggregator.service.RateService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@RestController
@RequestMapping(path = "/api")
public class AggregatorController {

    private static final Logger logger = LoggerFactory.getLogger(AggregatorController.class);

    @Autowired
    @Qualifier("servicesBean")
    private List<RateService> services;

    @Autowired
    @Qualifier("nbuBean")
    private RateService bestRateService;

    @GetMapping
    public String reference() {
        logger.info("Reference request");
        return REFERENCE;
    }

    @GetMapping(path = "/rate")
    @Cacheable("rates")
    public Callable<ResponseEntity<?>> getExchangeRate (
            @RequestParam(name = "date", defaultValue = "now") String date,
            @RequestParam(name = "code", defaultValue = "USD") String currCode,
            @RequestParam(name = "xml", required = false) String xml,
            @RequestParam(name = "file", required = false) String file) {

        currCode = currCode.toUpperCase(Locale.ROOT);
        if (date.equals("now")) {
            logger.debug("Date parsing \"now\" value");
            date = DateParser.nowCommonFormat();
        }

        logger.info("Rate request on " + date + " for " + currCode);
        logger.debug("Currency validation");
        if (!CommonDto.validateCurrency(currCode)) {
            logger.info("Currency validation failed");
            throw new IllegalArgumentException("Invalid currency");
        }
        logger.debug("Date validation");
        if (!DateParser.validateDate(date)) {
            logger.info("Date validation failed");
            throw new IllegalArgumentException("Invalid date");
        }

        String fDate = date;
        String fCurrCode = currCode;

        return () -> {
            logger.debug("Getting common DTOs on " + fDate + " for " + fCurrCode);
            List<Future<CommonDto>> futures = new ArrayList<>();
            for (RateService service : services) {
                futures.add(service.getCommonDto(fDate, fCurrCode));
            }

            List<CommonDto> rates = new ArrayList<>();
            for (Future<CommonDto> future : futures) {
                if (future.get() != null) rates.add(future.get());
            }

            logger.debug("Sending response");
            if(file != null) {
                logger.info("Saving to file");
                byte[] bytes = WordCreator.wordToBytes(WordCreator.createWord(rates));
                return ResponseEntity.ok().headers(getHeaders(xml, file, fCurrCode, fDate)).body(bytes);
            } else {
                return ResponseEntity.ok().headers(getHeaders(xml, null, fCurrCode, fDate)).body(rates);
            }
        };
    }

    @GetMapping(path = "/bestrate")
    @Cacheable("bestRates")
    public Callable<ResponseEntity<?>> getBestRate(
            @RequestParam(name = "period", defaultValue = "week") String period,
            @RequestParam(name = "code", defaultValue = "USD") String currCode,
            @RequestParam(name = "xml", required = false) String xml,
            @RequestParam(name = "file", required = false) String file) {

        currCode = currCode.toUpperCase(Locale.ROOT);
        period = period.toLowerCase();

        logger.info("Best rate request on " + period + " for " + currCode);
        logger.debug("Currency validation");
        if (!CommonDto.validateCurrency(currCode)) {
            logger.info("Currency validation failed");
            throw new IllegalArgumentException("Invalid currency");
        }

        if (!DateParser.validatePeriod(period)) {
            logger.info("Period validation failed");
            throw new IllegalArgumentException("Invalid period]");
        }

        String fPeriod = period;
        String fCurrCode = currCode;

        return () -> {
            LocalDate startDate = DateParser.parseStartDate(fPeriod);
            LocalDate endDate = DateParser.parseEndDate(fPeriod);

            double maxSell = 0;
            String sellDate = DateParser.parseToCommon(startDate);
            double maxBuy = 0;
            String buyDate = DateParser.parseToCommon(startDate);

            List<Future<CommonDto>> futures = new ArrayList<>();

            logger.debug("Getting common DTOs for every day of period");
            while (!startDate.isAfter(endDate)) {
                Future<CommonDto> future = bestRateService.getCommonDto(DateParser.parseToCommon(startDate), fCurrCode);
                futures.add(future);
                startDate = startDate.plusDays(1);
            }

            for (Future<CommonDto> future : futures) {
                CommonDto commonDto;
                commonDto = future.get();

                if (maxSell == 0 || maxSell < commonDto.getSellRate()) {
                    maxSell = commonDto.getSellRate();
                    sellDate = commonDto.getDate();
                }

                if (maxBuy == 0 || maxBuy > commonDto.getPurchaseRate()) {
                    maxBuy = commonDto.getPurchaseRate();
                    buyDate = commonDto.getDate();
                }
            }

            CommonDto bestDto = new CommonDto();
            bestDto.setService(services.get(0).getName());
            bestDto.setDate("sell date: " + sellDate + ", purchase date: " + buyDate);
            bestDto.setCurrency(fCurrCode);
            bestDto.setSellRate(maxSell);
            bestDto.setPurchaseRate(maxBuy);

            logger.debug("Sending response");
            if(file != null) {
                logger.info("Saving to file");
                byte[] bytes = WordCreator.wordToBytes(WordCreator.createWord(Collections.singletonList(bestDto)));
                return ResponseEntity.ok().headers(getHeaders(xml, file, fCurrCode, fPeriod)).body(bytes);
            } else {
                return ResponseEntity.ok().headers(getHeaders(xml, null, fCurrCode, fPeriod)).body(bestDto);
            }
        };
    }

    private HttpHeaders getHeaders(String xml, String file, String currCode, String date) {
        logger.debug("Setting response type");
        HttpHeaders headers = new HttpHeaders();
        if (file != null) {
            headers.add("Content-Disposition", "attachment; filename=" +
                    date + "_"  + currCode + "_rate.docx");
        } else if (xml != null) {
            headers.setContentType(MediaType.APPLICATION_XML);
        } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return headers;
    }

    private static final String REFERENCE = "Currency aggregator REST API endpoint reference\n" +
            "Base route (API reference): /api\n\n" +
            "Exchange rates: /api/rate\n" +
            "Parameters: 'date' - date in format \"dd.MM.yyyy\"\n" +
            "'code' - ISO 4217 currency letter-code\n" +
            "Keys: 'xml' - xml response format instead of json\n" +
            "'file' - key to get MS Word document with the response\n\n" +
            "Best rate: /api/bestrate\n" +
            "Parameters: 'period' - wished period for selection of best rate\n" +
            "Accepted date period in format dd.MM.yyyy-dd.MM.yyyy or values \"week\", \"month\"\n" +
            "that means last week and last month\n" +
            "Maximal period is 100 days\n" +
            "'code' - ISO 4217 currency letter-code\n" +
            "Keys: 'xml' - xml response format instead of json\n" +
            "'file' - key to get MS Word document with the response";
}
