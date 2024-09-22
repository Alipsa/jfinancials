package financials;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.alipsa.financials.InternalRateOfReturn.irr;

import org.junit.jupiter.api.Test;
import se.alipsa.financials.Cashflow;

import java.math.BigDecimal;

public class IrrTest {

  /**
   * The values we match up with is taken from the Excel sheet "Calculation loan agreement.xlsx" in the doc folder.
   */
  @Test
  public void testIrrCalculations() {

    double[] cashFlow = Cashflow.calculateCashFlow(50429, 5, 12, BigDecimal.valueOf(0.149000), 30);
    double irr = irr(cashFlow);
    assertEquals(0.013248756, irr, 1E-5);

    cashFlow = Cashflow.calculateCashFlow(263429, 15, 12, BigDecimal.valueOf(0.189000), 30);
    irr = irr(cashFlow);
    assertEquals(0.015878621, irr, 1E-5);

    // Highest possible values
    cashFlow = Cashflow.calculateCashFlow(500429, 15, 0, BigDecimal.valueOf(0.189500), 30);
    irr = irr(cashFlow);
    assertEquals(0.015860156, irr, 1E-5);

    // Lowest possible
    cashFlow = Cashflow.calculateCashFlow(20429, 2, 0, BigDecimal.valueOf(0.019500), 0);
    irr = irr(cashFlow);
    assertEquals(0.001625, irr, 1E-5);

    // Highest possible effective interest
    cashFlow = Cashflow.calculateCashFlow(20429, 2, 0, BigDecimal.valueOf(0.189500), 30);
    irr = irr(cashFlow);
    assertEquals(0.01829059, irr, 1E-5);

  }
}
