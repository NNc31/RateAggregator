package ua.j2ee.nefodov.rateaggregator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ua.j2ee.nefodov.rateaggregator.model.CommonDto;
import ua.j2ee.nefodov.rateaggregator.model.DateParser;
import ua.j2ee.nefodov.rateaggregator.model.NbuDto;

import java.util.concurrent.Future;

@Service
public class NbuService implements RateService {

    private static final Logger logger = LoggerFactory.getLogger(NbuService.class);

    @Override
    public String getName() {
        return "National bank of Ukraine";
    }

    @Async
    public Future<CommonDto> getCommonDto(String date, String currCode) {
        NbuDto nbuDto;
        CommonDto commonDto = new CommonDto();
        commonDto.setService(getName());
        commonDto.setDate(date);
        commonDto.setCurrency(currCode);

        date = DateParser.parseToNbu(date);

        String url = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json" +
                "&date=" + date + "&valcode=" + currCode;

        RestTemplate restTemplate = new RestTemplate();
        NbuDto[] response = restTemplate.getForObject(url, NbuDto[].class);
        if (response == null) {
            logger.warn("Unexpected response from NBU: null response for URL " + url);
            throw new IllegalStateException("Unexpected response from services");
        } else if (response.length == 1) {
            nbuDto = response[0];
        } else {
            logger.info("Invalid currency " + currCode);
            throw new IllegalArgumentException("Invalid currency");
        }

        commonDto.setSellRate(nbuDto.getRate());
        commonDto.setPurchaseRate(nbuDto.getRate());

        return new AsyncResult<>(commonDto);
    }
}
