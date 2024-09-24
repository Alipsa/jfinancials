package financials;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.alipsa.jfinancials.Financials.*;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

public class IrrTest {

  /**
   * The values we match up with is taken from the Excel sheet "Calculation loan agreement.xlsx" in the doc folder.
   */
  @Test
  public void testIrrCalculations() {

    double[] cashFlow = cashFlow(50429, BigDecimal.valueOf(0.149000),5 * 12, 12, 30);
    double irr = irr(cashFlow);
    assertEquals(0.013248756, irr, 1E-5);

    cashFlow = cashFlow(263429, BigDecimal.valueOf(0.189000), 15 * 12, 12, 30);
    irr = irr(cashFlow);
    assertEquals(0.015878621, irr, 1E-5);

    cashFlow = cashFlow(500429, BigDecimal.valueOf(0.189500), 15 * 12, 0, 30);
    irr = irr(cashFlow);
    assertEquals(0.015860156, irr, 1E-5);

    cashFlow = cashFlow(20429, BigDecimal.valueOf(0.019500), 2 * 12, 0, 0);
    irr = irr(cashFlow);
    assertEquals(0.001625, irr, 1E-5);

    cashFlow = cashFlow(20429, BigDecimal.valueOf(0.189500), 2 * 12, 0, 30);
    irr = irr(cashFlow);
    assertEquals(0.01829059, irr, 1E-5);

  }

  @Test
  void testFromPaymentPlan() {
    var loanAmt = 10000;
    var tenureYears = (int)(1.5 * 12);
    var amortizationFreeMonths = 6;
    var interest = BigDecimal.valueOf(3.5 / 100);
    var invoiceFee = BigDecimal.valueOf(30);

    var paymentPlan = paymentPlan(loanAmt, interest, tenureYears, amortizationFreeMonths, invoiceFee);
    var internalReturn = irr(paymentPlan.getColumn("cashFlow"));
    assertEquals(0.00715680230, internalReturn, 0.0000000001);

    assertEquals(internalReturn, irr(paymentPlan), "irr from PaymentPlan and from cashflow list differs");
  }

  @Test
  void testNpv() {
    assertEquals(5908.8656360761, npv(new double[] {-123400, 36200, 54800, 48100}, 0.035), 0.0000000001);
    assertEquals(5908.8656360761, npv(List.of(-123400, 36200, 54800, 48100), 0.035), 0.0000000001);
  }
}
