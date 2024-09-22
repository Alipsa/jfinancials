package se.alipsa.financials;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class InternalRateOfReturn {
  /**Number of iterations*/
  public static final int MAX_ITERATIONS=1000;

  /**Minimum difference*/
  public static final double MIN_DIFF=1E-7;

  /**
   * Utility class, only static methods
   *
   */
  private InternalRateOfReturn() {
  }

  public static double irr(List<Payment> paymentPlans) {
    double[] cashFlows = new double[paymentPlans.size()];
    AtomicInteger i = new AtomicInteger(0);
    paymentPlans.forEach(
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
      double npv= npv(cashFlow,testValue);
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

  public static double npv(double[] cashFlow, double rate){
    double npv=0;
    for(int i=1; i < cashFlow.length; i++){
      npv += cashFlow[i] / Math.pow(1+rate, i);
    }
    return npv;
  }

}
