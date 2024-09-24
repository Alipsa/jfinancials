package financials;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.alipsa.jfinancials.Financials.*;

import org.junit.jupiter.api.Test;
import se.alipsa.jfinancials.*;

import java.math.BigDecimal;

public class PaymentPlanTest {

  @Test
  public void testPaymentPlanWithInvoiceFee() {
    int loanAmt = 50_429;
    int tenure = 6 * 12;
    int amortizationFreeMonths = 0;

    BigDecimal interest = BigDecimal.valueOf(0.0677);
    BigDecimal invoiceFee = BigDecimal.valueOf(30);

    PaymentPlan paymentPlan = paymentPlan(loanAmt, interest, tenure, amortizationFreeMonths, invoiceFee);

    Payment m0 = paymentPlan.get(0);
    assertEquals(BigDecimal.valueOf(loanAmt), m0.getOutgoingBalance());
    assertEquals(BigDecimal.valueOf(loanAmt * -1), m0.getCacheFlow());

    verifyPayment(1, 854.21, 284.50, 569.70, invoiceFee,
        49_859.30, 884.21, paymentPlan.get(1));
    verifyPayment(5, 854.21, 271.54, 582.67, invoiceFee,
        47_548.17, 884.21, paymentPlan.get(5));
    verifyPayment(15, 854.21, 237.82, 616.39, invoiceFee,
        41_537.60, 884.21, paymentPlan.get(15));
    verifyPayment(35, 854.21, 164.41, 689.79, invoiceFee,
        28_452.83, 884.21, paymentPlan.get(35));
    verifyPayment(70, 854.21, 14.30, 839.91, invoiceFee,
        1_694.06, 884.21, paymentPlan.get(70));
    verifyPayment(72, 854.21, 4.79, 849.41, invoiceFee,
        0, 884.21, paymentPlan.get(72));

    double irr = irr(paymentPlan);
    assertEquals(0.006667407, irr, 0.0000001);

    double apr = apr(irr);
    assertEquals(0.0830, apr, 0.0001);

    double effectiveInterest = effectiveInterestRate(irr*12);
    assertEquals(0.0830, effectiveInterest, 0.0001);

  }

  @Test
  public void testToString() {
    var loanAmt = 10000;
    var tenureYears = (int)(1.5 * 12);
    var amortizationFreeMonths = 6;
    var interest = BigDecimal.valueOf(3.5 / 100);
    var invoiceFee = BigDecimal.valueOf(30);

    var paymentPlan = paymentPlan(loanAmt, interest, tenureYears, amortizationFreeMonths, invoiceFee);
    String[] rows = paymentPlan.toString().split("\n");
    assertEquals("month,costOfCredit,interestAmt,amortization,invoiceFee,outgoingBalance,cashFlow", rows[0]);
    assertEquals("0,0.00,0.00,0.00,0.00,10000.00,-10000.00", rows[1]);
    assertEquals("18,849.22,2.47,846.75,30.00,0.00,879.22", rows[19]);
  }

  @Test
  public void testPaymentPlanWithoutInvoiceFee() {
    int loanAmt = 100_429;
    int tenure = 2 * 12;
    int amortizationFreeMonths = 1;

    BigDecimal interest = BigDecimal.valueOf(0.0535);
    BigDecimal invoiceFee = BigDecimal.valueOf(0);

    PaymentPlan paymentPlan = paymentPlan(loanAmt, interest, tenure, amortizationFreeMonths, invoiceFee);

    Payment m0 = paymentPlan.get(0);
    assertEquals(BigDecimal.valueOf(loanAmt), m0.getOutgoingBalance());
    assertEquals(BigDecimal.valueOf(loanAmt * -1), m0.getCacheFlow());

    verifyPayment(1, 447.75, 447.75, 0, invoiceFee,
        100_429, 447.75, paymentPlan.get(1));

    verifyPayment(16, 4603.89, 180.68, 4423.21, invoiceFee,
        36_103.08, 4603.89, paymentPlan.get(16));

    verifyPayment(23, 4603.89, 40.78, 4563.12, invoiceFee,
        4583.46, 4603.89, paymentPlan.get(23));

    verifyPayment(24, 4603.89, 20.43, 4583.46, invoiceFee,
        0, 4603.89, paymentPlan.get(24));

    double irr = irr(paymentPlan);
    assertEquals(0.004458333, irr, 0.0000001);

    double apr = apr(irr);
    assertEquals(0.054832, apr, 0.000001);

    double effectiveInterest = effectiveInterestRate(irr*12);
    assertEquals(0.054832, effectiveInterest, 0.000001);

    var rowList = paymentPlan.toRowList();
    var p23 = rowList.get(23);
    double delta = 0.01;
    assertEquals(23, p23.get(0), "month");
    assertEquals(4603.89, p23.get(1).doubleValue(), delta, "costOfCredit");
    assertEquals(40.78, p23.get(2).doubleValue(), delta, "interestAmt");
    assertEquals(4563.12, p23.get(3).doubleValue(), delta, "amortization");
    assertEquals(invoiceFee, p23.get(4), "invoiceFee");
    assertEquals(4583.46, p23.get(5).doubleValue(), delta, "outgoingBalance");
    assertEquals(4603.89, p23.get(6).doubleValue(), delta, "cacheFlow");
  }

  private void verifyPayment(int month, double costOfCredit, double interestAmt, double amortization,
                             BigDecimal invoiceFee, double outGoingBalance, double cacheFlow, Payment p) {
    double delta = 0.01;
    assertEquals(month, p.getMonth(), "month");
    assertEquals(costOfCredit, p.getCostOfCredit().doubleValue(), delta, "costOfCredit");
    assertEquals(interestAmt, p.getInterestAmt().doubleValue(), delta, "interestAmt");
    assertEquals(amortization, p.getAmortization().doubleValue(), delta, "amortization");
    assertEquals(invoiceFee, p.getInvoiceFee(), "invoiceFee");
    assertEquals(outGoingBalance, p.getOutgoingBalance().doubleValue(), delta, "outgoingBalance");
    assertEquals(cacheFlow, p.getCacheFlow().doubleValue(), delta, "cacheFlow");
  }
}
