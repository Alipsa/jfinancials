package se.alipsa.jfinancials;

import static java.math.BigDecimal.ONE;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Utility class providing financial calculation methods for loans and investments. */
public class Financials {
  // Empirical tests comparing with Excel gives 9 as the lowest possible value for 0.01 error margin
  private static final int SCALE = 9;

  /** Number of iterations used by the bisection IRR solver. */
  public static final int MAX_ITERATIONS = 1000;

  /** Minimum difference used as the convergence criterion in the IRR solver. */
  public static final double MIN_DIFF = 1E-7;

  private Financials() {}

  /**
   * Create a PaymentPlan which essentially is a List of payments with some additional syntactic
   * sugar.
   *
   * @param loanAmount including startupFee
   * @param interest the yearly interest rate
   * @param tenureMonths the number of periods (usually months)
   * @param amFreeMonths the number of amortization free months before amortization of the loan
   *     starts
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
    BigDecimal interestCostAmFreePeriod =
        BigDecimal.valueOf(loanAmount)
            .multiply(interest)
            .divide(BigDecimal.valueOf(12), SCALE, RoundingMode.HALF_UP);
    BigDecimal monthlyAnnuity =
        BigDecimal.valueOf(
            monthlyAnnuityAmount(loanAmount, interest.doubleValue(), tenureMonths, amFreeMonths));
    Payment p = new Payment();
    p.setOutgoingBalance(BigDecimal.valueOf(loanAmount));
    p.setCashFlow(BigDecimal.valueOf((long) loanAmount * -1));
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
      p.setInterestAmt(
          prev.getOutgoingBalance()
              .multiply(interest.divide(BigDecimal.valueOf(12), SCALE, RoundingMode.HALF_UP)));
      p.setAmortization(p.getCostOfCredit().subtract(p.getInterestAmt()));
      p.setInvoiceFee(invoiceFee);
      p.setOutgoingBalance(prev.getOutgoingBalance().subtract(p.getAmortization()));
      p.setCashFlow(p.getCostOfCredit().add(p.getInvoiceFee()));
      paymentPlan.add(p);
    }
    return paymentPlan;
  }

  /**
   * Extracts the cash flow values from an existing payment plan as a primitive array.
   *
   * @param paymentPlan the list of payments to extract from
   * @return an array of cash flow values, one per payment period
   */
  public static double[] cashFlow(List<Payment> paymentPlan) {
    double[] cashFlow = new double[paymentPlan.size()];
    for (int i = 0; i < cashFlow.length; i++) {
      cashFlow[i] = paymentPlan.get(i).getCashFlow().doubleValue();
    }
    return cashFlow;
  }

  /**
   * Calculates the cash flow array for a loan from its parameters.
   *
   * @param loanAmount the total loan amount including capitalized fees
   * @param interest the annual nominal interest rate
   * @param tenureMonths the total tenure of the loan in months
   * @param amFreeMonths the number of initial amortization-free months
   * @param invoiceFee the per-period invoice fee charged to the borrower
   * @return an array of cash flow values starting with the (negative) loan disbursement
   */
  public static double[] cashFlow(
      int loanAmount, BigDecimal interest, int tenureMonths, int amFreeMonths, Integer invoiceFee) {
    double interestCostAmFreePeriod = loanAmount * interest.doubleValue() / 12;
    double monthlyAnnuity =
        monthlyAnnuityAmount(loanAmount, interest.doubleValue(), tenureMonths, amFreeMonths);
    List<Double> p = new ArrayList<>(tenureMonths + 1);
    p.add(loanAmount * -1.0);
    for (int month = 1; month <= tenureMonths; month++) {
      double costOfCredit;
      if (amFreeMonths >= month) {
        costOfCredit = interestCostAmFreePeriod;
      } else {
        costOfCredit = monthlyAnnuity;
      }
      double cashFlow = costOfCredit + invoiceFee.doubleValue();
      p.add(cashFlow);
    }
    double[] cashFlows = new double[p.size()];
    for (int i = 0; i < cashFlows.length; i++) {
      cashFlows[i] = p.get(i);
    }
    return cashFlows;
  }

