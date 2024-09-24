package se.alipsa.financials;

import static java.math.BigDecimal.ONE;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;

public class LoanCalculator {
  /**
   * Utility class, only static methods
   *
   */
  private LoanCalculator() {
  }

  public static double totalPaymentAmount(double loanAmount, double interestRate, int tenureMonths, int amortizationFreeMonths, int statementFee) {
    double monthlyAnnuity = monthlyAnnuityAmount(loanAmount, interestRate, tenureMonths, amortizationFreeMonths);
    return totalPaymentAmount(loanAmount, interestRate, tenureMonths, amortizationFreeMonths, statementFee, monthlyAnnuity);
  }

  /** If we know the monthly annuity we take advantage of that for faster execution */
  public static double totalPaymentAmount(double loanAmount, double interestRate, int tenureMonths, int amortizationFreeMonths, int statementFee, double monthlyAnnuity) {
    double interestCostAmfreePeriod = loanAmount * interestRate / 12;
    return (monthlyAnnuity + statementFee) * tenureMonths - (monthlyAnnuity - interestCostAmfreePeriod) * amortizationFreeMonths;
  }

  public static BigDecimal totalPaymentAmountRounded(double loanAmount, BigDecimal interestRate, int tenureMonths, int amortizationFreeMonths, int statementFee) {
    return BigDecimal
        .valueOf(totalPaymentAmount(loanAmount, interestRate.doubleValue(), tenureMonths, amortizationFreeMonths, statementFee))
        .setScale(2, RoundingMode.HALF_UP);
  }


  /**
   *
   * @param loanAmount the loan amount including startup fee
   * @param interestRate the nominal yearly interest
   * @param tenureMonths tenure in months
   * @param amortizationFreemonths number of month amortization free
   * @return the monthyl annuity amount
   */
  public static double monthlyAnnuityAmount(double loanAmount, double interestRate, int tenureMonths, int amortizationFreemonths) {
    double montlyInterest = interestRate / 12;
    int totalNumberOfPaymentPeriods = tenureMonths - amortizationFreemonths;
    return pmt(montlyInterest, totalNumberOfPaymentPeriods, loanAmount * -1);
  }

  public static double dailyInterestAmount(int loanAmount, BigDecimal interestRate, int tenureMonths, int amFreeMonths, int statementFee) {
    List<Payment> paymentPlan = Cashflow.paymentPlan(loanAmount, interestRate, tenureMonths, amFreeMonths, BigDecimal.valueOf(statementFee));
    return dailyInterestAmount(paymentPlan, tenureMonths);
  }

  public static double dailyInterestAmount(List<Payment> paymentPlan, int tenureMonths) {
    double totalInterest = paymentPlan.stream().mapToDouble(p -> nz(p.getInterestAmt())).sum();
    // 30.41666 is from Konsumentverkets guidelines https://www.konsumentverket.se/globalassets/publikationer/produkter-och-tjanster/finansiella-tjanster/kovfs-2011-01-allmanna-rad-konsumentkrediter-v3--konsumentverket.pdf
    return totalInterest / (tenureMonths * 30.41666);
  }

  /**
   * Emulates Excel/Calc's PMT(interest_rate, number_payments, PV, FV, Type)
   * function, which calculates the payments for a loan or the future value of an investment
   *
   * @param r    - periodic interest rate represented as a decimal.
   * @param nper - number of total payments / periods.
   * @param pv   - present value -- borrowed or invested principal.
   * @param fv   - future value of loan or annuity.
   * @param type - when payment is made: beginning of period is 1; end, 0.
   * @return <code>double</code> representing periodic payment amount.
   */
  public static double pmt(double r, int nper, double pv, double fv, int type) {
    return (-r * (pv * Math.pow(1 + r, nper) + fv)) / ((1 + r * type) * (Math.pow(1 + r, nper) - 1));
  }

  /**
   * Overloaded pmt() call omitting type, which defaults to 0.
   *
   * @see #pmt(double, int, double, double, int)
   */
  public static double pmt(double r, int nper, double pv, double fv) {
    return pmt(r, nper, pv, fv, 0);
  }

  /**
   * Overloaded pmt() call omitting fv and type, which both default to 0.
   *
   * @see #pmt(double, int, double, double, int)
   */
  public static double pmt(double r, int nper, double pv) {
    return pmt(r, nper, pv, 0);
  }

  public static BigDecimal pmt(BigDecimal intRate, int nper, BigDecimal pv, BigDecimal fv) {
    return pmt(intRate, nper, pv, fv, 0);
  }

  public static BigDecimal pmt(BigDecimal intRate, int nper, BigDecimal pv) {
    return pmt(intRate, nper, pv, BigDecimal.ZERO, 0);
  }

  /* An alternative, extremely precise way */
  public static BigDecimal pmt(BigDecimal intRate, int nper, BigDecimal pv, BigDecimal fv, int type) {
    BigDecimal numerator = intRate.multiply(((pv.multiply(ONE.add(intRate).pow(nper))).add(fv)));
    BigDecimal denominator = (ONE.add(intRate.multiply(BigDecimal.valueOf(type)))).multiply(((ONE.add(intRate)).pow(nper)).subtract(ONE));
    return numerator.divide(denominator, 9, RoundingMode.HALF_UP).negate();
  }

  /**
   * @param monthlyIrr the MONTHLY internal rate of return (monthly irr)
   * @return the annual percentage rate (effective interest)
   */
  public static double apr(double monthlyIrr) {
    //=(( (1+(irr)) ^12)-1)
    int n = 12;
    return Math.pow((1 + monthlyIrr), n) - 1;
  }

  /**
   *  r = (1 + i/n)^n - 1
   * r represents the effective interest rate,
   * i represents the YEARLY internal rate of return (irr) see InternalRateOfReturn,
   * If you have montly irr you can just do irr*12 to feed this method
   * n represents the number of compounding periods per year.
   *
   */
  public static double effectiveInterestRate(double yearlyIrr) {
    int n = 12;
    return Math.pow((1 + yearlyIrr/n), n) - 1;
  }

  /**
   * @param loanAmt *INCLUDING* startupfee
   * @param tenureMonths the tenure in months
   * @param amortizationFreeMonths number of amortization free months
   * @param interest then nominal yearly interest
   * @param statementFee invoice fee
   * @return the effective interest rate
   */
  public static double effectiveInterestRate(int loanAmt, BigDecimal interest, int tenureMonths, int amortizationFreeMonths, Integer statementFee) {
    //List<Payment> paymentPlanList = Cashflow.calculatePaymentPlan(loanAmt, tenureMonths, amortizationFreeMonths, interest, BigDecimal.valueOf(statementFee));
    double[] cashFlow = Cashflow.cashFlow(loanAmt, interest, tenureMonths, amortizationFreeMonths, statementFee);
    double irr = InternalRateOfReturn.irr(cashFlow);
    return LoanCalculator.apr(irr);
  }

  public static int nz(Integer val) {
    return val == null ? 0 : val;
  }

  public static double nz(Double val) {
    return val == null ? 0 : val;
  }

  public static double nz(BigDecimal val) {
    return val == null ? 0 : val.doubleValue();
  }

  public static long nz(Long val) {
    return val == null ? 0 : val;
  }

  public static long nz(BigInteger val) {
    return val == null ? 0 : Math.abs(val.longValue());
  }
}
