# jfinancials
Java library for working with financial data. Based on the [financials R package](https://github.com/Alipsa/financials).

To use it add the following dependency to your pom

```xml
<dependency>
    <groupId>se.alipsa</groupId>
    <artifactId>jfinancials</artifactId>
    <version>1.0.0</version>
</dependency>
```
All methods of the public api are in the se.alipsa.jfinancials.Financials class

### Payment
`double pmt(interestRate, nper, pv, fv = 0, type = 0)`

Equivalent to Excel/Calc's PMT(interest_rate, number_payments, PV, FV, Type)
function, which calculates the payments for a loan or the future value of an investment
#### Parameters
- _interestRate_ periodic interest rate represented as a decimal.
- _nper_ number of total payments / periods.
- _pv_   present value -- borrowed or invested principal.
- _fv_   future value of loan or annuity, default to 0 (which is what you want for loans)
- _type_ when payment is made: beginning of period is 1; end is 0. Default is 0

#### Value
_returns_ A double representing the periodic payment amount.

#### Example
__Calulate payment for a loan__

The following data:

| Item            | amount  |
|-----------------|---------|
| Loan Amount	    | 50000   |
| Interest rate	  | 3.50%   |
| Periods	        | 60      |
| Monthly payment | 	909.59 |

Then the Monthly payment can be calculated as
`pmt(3.5/100, 60, -50000)` ≈ 2004.43

### Monthly Annuity Amount
`monthlyAnnuityAmount(loanAmount, interestRate, tenureMonths, amortizationFreemonths = 0, type = 0)`

Calculate the monthly annuity amount i.e. the amortization and interest amount each payment period (month)

#### Parameters
- _loanAmount_ the total loan amount including capitalized fees (e.g. startup fee)
- _interestRate_ the annual nominal interest
- _tenureMonths_ the tenure of the loan in number of months
- _amortizationFreeMonths_ the number of initial amortization free months, default to 0
- _type_ - when payment is made: beginning of period is 1; end is 0. Default is 0

#### Value
_returns_ the monthly annuity amount

#### Example
Assuming the following:

| Item                     | amount |
|--------------------------|--------|
| Loan Amount	             | 50000  |
| Interest rate	           | 3.50%  |
| Tenure	                  | 60     |
| Amortization Free months | 	6     |

The monthly annuity amount would be
`monthlyAnnuityAmount(50000, 3.5/100, 60, 6)` == 1002.10

### Cash Flow
`cashFlow(loanAmount, interestRate, tenureMonths, amortizationFreeMonths, invoiceFee)`

#### Parameters
- _loanAmount_ the total loan amount including capitalized fees (e.g. startup fee)
- _interestRate_ the annual nominal interest
- _tenureYears_ the tenure of the loan in number of years
- _amortizationFreeMonths_ the number of initial amortization free months, default to 0
- _invoiceFee_ a fee for each statement invoiced, default to 0

#### Value
_returns_ an array of doubles of cachFlow entries for each period

#### Example
```r
var cf = Financials.cashFlow(50000, 0.035, 5, 6, 30)
```

### Payment Plan
`paymentPlan(loanAmount, interestRate, tenureMonths, amortizationFreeMonths = 0, invoiceFee = 0)`

#### Parameters
- _loanAmount_ the total loan amount including capitalized fees (e.g. startup fee)
- _interestRate_ the annual nominal interest
- _tenureMonths_ the total tenure of the loan in number of months
- _amortizationFreeMonths_ the number of initial amortization free months, default to 0
- _invoiceFee_ a fee for each statement invoiced, default to 0

#### Value
_returns_ a List of Payment with the initial payment plan based on the input, based on monthly payment periods

#### Example

```groovy
var loanAmt = 10000;
var tenureYears = (int)(1.5 * 12);
var amortizationFreeMonths = 6;
var interest = BigDecimal.valueOf(3.5 / 100);
var invoiceFee = BigDecimal.valueOf(30);

var paymentPlan = Financials.paymentPlan(loanAmt, interest, tenureYears, amortizationFreeMonths, invoiceFee);
System.out.println(paymentPlan);
```

which will the following output:

| month	 | costOfCredit | interestAmt | amortization | invoiceFee | outgoingBalance | cashFlow  |
|--------|--------------|-------------|--------------|------------|-----------------|-----------|
| 0      | 0.00         | 0.00        | 0.00         | 0.00       | 10000.00        | -10000.00 |
| 1      | 29.17        | 29.17       | 0.00         | 30.00      | 10000.00        | 59.17     |
| 2      | 29.17        | 29.17       | 0.00         | 30.00      | 10000.00        | 59.17     |
| 3      | 29.17        | 29.17       | 0.00         | 30.00      | 10000.00        | 59.17     |
| 4      | 29.17        | 29.17       | 0.00         | 30.00      | 10000.00        | 59.17     |
| 5      | 29.17        | 29.17       | 0.00         | 30.00      | 10000.00        | 59.17     |
| 6      | 29.17        | 29.17       | 0.00         | 30.00      | 10000.00        | 59.17     |
| 7      | 849.22       | 29.17       | 820.05       | 30.00      | 9179.95         | 879.22    |
| 8      | 849.22       | 26.77       | 822.44       | 30.00      | 8357.51         | 879.22    |
| 9      | 849.22       | 24.38       | 824.84       | 30.00      | 7532.67         | 879.22    |
| 10     | 849.22       | 21.97       | 827.25       | 30.00      | 6705.42         | 879.22    |
| 11     | 849.22       | 19.56       | 829.66       | 30.00      | 5875.76         | 879.22    |
| 12     | 849.22       | 17.14       | 832.08       | 30.00      | 5043.69         | 879.22    |
| 13     | 849.22       | 14.71       | 834.51       | 30.00      | 4209.18         | 879.22    |
| 14     | 849.22       | 12.28       | 836.94       | 30.00      | 3372.24         | 879.22    |
| 15     | 849.22       | 9.84        | 839.38       | 30.00      | 2532.86         | 879.22    |
| 16     | 849.22       | 7.39        | 841.83       | 30.00      | 1691.03         | 879.22    |
| 17     | 849.22       | 4.93        | 844.28       | 30.00      | 846.75          | 879.22    |
| 18     | 849.22       | 2.47        | 846.75       | 30.00      | 0.00            | 879.22    |

### Total Payment amount
`Financials.totalPaymentAmount(loanAmount, interestRate, tenureMonths, amortizationFreeMonths, invoiceFee)`

Total Payment amount is the sum of all payments.
#### Parameters
- _loanAmount_ the total loan amount including capitalized fees (e.g. startup fee)
- _interestRate_ the annual nominal interest
- _tenureMonths_ the total tenure of the loan in number of months
- _amortizationFreeMonths_ the number of initial amortization free months, default to 0
- _invoiceFee_ a fee for each statement invoiced, default to 0

#### Value
_returns_ a double containing the sum of all payments

#### Example

```groovy
import static se.alipsa.jfinancials.Financials.*;

double loanAmt = 10000;
int tenureMonths = (int) (1.5 * 12);
int amortizationFreeMonths = 6;
double interest = 3.5 / 100;
double invoiceFee = 30

double totalAmt = totalPaymentAmount(loanAmt, interest, tenureMonths, amortizationFreeMonths, invoiceFee);
print(totalAmt);
```
```
11725.645213062116
```

### Internal Rate of Return
`double irr(PaymentPlan cf)`
`double irr(double[] cf)`

#### Parameters
- _cf_ a cash flow array or PaymentPlan (see the cashFlow or paymentPlan functions)
#### Value
_returns_ a double containing the internal return rate

#### Example
Given the cache flow above

```groovy
import static se.alipsa.jfinancials.Financials.*;

var internalReturn = irr(paymentPlan.getColumn("cashFlow"));
System.out.println(internalReturn);
```
output:
```
 0.00291665871251
```

### Annual Percentage Rate (a.k.a. effective interest)

`double apr(double monthlyIrr)`

#### Parameters
- _monthlyIrr_ the MONTHLY internal rate of return (monthly irr)

#### Value
_Returns_ a double with the annual percentage rate

#### Example

```groovy
import static se.alipsa.jfinancials.Financials.*;

double annualPercentage = apr(internalReturn)
print(annualPercentage)
```
```
0.08934409474458183
```

### Net present value
`double npv(double[] cashFlow, double rate)`

Net present value (NPV) is the difference between the present value
of cash inflows and the present value of cash outflows over a period of time.
This function produces the same results as Excel does.

#### Parameters
- _cashFlow_ cache flow e.g. the cashFlow list of the payment plan
- _rate_ interest rate

#### Value
_Returns_ a double with the net present value

#### Examples

```groovy
import se.alipsa.jfinancials.Financials.*

System.out.println(npv(List.of(-123400, 36200, 54800, 48100), 0.035));
```
```
5908.8656360761
```