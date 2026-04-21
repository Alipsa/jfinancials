package se.alipsa.jfinancials;

import java.math.BigDecimal;
import java.util.List;

/** Represents a single payment period in a {@link PaymentPlan}. */
public class Payment {

  /**
   * The ordered list of field names corresponding to the column indices used in {@link #get(int)}.
   */
  public static final List<String> columnNames =
      List.of(
          "month",
          "costOfCredit",
          "interestAmt",
          "amortization",
          "invoiceFee",
          "outgoingBalance",
          "cashFlow");

  private int month;
  private BigDecimal costOfCredit = BigDecimal.ZERO;
  private BigDecimal interestAmt = BigDecimal.ZERO;
  private BigDecimal amortization = BigDecimal.ZERO;
  private BigDecimal invoiceFee = BigDecimal.ZERO;
  private BigDecimal outgoingBalance = BigDecimal.ZERO;
  private BigDecimal cashFlow = BigDecimal.ZERO;

  /** Creates a new Payment with all monetary fields initialised to zero. */
  public Payment() {}

  /**
   * Returns the payment period number (1-based; period 0 represents the initial loan disbursement).
   *
   * @return the month number
   */
  public int getMonth() {
    return month;
  }

  /**
   * Sets the payment period number.
   *
   * @param month the month number (1-based)
   */
  public void setMonth(int month) {
    this.month = month;
  }

  /**
   * Returns the total cost of credit for this period (interest plus amortization).
   *
   * @return the cost of credit
   */
  public BigDecimal getCostOfCredit() {
    return costOfCredit;
  }

  /**
   * Sets the total cost of credit for this period.
   *
   * @param costOfCredit the cost of credit
   */
  public void setCostOfCredit(BigDecimal costOfCredit) {
    this.costOfCredit = costOfCredit;
  }

  /**
   * Returns the interest portion of this period's payment.
   *
   * @return the interest amount
   */
  public BigDecimal getInterestAmt() {
    return interestAmt;
  }

  /**
   * Sets the interest portion of this period's payment.
   *
   * @param interestAmt the interest amount
   */
  public void setInterestAmt(BigDecimal interestAmt) {
    this.interestAmt = interestAmt;
  }

  /**
   * Returns the principal repayment (amortization) for this period.
   *
   * @return the amortization amount
   */
  public BigDecimal getAmortization() {
    return amortization;
  }

  /**
   * Sets the principal repayment for this period.
   *
   * @param amortization the amortization amount
   */
  public void setAmortization(BigDecimal amortization) {
    this.amortization = amortization;
  }

  /**
   * Returns the per-period invoice fee charged to the borrower.
   *
   * @return the invoice fee
   */
  public BigDecimal getInvoiceFee() {
    return invoiceFee;
  }

  /**
   * Sets the per-period invoice fee.
   *
   * @param invoiceFee the invoice fee
   */
  public void setInvoiceFee(BigDecimal invoiceFee) {
    this.invoiceFee = invoiceFee;
  }

  /**
   * Returns the remaining loan balance after this period's payment.
   *
   * @return the outgoing balance
   */
  public BigDecimal getOutgoingBalance() {
    return outgoingBalance;
  }

  /**
   * Sets the remaining loan balance after this period's payment.
   *
   * @param outgoingBalance the outgoing balance
   */
  public void setOutgoingBalance(BigDecimal outgoingBalance) {
    this.outgoingBalance = outgoingBalance;
  }

  /**
   * Returns the net cash flow for this period (cost of credit plus invoice fee).
   *
   * @return the cash flow
   */
  public BigDecimal getCashFlow() {
    return cashFlow;
  }

  /**
   * Sets the net cash flow for this period.
   *
   * @param cashFlow the cash flow
   */
  public void setCashFlow(BigDecimal cashFlow) {
    this.cashFlow = cashFlow;
  }

  /**
   * Returns a field value by its column index as defined in {@link #columnNames}.
   *
   * @param index the zero-based column index (0 = month, 1 = costOfCredit, ..., 6 = cashFlow)
   * @return the field value as a {@link Number}
   * @throws IndexOutOfBoundsException if {@code index} is outside the range [0, 6]
   */
  public Number get(int index) {
    return switch (index) {
      case 0 -> month;
      case 1 -> costOfCredit;
      case 2 -> interestAmt;
      case 3 -> amortization;
      case 4 -> invoiceFee;
      case 5 -> outgoingBalance;
      case 6 -> cashFlow;
      default -> throw new IndexOutOfBoundsException("No column at index " + index);
    };
  }

  /**
   * Returns a field value by its column name as defined in {@link #columnNames}.
   *
   * @param name the column name
   * @return the field value as a {@link Number}
   */
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
    sb.append(", cashFlow=").append(cashFlow);
    sb.append('}');
    return sb.toString();
  }
}
