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

    double[] cashFlow = Cashflow.cashFlow(50429, BigDecimal.valueOf(0.149000),5, 12, 30);
    double irr = irr(cashFlow);
    assertEquals(0.013248756, irr, 1E-5);

    cashFlow = Cashflow.cashFlow(263429, BigDecimal.valueOf(0.189000), 15, 12, 30);
    irr = irr(cashFlow);
    assertEquals(0.015878621, irr, 1E-5);

    // Highest possible values
    cashFlow = Cashflow.cashFlow(500429, BigDecimal.valueOf(0.189500), 15, 0, 30);
    irr = irr(cashFlow);
    assertEquals(0.015860156, irr, 1E-5);

    // Lowest possible
    cashFlow = Cashflow.cashFlow(20429, BigDecimal.valueOf(0.019500), 2, 0, 0);
    irr = irr(cashFlow);
    assertEquals(0.001625, irr, 1E-5);

    // Highest possible effective interest
    cashFlow = Cashflow.cashFlow(20429, BigDecimal.valueOf(0.189500), 2, 0, 30);
    irr = irr(cashFlow);
    assertEquals(0.01829059, irr, 1E-5);

  }
}
