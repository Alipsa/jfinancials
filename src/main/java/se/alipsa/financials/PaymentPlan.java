package se.alipsa.financials;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Payment plan is a List of Payments with some additional sugar making it almost
 * like a Matrix or at least simple to create a Matrix from using
 * Matrix.builder().rows(myPp.rowList()).columnNames(myPp.getColumnNames()).build()
 */
public class PaymentPlan extends ArrayList<Payment> {

  private static final List<String> columnNames = Payment.columnNames;
  /**
   * Default constructor
   */
  public PaymentPlan() {
  }

  /**
   * Create anew PaymentPlan from an existing one
   *
   * @param payments the existing PaymentPlan
   */
  public PaymentPlan(PaymentPlan payments) {
    super(payments);
  }

  /**
   * create an empty payment pln with the capacity specified (it can still grow beyond that if needed)
   *
   * @param initialCapacity the estimated size of the plan
   */
  public PaymentPlan(int initialCapacity) {
    super(initialCapacity);
  }

  /**
   * Transform the list of Payment into List of List of values
   *
   * @return a List of List of Number
   */
  public List<List<Number>> toRowList() {
    List<List<Number>> rows = new ArrayList<>();
    for (Payment p : this) {
      List<Number> row = new ArrayList<>();
      row.add(p.getMonth());
      row.add(p.getCostOfCredit());
      row.add(p.getInterestAmt());
      row.add(p.getAmortization());
      row.add(p.getInvoiceFee());
      row.add(p.getOutgoingBalance());
      row.add(p.getCacheFlow());
      rows.add(row);
    }
    return rows;
  }

  /**
   * return the column names (Payment attribute names)
   *
   * @return a list of the headers (names of each 'column')
   */
  public List<String> getColumnNames() {
    return columnNames;
  }



  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.join(",", getColumnNames())).append("\n");
    toRowList().forEach(row ->
      sb.append(row.stream()
          .map(v -> String.valueOf(v instanceof BigDecimal ? ((BigDecimal) v).setScale(2, RoundingMode.HALF_UP) : v))
          .collect(Collectors.joining(","))
      ).append("\n")
    );
    return sb.toString();
  }

  public List<Number> getColumn(String name) {
    List<Number> col = new ArrayList<>();
    for (Payment p : this) {
      col.add(p.get(name));
    }
    return col;
  }
}
