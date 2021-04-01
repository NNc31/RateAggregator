package ua.j2ee.nefodov.rateaggregator.model;

public class Rate {
    private String service;
    private String date;
    private String currency;
    private double sellRate;
    private double purchaseRate;

    public Rate(String service, String date, String currency) {
        this.service = service;
        this.date = date;
        this.currency = currency;
    }

    public Rate(String service, String date, String currency, double sellRate, double purchaseRate) {
        this.service = service;
        this.date = date;
        this.currency = currency;
        this.sellRate = sellRate;
        this.purchaseRate = purchaseRate;
    }

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
}