  /**
   * Calculates the internal rate of return for a payment plan.
   *
   * @param paymentPlan the payment plan whose cash flows are used
   * @return the monthly internal rate of return
   */
  public static double irr(PaymentPlan paymentPlan) {
    double[] cashFlows = new double[paymentPlan.size()];
    AtomicInteger i = new AtomicInteger(0);
    paymentPlan.forEach(p -> cashFlows[i.getAndIncrement()] = p.getCashFlow().doubleValue());
    return irr(cashFlows);
  }

  /**
   * This is a "brute force" way of calculating irr. It is quite cpu intensive, but we do not have
   * that many compounds, so typically it takes only 1-2 milliseconds to complete, so I did not go
   * further with using commons math to use one of the deterministic solvers (e.g. the Brent
   * solver). Newton-Raphson is fast but non-deterministic so cannot be used without a fallback.
   * Excel uses that but have some secret fallback which is unknown (it is closed source) to get a
   * deterministic outcome.
   *
   * @param cashFlow money flow
   * @return yield
   */
  public static double irr(double[] cashFlow) {
    double flowOut = cashFlow[0];
    double minValue = 0d;
    double maxValue = 1d;
    double testValue = 0d;
    int iterations = MAX_ITERATIONS;

    while (iterations > 0) {
      testValue = (minValue + maxValue) / 2;
      double npv = cfNpv(cashFlow, testValue);
      if (Math.abs(flowOut + npv) < MIN_DIFF) {
        break;
      } else if (Math.abs(flowOut) > npv) {
        maxValue = testValue;
      } else {
        minValue = testValue;
      }
      iterations--;
    }
    return testValue;
  }

  /**
   * Calculates the internal rate of return from a list of cash flow values.
   *
   * @param cashFlowCol the cash flow values; the first entry must be the (negative) initial outflow
   * @return the monthly internal rate of return
   */
  public static double irr(List<Number> cashFlowCol) {
    return irr(toDoubleArray(cashFlowCol));
  }

  private static double[] toDoubleArray(List<Number> cashFlowCol) {
    double[] cashFlows = new double[cashFlowCol.size()];
    AtomicInteger i = new AtomicInteger(0);
    cashFlowCol.forEach(p -> cashFlows[i.getAndIncrement()] = p.doubleValue());
    return cashFlows;
  }

  /*
   * npv except the first entry, used in the irr calculation
   */
  private static double cfNpv(double[] cashFlow, double rate) {
    double npv = 0;
    for (int i = 1; i < cashFlow.length; i++) {
      npv += cashFlow[i] / Math.pow(1 + rate, i);
    }
    return npv;
  }

  /**
   * Calculates the net present value of a series of cash flows.
   *
   * <p>Equivalent to R's {@code npv = function(i, cf, t=seq(along=cf)) sum(cf/(1+i)^t)}.
   *
   * @param cashFlowCol the list of cash flow values
   * @param rate the discount rate per period
   * @return the net present value
   */
  public static double npv(List<Number> cashFlowCol, double rate) {
    double cfs = 0;
    int t = 1;
    for (Number cf : cashFlowCol) {
      cfs += cf.doubleValue() / Math.pow(1 + rate, t++);
    }
    return cfs;
  }

  /**
   * Calculates the net present value of a series of cash flows.
   *
   * <p>Equivalent to R's {@code npv = function(i, cf, t=seq(along=cf)) sum(cf/(1+i)^t)}.
   *
   * @param cashFlow an array of cash flow values
   * @param rate the discount rate per period
   * @return the net present value
   */
  public static double npv(double[] cashFlow, double rate) {
    double cfs = 0;
    int t = 1;
    for (Number cf : cashFlow) {
      cfs += cf.doubleValue() / Math.pow(1 + rate, t++);
    }
    return cfs;
  }

