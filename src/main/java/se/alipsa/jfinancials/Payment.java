package se.alipsa.jfinancials;

import java.math.BigDecimal;
import java.util.List;

public class Payment {

  public static final List<String> columnNames = List.of("month", "costOfCredit", "interestAmt", "amortization", "invoiceFee", "outgoingBalance", "cashFlow");

  private int month;
  private BigDecimal costOfCredit = BigDecimal.ZERO;
  private BigDecimal interestAmt = BigDecimal.ZERO;
  private BigDecimal amortization = BigDecimal.ZERO;
  private BigDecimal invoiceFee = BigDecimal.ZERO;
  private BigDecimal outgoingBalance = BigDecimal.ZERO;
  private BigDecimal cacheFlow = BigDecimal.ZERO;

  public int getMonth() {
    return month;
  }

  public void setMonth(int month) {
    this.month = month;
  }

  public BigDecimal getCostOfCredit() {
    return costOfCredit;
  }

  public void setCostOfCredit(BigDecimal costOfCredit) {
    this.costOfCredit = costOfCredit;
  }

  public BigDecimal getInterestAmt() {
    return interestAmt;
  }

  public void setInterestAmt(BigDecimal interestAmt) {
    this.interestAmt = interestAmt;
  }

  public BigDecimal getAmortization() {
    return amortization;
  }

  public void setAmortization(BigDecimal amortization) {
    this.amortization = amortization;
  }

  public BigDecimal getInvoiceFee() {
    return invoiceFee;
  }

  public void setInvoiceFee(BigDecimal invoiceFee) {
    this.invoiceFee = invoiceFee;
  }

  public BigDecimal getOutgoingBalance() {
    return outgoingBalance;
  }

  public void setOutgoingBalance(BigDecimal outgoingBalance) {
    this.outgoingBalance = outgoingBalance;
  }

  public BigDecimal getCacheFlow() {
    return cacheFlow;
  }

  public void setCacheFlow(BigDecimal cacheFlow) {
    this.cacheFlow = cacheFlow;
  }

  public Number get(int index) {
    return switch (index) {
      case 0 -> month;
      case 1 -> costOfCredit;
      case 2 -> interestAmt;
      case 3 -> amortization;
      case 4 -> invoiceFee;
      case 5 -> outgoingBalance;
      case 6 -> cacheFlow;
      default -> null;
    };
  }

  public Number get(String name) {
    return get(columnNames.indexOf(name));
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Payment{");
    sb.append("month=").append(month);
    sb.append(", costOfCredit=").append(costOfCredit);
    sb.append(", interestAmt=").append(interestAmt);
    sb.append(", amortization=").append(amortization);
    sb.append(", invoiceFee=").append(invoiceFee);
    sb.append(", outgoingBalance=").append(outgoingBalance);
    sb.append(", cacheFlow=").append(cacheFlow);
    sb.append('}');
    return sb.toString();
  }
}
