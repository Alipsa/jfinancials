package se.alipsa.jfinancials;

import static java.math.BigDecimal.ONE;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Financials {
  // Empirical tests comparing with Excel gives 9 as the lowest possible value for 0.01 error margin
  private static final int SCALE = 9;
  /**Number of iterations*/
  public static final int MAX_ITERATIONS=1000;

  /**Minimum difference*/
  public static final double MIN_DIFF=1E-7;

  private Financials() {}

  /**
   * Create a PaymentPlan which essentially is a List of payments with some additional syntactic sugar
   *
   * @param loanAmount including startupFee
   * @param interest the yearly interest rate
   * @param tenureMonths the number of periods (usually months)
   * @param amFreeMonths the number of amortization free months before amortization of the loan starts
   * @param invoiceFee the cost of each period invoicing affecting the borrower
   * @return a PaymentPlan which essentially is a List of payments
   */
  public static PaymentPlan paymentPlan(
      int loanAmount,
      BigDecimal interest,
      int tenureMonths,
      int amFreeMonths,
      BigDecimal invoiceFee) {
    PaymentPlan paymentPlan = new PaymentPlan(tenureMonths + 1);
    BigDecimal interestCostAmFreePeriod = BigDecimal.valueOf(loanAmount).multiply(interest).divide(BigDecimal.valueOf(12), SCALE, RoundingMode.HALF_UP);
    BigDecimal monthlyAnnuity = BigDecimal.valueOf(monthlyAnnuityAmount(loanAmount, interest.doubleValue(), tenureMonths, amFreeMonths));
    Payment p = new Payment();
    p.setOutgoingBalance(BigDecimal.valueOf(loanAmount));
    p.setCacheFlow(BigDecimal.valueOf((long) loanAmount * -1));
    paymentPlan.add(p);
    for (int month = 1; month <= tenureMonths; month++) {
      Payment prev = paymentPlan.get(month - 1);
      p = new Payment();
      p.setMonth(month);
      if (amFreeMonths >= month) {
        p.setCostOfCredit(interestCostAmFreePeriod);
      } else {
        p.setCostOfCredit(monthlyAnnuity);
      }
      p.setInterestAmt(prev.getOutgoingBalance().multiply(interest.divide(BigDecimal.valueOf(12), SCALE, RoundingMode.HALF_UP)));
      p.setAmortization(p.getCostOfCredit().subtract(p.getInterestAmt()));
      p.setInvoiceFee(invoiceFee);
      p.setOutgoingBalance(prev.getOutgoingBalance().subtract(p.getAmortization()));
      p.setCacheFlow(p.getCostOfCredit().add(p.getInvoiceFee()));
      paymentPlan.add(p);
    }
    return paymentPlan;
  }

  public static double[] cashFlow(List<Payment> paymentPlan) {
    double[] cashFlow = new double[paymentPlan.size()];
    for (int i = 0; i < cashFlow.length; i++) {
      cashFlow[i] = paymentPlan.get(i).getCacheFlow().doubleValue();
    }
    return cashFlow;
  }

  public static double[] cashFlow(int loanAmount,
                                  BigDecimal interest,
                                  int tenureMonths,
                                  int amFreeMonths,
                                  Integer invoiceFee) {
    double interestCostAmFreePeriod = loanAmount * interest.doubleValue() / 12;
    double monthlyAnnuity = monthlyAnnuityAmount(loanAmount, interest.doubleValue(), tenureMonths, amFreeMonths);
    List<Double> p = new ArrayList<>(tenureMonths + 1);
    p.add(loanAmount * -1.0);
    for (int month = 1; month <= tenureMonths; month++) {
      double costOfCredit;
      if (amFreeMonths >= month) {
        costOfCredit = interestCostAmFreePeriod;
      } else {
        costOfCredit = monthlyAnnuity;
      }
      double cacheFlow = costOfCredit + invoiceFee.doubleValue();
      p.add(cacheFlow);
    }
    double[] cashFlows = new double[p.size()];
    for (int i = 0; i < cashFlows.length; i++) {
      cashFlows[i] = p.get(i);
    }
    return cashFlows;
  }

  public static double irr(PaymentPlan paymentPlan) {
    double[] cashFlows = new double[paymentPlan.size()];
    AtomicInteger i = new AtomicInteger(0);
    paymentPlan.forEach(
        p -> cashFlows[i.getAndIncrement()] = p.getCacheFlow().doubleValue()
    );
    return irr(cashFlows);
  }


  /**
   * This is a "brute force" way of calculating irr.
   * It is quite cpu intensive, but we do not have that many compounds, so typically it takes only
   * 1-2 milliseconds to complete, so I did not go further with using commons math to use one of the
   * deterministic solvers (e.g. the Brent solver).
   * Newton-Raphson is fast but non-deterministic so cannot be used without a fallback. Excel uses that but have some
   * secret fallback which is unknown (it is closed source) to get a deterministic outcome.
   *
   * @param cashFlow money flow
   * @return yield
   */
  public static double irr(double[] cashFlow){
    double flowOut = cashFlow[0];
    double minValue = 0d;
    double maxValue = 1d;
    double testValue = 0d;
    int iterations = MAX_ITERATIONS;

    while ( iterations > 0 ) {
      testValue = (minValue+maxValue) / 2;
      double npv= cfNpv(cashFlow,testValue);
      if ( Math.abs(flowOut+npv) < MIN_DIFF){
        break;
      } else if(Math.abs(flowOut) > npv){
        maxValue = testValue;
      } else {
        minValue = testValue;
      }
      iterations--;
    }
    return testValue;
  }

  public static double irr(List<Number> cashFlowCol){
    return irr(toDoubleArray(cashFlowCol));
  }

  private static double[] toDoubleArray(List<Number> cashFlowCol) {
    double[] cashFlows = new double[cashFlowCol.size()];
    AtomicInteger i = new AtomicInteger(0);
    cashFlowCol.forEach(p -> cashFlows[i.getAndIncrement()] = p.doubleValue() );
    return cashFlows;
  }

  /*
   * npv except the first entry, used in the irr calculation
   */
  private static double cfNpv(double[] cashFlow, double rate){
    double npv=0;
    for(int i=1; i < cashFlow.length; i++){
      npv += cashFlow[i] / Math.pow(1+rate, i);
    }
    return npv;
  }

  /**
   * npv = function(i, cf, t=seq(along=cf)) sum(cf/(1+i)^t)
   *
   * @param cashFlowCol
   * @param rate
   * @return
   */
  public static double npv(List<Number> cashFlowCol, double rate){
    double cfs = 0;
    int t = 1;
    for(Number cf : cashFlowCol) {
      cfs += cf.doubleValue() / Math.pow(1+rate, t++);
    }
    return cfs;
  }

  /**
   * npv = function(i, cf, t=seq(along=cf)) sum(cf/(1+i)^t)
   *
   * @param cashFlow an array of double
   * @param rate the interest rate
   * @return
   */
  public static double npv(double[] cashFlow, double rate){
    double cfs = 0;
    int t = 1;
    for(Number cf : cashFlow) {
      cfs += cf.doubleValue() / Math.pow(1+rate, t++);
    }
    return cfs;
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

  public static BigDecimal totalPaymentAmountRounded(double loanAmount, BigDecimal interestRate, int tenureMonths, int amortizationFreeMonths, int statementFee, int decimals) {
    return BigDecimal.valueOf(
        totalPaymentAmount(loanAmount, interestRate.doubleValue(), tenureMonths, amortizationFreeMonths, statementFee)
        ).setScale(decimals, RoundingMode.HALF_UP);
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
    List<Payment> paymentPlan = paymentPlan(loanAmount, interestRate, tenureMonths, amFreeMonths, BigDecimal.valueOf(statementFee));
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
    double[] cashFlow = cashFlow(loanAmt, interest, tenureMonths, amortizationFreeMonths, statementFee);
    double irr = irr(cashFlow);
    return apr(irr);
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