  /**
   * Calculates the total amount paid over the life of a loan.
   *
   * @param loanAmount the total loan amount including capitalized fees
   * @param interestRate the annual nominal interest rate
   * @param tenureMonths the total tenure of the loan in months
   * @param amortizationFreeMonths the number of initial amortization-free months
   * @param statementFee the per-period invoice fee
   * @return the total payment amount
   */
  public static double totalPaymentAmount(
      double loanAmount,
      double interestRate,
      int tenureMonths,
      int amortizationFreeMonths,
      int statementFee) {
    double monthlyAnnuity =
        monthlyAnnuityAmount(loanAmount, interestRate, tenureMonths, amortizationFreeMonths);
    return totalPaymentAmount(
        loanAmount,
        interestRate,
        tenureMonths,
        amortizationFreeMonths,
        statementFee,
        monthlyAnnuity);
  }

  /**
   * Calculates the total amount paid over the life of a loan using a pre-computed monthly annuity,
   * which avoids recalculating it when it is already known.
   *
   * @param loanAmount the total loan amount including capitalized fees
   * @param interestRate the annual nominal interest rate
   * @param tenureMonths the total tenure of the loan in months
   * @param amortizationFreeMonths the number of initial amortization-free months
   * @param statementFee the per-period invoice fee
   * @param monthlyAnnuity the pre-computed monthly annuity amount
   * @return the total payment amount
   */
  public static double totalPaymentAmount(
      double loanAmount,
      double interestRate,
      int tenureMonths,
      int amortizationFreeMonths,
      int statementFee,
      double monthlyAnnuity) {
    double interestCostAmfreePeriod = loanAmount * interestRate / 12;
    return (monthlyAnnuity + statementFee) * tenureMonths
        - (monthlyAnnuity - interestCostAmfreePeriod) * amortizationFreeMonths;
  }

  /**
   * Calculates the total amount paid over the life of a loan, rounded to the specified number of
   * decimal places.
   *
   * @param loanAmount the total loan amount including capitalized fees
   * @param interestRate the annual nominal interest rate
   * @param tenureMonths the total tenure of the loan in months
   * @param amortizationFreeMonths the number of initial amortization-free months
   * @param statementFee the per-period invoice fee
   * @param decimals the number of decimal places to round to
   * @return the total payment amount rounded to the specified scale
   */
  public static BigDecimal totalPaymentAmountRounded(
      double loanAmount,
      BigDecimal interestRate,
      int tenureMonths,
      int amortizationFreeMonths,
      int statementFee,
      int decimals) {
    return BigDecimal.valueOf(
            totalPaymentAmount(
                loanAmount,
                interestRate.doubleValue(),
                tenureMonths,
                amortizationFreeMonths,
                statementFee))
        .setScale(decimals, RoundingMode.HALF_UP);
  }

  /**
   * Calculates the monthly annuity amount for a loan, i.e. the fixed payment covering both interest
   * and principal repayment each month after the amortization-free period.
   *
   * @param loanAmount the loan amount including startup fee
   * @param interestRate the nominal yearly interest rate
   * @param tenureMonths tenure in months
   * @param amortizationFreemonths number of amortization-free months
   * @return the monthly annuity amount
   */
  public static double monthlyAnnuityAmount(
      double loanAmount, double interestRate, int tenureMonths, int amortizationFreemonths) {
    double monthlyInterest = interestRate / 12;
    int totalNumberOfPaymentPeriods = tenureMonths - amortizationFreemonths;
    return pmt(monthlyInterest, totalNumberOfPaymentPeriods, loanAmount * -1);
  }

  /**
   * Calculates the average daily interest amount for a loan.
   *
   * @param loanAmount the total loan amount including capitalized fees
   * @param interestRate the annual nominal interest rate
   * @param tenureMonths the total tenure of the loan in months
   * @param amFreeMonths the number of initial amortization-free months
   * @param statementFee the per-period invoice fee
   * @return the average daily interest amount
   */
  public static double dailyInterestAmount(
      int loanAmount,
      BigDecimal interestRate,
      int tenureMonths,
      int amFreeMonths,
      int statementFee) {
    List<Payment> paymentPlan =
        paymentPlan(
            loanAmount, interestRate, tenureMonths, amFreeMonths, BigDecimal.valueOf(statementFee));
    return dailyInterestAmount(paymentPlan, tenureMonths);
  }

