package ua.j2ee.nefodov.rateaggregator.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import ua.j2ee.nefodov.rateaggregator.model.CommonDto;
import ua.j2ee.nefodov.rateaggregator.model.DateParser;
import ua.j2ee.nefodov.rateaggregator.model.MonoDto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;


@Service
public class MonoService implements RateService {

    private static final Logger logger = LoggerFactory.getLogger(MonoService.class);

    private MonoDto monoDto = null;

    @Override
    public String getName() {
        return "MonoBank";
    }

    @Async
    public Future<CommonDto> getCommonDto(String date, String currCode) {
        CommonDto commonDto = new CommonDto();
        commonDto.setDate(date);
        commonDto.setCurrency(currCode);

        if (!DateParser.nowCommonFormat().equals(date)) {
            return new AsyncResult<>(null);
        }

        if (monoDto == null) {
            monoDto = new MonoDto();
            URL url;
            try {
                url = new URL("https://api.monobank.ua/bank/currency");
            } catch (MalformedURLException e) {
                logger.warn("Malformed URL exception in MonoBank");
                throw new RuntimeException("Unexpected response from MonoBank");
            }

            StringBuilder builder = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String inputStr;
                while ((inputStr = in.readLine()) != null) {
                    builder.append(inputStr);
                }
            } catch (IOException e) {
                logger.warn("IOException while reading response from MonoBank URL");
                throw new IllegalStateException("Unexpected response from MonoBank");
            }

            monoDto.setJsonArray(new JSONArray(builder.toString()));
        }

        JSONObject data = monoDto.getJsonObject(currCode);
        if (data == null) {
            logger.info("Data is null for currency " + currCode);
            throw new IllegalArgumentException("Invalid currency code");
        }

        commonDto.setService(getName());
        if (data.has("rateSell")) {
            commonDto.setSellRate(data.getDouble("rateSell"));
        } else if (data.has("rateCross")) {
            commonDto.setSellRate(data.getDouble("rateCross"));
        } else {
            logger.info("Unexpected response from MonoBank for currency " + currCode);
            throw new IllegalStateException("Unexpected response from MonoBank");
        }

        if (data.has("rateBuy")) {
            commonDto.setPurchaseRate(data.getDouble("rateBuy"));
        } else if (data.has("rateCross")) {
            commonDto.setPurchaseRate(data.getDouble("rateCross"));
        } else {
            logger.info("Unexpected response from MonoBank for currency " + currCode);
            throw new IllegalStateException("Unexpected response from MonoBank");
        }

        return new AsyncResult<>(commonDto);
    }
}
