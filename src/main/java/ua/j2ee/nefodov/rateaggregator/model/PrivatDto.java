package ua.j2ee.nefodov.rateaggregator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrivatDto {

    private List<PrivatRate> exchangeRate;

    public List<PrivatRate> getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(List<PrivatRate> exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PrivatRate {

        private String currency;
        private double saleRate;
        private double purchaseRate;
        private double saleRateNB;
        private double purchaseRateNB;

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public double getSaleRate() {
            return saleRate;
        }

        public void setSaleRate(double saleRate) {
            this.saleRate = saleRate;
        }

        public double getPurchaseRate() {
            return purchaseRate;
        }

        public void setPurchaseRate(double purchaseRate) {
            this.purchaseRate = purchaseRate;
        }

        public double getSaleRateNB() {
            return saleRateNB;
        }

        public void setSaleRateNB(double saleRateNB) {
            this.saleRateNB = saleRateNB;
        }

        public double getPurchaseRateNB() {
            return purchaseRateNB;
        }

        public void setPurchaseRateNB(double purchaseRateNB) {
            this.purchaseRateNB = purchaseRateNB;
        }
    }
}
