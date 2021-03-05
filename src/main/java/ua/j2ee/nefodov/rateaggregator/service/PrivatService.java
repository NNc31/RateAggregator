package ua.j2ee.nefodov.rateaggregator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import ua.j2ee.nefodov.rateaggregator.model.CommonDto;
import ua.j2ee.nefodov.rateaggregator.model.DateParser;
import ua.j2ee.nefodov.rateaggregator.model.PrivatDto;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Future;

@Service
public class PrivatService implements RateService {

    private static final Logger logger = LoggerFactory.getLogger(PrivatService.class);

    @Override
    public String getName() {
        return "PrivatBank";
    }

    @Async
    public Future<CommonDto> getCommonDto(String date, String currCode) {
        PrivatDto privatDto;
        CommonDto commonDto = new CommonDto();
        commonDto.setService(getName());
        commonDto.setDate(date);
        commonDto.setCurrency(currCode);

        date = DateParser.parseToPrivat(date);

        String url = "https://api.privatbank.ua/p24api/exchange_rates?json&date=" + date;

        ObjectMapper mapper = new ObjectMapper();
        try {
            privatDto = mapper.readValue(new URL(url), PrivatDto.class);
        } catch (IOException e) {
            logger.warn("IOException for URL " + url);
            throw new IllegalStateException("Unexpected response from PrivatBank");
        }

        if (privatDto.getExchangeRate().isEmpty()) {
            throw new IllegalStateException("PrivatBank is unavailable");
        }

        PrivatDto.PrivatRate rate = privatDto.getExchangeRate().stream().
                filter(privatRate -> currCode.equals(privatRate.getCurrency())).
                findAny().orElse(null);
        if (rate == null) {
            throw new IllegalArgumentException("Invalid currency");
        }

        if (rate.getSaleRate() != 0) commonDto.setSellRate(rate.getSaleRate());
        else commonDto.setSellRate(rate.getSaleRateNB());

        if (rate.getPurchaseRate() != 0) commonDto.setPurchaseRate(rate.getPurchaseRate());
        else commonDto.setPurchaseRate(rate.getPurchaseRateNB());

        return new AsyncResult<>(commonDto);
    }
}
