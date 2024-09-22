package se.alipsa.financials;

import java.math.BigDecimal;

public class Payment {
  private int month;
  private BigDecimal costOfCredit;
  private BigDecimal interestAmt;
  private BigDecimal amortization;
  private BigDecimal invoiceFee;
  private BigDecimal outgoingBalance;
  private BigDecimal cacheFlow;

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
}
