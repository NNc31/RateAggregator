package ua.j2ee.nefodov.rateaggregator.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import ua.j2ee.nefodov.rateaggregator.model.MonoDto;
import ua.j2ee.nefodov.rateaggregator.model.Rate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.concurrent.Future;

@Service
public class MonoService implements RateService {

    private static final Logger logger = LoggerFactory.getLogger(MonoService.class);

    private static final String STRING_URL = "https://api.monobank.ua/bank/currency";

    private MonoDto monoDto = null;

    @Async
    @Override
    public Future<Rate> getRate(LocalDate date, Currency currency) {
        Rate rate = new Rate("MonoBank",
                date.format(DateTimeFormatter.ISO_DATE), currency.getCurrencyCode());

        if (!date.equals(LocalDate.now()) || !setMonoDto()) {
            rate.setSellRate(0);
            rate.setPurchaseRate(0);
        } else {
            JSONObject data = monoDto.getJsonObject(currency.getCurrencyCode());
            if (data == null) {
                logger.warn("No data for currency " + currency.getCurrencyCode());
                rate.setSellRate(0);
                rate.setPurchaseRate(0);
            } else {
                if (data.has("rateSell")) {
                    rate.setSellRate(data.getDouble("rateSell"));
                } else if (data.has("rateCross")) {
                    rate.setSellRate(data.getDouble("rateCross"));
                } else {
                    logger.warn("No sell rate from MonoBank for currency " + currency.getCurrencyCode());
                    rate.setSellRate(0);
                }

                if (data.has("rateBuy")) {
                    rate.setPurchaseRate(data.getDouble("rateBuy"));
                } else if (data.has("rateCross")) {
                    rate.setPurchaseRate(data.getDouble("rateCross"));
                } else {
                    logger.warn("No buy rate from MonoBank for currency " + currency.getCurrencyCode());
                    rate.setPurchaseRate(0);
                }
            }
        }

        return new AsyncResult<>(rate);
    }

    @Async
    public Future<Rate> getBestRateOnPeriod(LocalDate start, LocalDate end, Currency currency) {
        throw new UnsupportedOperationException();
    }

    private boolean setMonoDto() {
        if (monoDto == null) {

            URL url;
            try {
                url = new URL(STRING_URL);
            } catch (MalformedURLException e) {
                logger.warn("MonoBank is unavailable");
                return false;
            }

            StringBuilder builder = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String inputStr;
                while ((inputStr = in.readLine()) != null) {
                    builder.append(inputStr);
                }
            } catch (IOException e) {
                logger.warn("IOException in response from MonoBank");
                return false;
            }

            monoDto = new MonoDto();
            monoDto.setJsonArray(new JSONArray(builder.toString()));
        }

        return true;
    }
}
