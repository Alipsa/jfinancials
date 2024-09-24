package financials;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.financials.LoanCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

public class LoanCalculatorTest {
  private static final Logger LOG = LoggerFactory.getLogger(LoanCalculatorTest.class);

  @Test
  public void testPaymentCalculations() {

    int monthlyPayments = (int)Math.round(LoanCalculator.monthlyAnnuityAmount(100_000, 0.0495, 3 * 12,0));
    assertEquals(2995, monthlyPayments, "monthlyPayments, 100 000 at 4.95 over 3 years");
    assertEquals(
        //108900,
        108894,
        (int)Math.round(LoanCalculator.totalPaymentAmount(100_000, 0.0495, 3 * 12, 0,30)),
        "Total payment, 100 000 at 4.95 over 3 years");

    assertEquals(644.1859,
        round(
            LoanCalculator.monthlyAnnuityAmount(120_000, 0.05, 30 * 12, 0), 4),
        0.00001,
        "monthlyPayments, 120 000 at 5% over 30 years");

    assertEquals(4603.89,
        round(
            LoanCalculator.monthlyAnnuityAmount(100_429, 0.0535, 2 * 12, 1), 4),
        0.01,
        "monthlyPayments, 100 429 at 5.35% over 2 years");

    assertEquals(4191,
        round(
            LoanCalculator.monthlyAnnuityAmount(400_429, 0.0471, 10 * 12, 0), 0),
        0.01,
        "monthlyPayments, 400 439 at 4.71% over 10 years");

  }

  @Test
  public void testEffectiveInterest() {
    //loanAmount: 263429, tenureYears: 15,amfreeMonths: 0, interest: 0.055000, statementFee: 30. ApplicationId = 1234567
    long start = System.currentTimeMillis();
    double eir = LoanCalculator.effectiveInterestRate(263429, BigDecimal.valueOf(0.055000), 15 * 12, 12, 30);
    assertEquals(0.0585669, eir, 1E-7);
    eir = LoanCalculator.effectiveInterestRate(50429, BigDecimal.valueOf(0.045000), 14 * 12, 12, 30);
    assertEquals(0.0574472, eir, 1E-7);
    eir = LoanCalculator.effectiveInterestRate(463429, BigDecimal.valueOf(0.055000), 15 * 12, 12, 30);
    assertEquals(0.0576366, eir, 1E-7);
    eir = LoanCalculator.effectiveInterestRate(50429, BigDecimal.valueOf(0.149000), 5 * 12, 12, 30);
    assertEquals(0.1710972, eir, 1E-7);
    eir = LoanCalculator.effectiveInterestRate(50429, BigDecimal.valueOf(0.149000), 15 * 12, 12, 30);
    assertEquals(0.1693646, eir, 1E-7);
    long end = System.currentTimeMillis();
    long executionTime = (end - start);
    assertTrue(executionTime < 50, "5 Effective interest calculations should take less than 50ms but was " + executionTime);
  }

  @Test
  public void testExamples() {

    verifyCalculations(263_429, 15 * 12, 12, 0.055000, 30,
        0.0585669, 2251.86, 398_200.88, 23.63);

    verifyCalculations(20_429, 2 * 12, 0, 0.1895, 30,
        0.2429703, 1_029.30, 25_423.23, 5.86);

    // Same as we use in ModelTestdata but we cannot use it directly as we would ge a circular dependency
    // so we just copy the values here
    verifyCalculations(50_429, 8 * 12, 12, 0.135, 30,
        0.1548003, 931.17, 87905.87, 11.85);

    verifyCalculations(400_429, 10 * 12, 0, 0.0471, 30,
        0.0497410, 4190.64, 506476.43, 28.07);

  }

  private void verifyCalculations(int loanAmt, int tenureMonths, int amortizationFreeMonths, double interest, int statementFee,
                                  double expEffectiveInterest, double expMonthlyAnnuityAmt, double expTotalPaymentAmt,
                                  double expDailyInterestAmt) {

    double eir = LoanCalculator.effectiveInterestRate(loanAmt, BigDecimal.valueOf(interest), tenureMonths, amortizationFreeMonths, statementFee);
    assertEquals(expEffectiveInterest, eir, 1E-7);

    NumberFormat formatter = NumberFormat.getPercentInstance();
    formatter.setMinimumFractionDigits(2);
    String prettyInterest = formatter.format(interest);

    double monthlyPayments = LoanCalculator.monthlyAnnuityAmount(loanAmt, interest, tenureMonths, amortizationFreeMonths);
    assertEquals(expMonthlyAnnuityAmt, monthlyPayments, 0.01,
        "monthlyPayments, " + loanAmt + " at " + prettyInterest + " over " + tenureMonths/12 + " years");

    double totalPaymentAmt = LoanCalculator.totalPaymentAmount(loanAmt, interest, tenureMonths, amortizationFreeMonths, statementFee);
    assertEquals(
        expTotalPaymentAmt, totalPaymentAmt, 0.01,
        "Total payment, " + loanAmt + " at " + prettyInterest + " over " + tenureMonths/12 + " years");

    double daylyInterestAmt = LoanCalculator.dailyInterestAmount(loanAmt, BigDecimal.valueOf(interest), tenureMonths,
        amortizationFreeMonths, statementFee);
    assertEquals(expDailyInterestAmt, daylyInterestAmt, 0.01, "Daily Interest");
  }

  /*
   * check that calculations works for short tenure with amortizationfree
   */
  @Test
  public void testShortTenureAmortizationFree() {
    //System.out.println(LoanCalculator.createPaymentInfo(236429, new BigDecimal("0.059"), 2, 12, 0));
    verifyCalculations(236429, 2 * 12, 12, 0.059, 0,
        0.060621898, 20_337.73, 258_002.12, 29.55);
  }

  private static double round(double value, int decimals) {
    if (decimals < 0) throw new IllegalArgumentException();
    BigDecimal bd = new BigDecimal(Double.toString(value));
    bd = bd.setScale(decimals, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }
}
