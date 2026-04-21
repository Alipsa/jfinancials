package financials;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import se.alipsa.jfinancials.Payment;

import java.math.BigDecimal;

public class PaymentTest {

  @Test
  void testGetByIndex() {
    Payment p = new Payment();
    p.setMonth(1);
    p.setCostOfCredit(BigDecimal.valueOf(100));
    p.setInterestAmt(BigDecimal.valueOf(10));
    p.setAmortization(BigDecimal.valueOf(90));
    p.setInvoiceFee(BigDecimal.valueOf(30));
    p.setOutgoingBalance(BigDecimal.valueOf(910));
    p.setCashFlow(BigDecimal.valueOf(130));

    assertEquals(1, p.get(0), "month");
    assertEquals(BigDecimal.valueOf(100), p.get(1), "costOfCredit");
    assertEquals(BigDecimal.valueOf(10), p.get(2), "interestAmt");
    assertEquals(BigDecimal.valueOf(90), p.get(3), "amortization");
    assertEquals(BigDecimal.valueOf(30), p.get(4), "invoiceFee");
    assertEquals(BigDecimal.valueOf(910), p.get(5), "outgoingBalance");
    assertEquals(BigDecimal.valueOf(130), p.get(6), "cashFlow");
  }

  @Test
  void testGetByName() {
    Payment p = new Payment();
    p.setMonth(3);
    p.setCostOfCredit(BigDecimal.valueOf(500));
    assertEquals(3, p.get("month"));
    assertEquals(BigDecimal.valueOf(500), p.get("costOfCredit"));
  }

  @Test
  void testGetByIndexOutOfBounds() {
    Payment p = new Payment();
    assertThrows(IndexOutOfBoundsException.class, () -> p.get(7));
    assertThrows(IndexOutOfBoundsException.class, () -> p.get(-1));
  }
}
