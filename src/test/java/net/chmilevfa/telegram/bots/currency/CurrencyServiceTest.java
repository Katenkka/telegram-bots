package net.chmilevfa.telegram.bots.currency;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link CurrencyService}
 *
 * @author chmilevfa
 * @since 07.07.18
 */
public class CurrencyServiceTest {

    private CurrencyService underTest;
    private final String testJsonRate = "{\"USD_PHP\":{\"val\":53.310001}}";

    @Before
    public void init() {
        underTest = new CurrencyService();
    }

    @Test
    public void extractCurrencyRateTest() {
        Float actualVal = underTest.extractCurrencyRate(testJsonRate, "USD_PHP");
        Assert.assertEquals(53.310001, actualVal, 0.00001);
    }
}