  /**
   * Calculates the average daily interest amount from an existing payment plan.
   *
   * @param paymentPlan the list of payments to sum interest from
   * @param tenureMonths the total tenure of the loan in months
   * @return the average daily interest amount
   */
  public static double dailyInterestAmount(List<Payment> paymentPlan, int tenureMonths) {
    double totalInterest = paymentPlan.stream().mapToDouble(p -> nz(p.getInterestAmt())).sum();
    // 30.41666 is from Konsumentverkets guidelines
    // https://www.konsumentverket.se/globalassets/publikationer/produkter-och-tjanster/finansiella-tjanster/kovfs-2011-01-allmanna-rad-konsumentkrediter-v3--konsumentverket.pdf
    return totalInterest / (tenureMonths * 30.41666);
  }

  /**
   * Emulates Excel/Calc's PMT(interest_rate, number_payments, PV, FV, Type) function, which
   * calculates the payments for a loan or the future value of an investment.
   *
   * @param r - periodic interest rate represented as a decimal.
   * @param nper - number of total payments / periods.
   * @param pv - present value -- borrowed or invested principal.
   * @param fv - future value of loan or annuity.
   * @param type - when payment is made: beginning of period is 1; end, 0.
   * @return <code>double</code> representing periodic payment amount.
   */
  public static double pmt(double r, int nper, double pv, double fv, int type) {
    return (-r * (pv * Math.pow(1 + r, nper) + fv))
        / ((1 + r * type) * (Math.pow(1 + r, nper) - 1));
  }

  /**
   * Overloaded pmt() call omitting type, which defaults to 0.
   *
   * @param r periodic interest rate represented as a decimal
   * @param nper number of total payments / periods
   * @param pv present value -- borrowed or invested principal
   * @param fv future value of loan or annuity
   * @return double representing periodic payment amount
   * @see #pmt(double, int, double, double, int)
   */
  public static double pmt(double r, int nper, double pv, double fv) {
    return pmt(r, nper, pv, fv, 0);
  }

  /**
   * Overloaded pmt() call omitting fv and type, which both default to 0.
   *
   * @param r periodic interest rate represented as a decimal
   * @param nper number of total payments / periods
   * @param pv present value -- borrowed or invested principal
   * @return double representing periodic payment amount
   * @see #pmt(double, int, double, double, int)
   */
  public static double pmt(double r, int nper, double pv) {
    return pmt(r, nper, pv, 0);
  }

  /**
   * High-precision PMT using {@link BigDecimal} arithmetic, omitting type (defaults to 0).
   *
   * @param intRate periodic interest rate represented as a decimal
   * @param nper number of total payments / periods
   * @param pv present value -- borrowed or invested principal
   * @param fv future value of loan or annuity
   * @return BigDecimal representing the periodic payment amount
   * @see #pmt(BigDecimal, int, BigDecimal, BigDecimal, int)
   */
  public static BigDecimal pmt(BigDecimal intRate, int nper, BigDecimal pv, BigDecimal fv) {
    return pmt(intRate, nper, pv, fv, 0);
  }

  /**
   * High-precision PMT using {@link BigDecimal} arithmetic, omitting fv and type (both default to
   * 0).
   *
   * @param intRate periodic interest rate represented as a decimal
   * @param nper number of total payments / periods
   * @param pv present value -- borrowed or invested principal
   * @return BigDecimal representing the periodic payment amount
   * @see #pmt(BigDecimal, int, BigDecimal, BigDecimal, int)
   */
  public static BigDecimal pmt(BigDecimal intRate, int nper, BigDecimal pv) {
    return pmt(intRate, nper, pv, BigDecimal.ZERO, 0);
  }

