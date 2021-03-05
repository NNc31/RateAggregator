package ua.j2ee.nefodov.rateaggregator.model;

import java.util.Currency;

public class CommonDto {
    private String service;
    private String date;
    private String currency;
    private double sellRate;
    private double purchaseRate;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getSellRate() {
        return sellRate;
    }

    public void setSellRate(double sellRate) {
        this.sellRate = sellRate;
    }

    public double getPurchaseRate() {
        return purchaseRate;
    }

    public void setPurchaseRate(double purchaseRate) {
        this.purchaseRate = purchaseRate;
    }

    public static boolean validateCurrency(String currCode) {
        try {
            Currency.getInstance(currCode);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
