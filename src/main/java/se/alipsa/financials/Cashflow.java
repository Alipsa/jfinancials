package se.alipsa.financials;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Cashflow {
  // Empirical tests comparing with Excel gives 9 as the lowest possible value for 0.01 error margin
  private static final int SCALE = 9;

  /**
   * Utility class, only static methods
   *
   */
  private Cashflow() {
  }

  /**
   *
   * @param loanAmount including startupFee
   * @return a List of Payment
   */
  public static PaymentPlan paymentPlan(
      int loanAmount,
      BigDecimal interest,
      int tenureMonths,
      int amFreeMonths,
      BigDecimal invoiceFee) {
    PaymentPlan paymentPlan = new PaymentPlan(tenureMonths + 1);
    BigDecimal interestCostAmFreePeriod = BigDecimal.valueOf(loanAmount).multiply(interest).divide(BigDecimal.valueOf(12), SCALE, RoundingMode.HALF_UP);
    BigDecimal monthlyAnnuity = BigDecimal.valueOf(LoanCalculator.monthlyAnnuityAmount(loanAmount, interest.doubleValue(), tenureMonths, amFreeMonths));
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
    double monthlyAnnuity = LoanCalculator.monthlyAnnuityAmount(loanAmount, interest.doubleValue(), tenureMonths, amFreeMonths);
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
}