  /**
   * High-precision PMT using {@link BigDecimal} arithmetic with scale 9.
   *
   * @param intRate periodic interest rate represented as a decimal
   * @param nper number of total payments / periods
   * @param pv present value -- borrowed or invested principal
   * @param fv future value of loan or annuity
   * @param type when payment is made: beginning of period is 1; end is 0
   * @return BigDecimal representing the periodic payment amount
   */
  public static BigDecimal pmt(
      BigDecimal intRate, int nper, BigDecimal pv, BigDecimal fv, int type) {
    BigDecimal numerator = intRate.multiply(((pv.multiply(ONE.add(intRate).pow(nper))).add(fv)));
    BigDecimal denominator =
        (ONE.add(intRate.multiply(BigDecimal.valueOf(type))))
            .multiply(((ONE.add(intRate)).pow(nper)).subtract(ONE));
    return numerator.divide(denominator, 9, RoundingMode.HALF_UP).negate();
  }

  /**
   * Calculates the annual percentage rate (APR), also known as effective interest, from a monthly
   * IRR.
   *
   * @param monthlyIrr the MONTHLY internal rate of return (monthly irr)
   * @return the annual percentage rate (effective interest)
   */
  public static double apr(double monthlyIrr) {
    // =(( (1+(irr)) ^12)-1)
    int n = 12;
    return Math.pow((1 + monthlyIrr), n) - 1;
  }

  /**
   * Calculates the effective interest rate from an annual IRR using monthly compounding.
   *
   * <p>Formula: {@code r = (1 + i/n)^n - 1} where {@code r} is the effective interest rate, {@code
   * i} is the yearly IRR, and {@code n} is 12 (monthly compounding periods). If you have a monthly
   * IRR you can multiply it by 12 to obtain the yearly value to pass to this method.
   *
   * @param yearlyIrr the YEARLY internal rate of return
   * @return the effective interest rate
   */
  public static double effectiveInterestRate(double yearlyIrr) {
    int n = 12;
    return Math.pow((1 + yearlyIrr / n), n) - 1;
  }

  /**
   * Calculates the effective interest rate for a loan from its parameters.
   *
   * @param loanAmt the loan amount <em>including</em> startup fee
   * @param interest the nominal yearly interest rate
   * @param tenureMonths the tenure in months
   * @param amortizationFreeMonths the number of amortization-free months
   * @param statementFee the per-period invoice fee
   * @return the effective interest rate
   */
  public static double effectiveInterestRate(
      int loanAmt,
      BigDecimal interest,
      int tenureMonths,
      int amortizationFreeMonths,
      Integer statementFee) {
    double[] cashFlow =
        cashFlow(loanAmt, interest, tenureMonths, amortizationFreeMonths, statementFee);
    double irr = irr(cashFlow);
    return apr(irr);
  }

  /**
   * Returns zero if the value is {@code null}, otherwise returns the value itself.
   *
   * @param val an Integer, or {@code null}
   * @return the value, or 0 if {@code null}
   */
  public static int nz(Integer val) {
    return val == null ? 0 : val;
  }

  /**
   * Returns zero if the value is {@code null}, otherwise returns the value itself.
   *
   * @param val a Double, or {@code null}
   * @return the value, or 0 if {@code null}
   */
  public static double nz(Double val) {
    return val == null ? 0 : val;
  }

  /**
   * Returns zero if the value is {@code null}, otherwise returns its {@code double} representation.
   *
   * @param val a BigDecimal, or {@code null}
   * @return the double value, or 0 if {@code null}
   */
  public static double nz(BigDecimal val) {
    return val == null ? 0 : val.doubleValue();
  }

  /**
   * Returns zero if the value is {@code null}, otherwise returns the value itself.
   *
   * @param val a Long, or {@code null}
   * @return the value, or 0 if {@code null}
   */
  public static long nz(Long val) {
    return val == null ? 0 : val;
  }

  /**
   * Returns zero if the value is {@code null}, otherwise returns the absolute value as a {@code
   * long}.
   *
   * @param val a BigInteger, or {@code null}
   * @return the absolute long value, or 0 if {@code null}
   */
  public static long nz(BigInteger val) {
    return val == null ? 0 : Math.abs(val.longValue());
  }
}
