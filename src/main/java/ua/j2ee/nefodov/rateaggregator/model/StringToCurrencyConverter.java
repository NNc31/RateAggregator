package ua.j2ee.nefodov.rateaggregator.model;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class StringToCurrencyConverter implements Converter<String, Currency> {

    @Override
    public Currency convert(String currCode) {
        return Currency.getInstance(currCode.toUpperCase());
    }
}
