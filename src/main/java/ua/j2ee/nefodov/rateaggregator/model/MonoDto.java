package ua.j2ee.nefodov.rateaggregator.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Currency;

public class MonoDto {

    private JSONArray jsonArray = null;
    private final int UAH_CODE = 980;

    public void setJsonArray(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    public JSONObject getJsonObject(String currCode) {
        int numericCode = Currency.getInstance(currCode).getNumericCode();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject data = jsonArray.getJSONObject(i);

            if (data.getInt("currencyCodeB") == UAH_CODE &&
                    data.getInt("currencyCodeA") == numericCode) {
                return data;
            }
        }

        return null;
    }
}
