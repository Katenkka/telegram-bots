package net.chmilevfa.telegram.bots.currency.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chmilevfa.telegram.bots.currency.Currencies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Service for getting an up-to-date currency rate.
 *
 * @author chmilevfa
 * @since 07.07.18
 */
@Service
public class CurrencyService {

    private static Logger logger = LoggerFactory.getLogger(CurrencyService.class);

    /** URL of exchange rate service */
    private final static String CURRENCY_CONVERTER_URL =
            "https://free.currencyconverterapi.com/api/v5/convert?q=%s&compact=y";
    /** Mapper for reading JSON */
    private static final ObjectMapper MAPPER = new ObjectMapper();
    /**
     * Get rate for pair of currencies.
     * @param from currency to convert from.
     * @param to currency to convert to.
     * @return current rate for from/to.
     */
    public float getRate(Currencies from, Currencies to) throws IOException {
        String currencyArg = from.name() + "_" + to.name();
        String uri = String.format(CURRENCY_CONVERTER_URL, currencyArg);

        URL url = new URL(uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        logger.info("Requesting currency rate for {} pair. Request: {}", currencyArg, con.getURL());

        StringBuilder content = new StringBuilder();
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
        }

        return extractCurrencyRate(content.toString(), currencyArg);
    }

    float extractCurrencyRate(String data, String currencyArg) throws IOException {
        String textValue = MAPPER.readTree(data).at("/" + currencyArg + "/val").asText();
        return Float.parseFloat(textValue);
    }
}
