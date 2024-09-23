# jfinancials
Java library for working with financial data. Based on the financials R package (also created by me).

To use it add the following dependency to your pom

```xml
<dependency>
    <groupId>se.alipsa</groupId>
    <artifactId>jfinancials</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Payment
`LoanCalculator.pmt(interestRate, nper, pv, fv = 0, type = 0)`

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

| Item            | amount |
| -----           | -----  |
| Loan Amount	  |  50000 |
| Interest rate	  |  3.50% |
| Periods	      |     60 |
| Monthly payment |	909.59 |

Then the Monthly payment can be calculated as
`LoanCalculator.pmt(3.5/100/12, 60, -50000)`

### Monthly Annuity Amount
`LoanCalculator.monthlyAnnuityAmount(loanAmount, interestRate, tenureMonths, amortizationFreemonths = 0, type = 0)`

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

| Item            | amount |
| -----           | -----  |
| Loan Amount	  |  50000 |
| Interest rate	  |  3.50% |
| Tenure	      |     60 |
| Amortization Free months |	6 |

The monthly annuity amount would be
`LoanCalculator.monthlyAnnuityAmount(50000, 3.5/100, 60, 6)` == 1002.10

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
var cf = Cashflow.cashFlow(50000, 0.035, 5, 6, 30)
```

### Payment Plan
`paymentPlan <- function(loanAmount, interestRate, tenureMonths, amortizationFreeMonths = 0, invoiceFee = 0)`

#### Parameters
- _loanAmount_ the total loan amount including capitalized fees (e.g. startup fee)
- _interestRate_ the annual nominal interest
- _tenureMonths_ the total tenure of the loan in number of months
- _amortizationFreeMonths_ the number of initial amortization free months, default to 0
- _invoiceFee_ a fee for each statement invoiced, default to 0

#### Value
_returns_ a List of Payment with the initial payment plan based on the input, based on monthly payment periods

#### Example

```r
loanAmt <- 10000
tenureMonths <- 1.5 * 12
amortizationFreeMonths <- 6
interest <- 3.5 / 100
invoiceFee <- 30

ppdf <- paymentPlan(loanAmt, interest, tenureMonths, amortizationFreeMonths, invoiceFee)
```

which will yield a data.frame (ppdf) as follows:

| month	| costOfCredit | interestAmt | amortization | invoiceFee | outgoingBalance | cashFlow |
| ----  | --------     | ------      | ------       | ------     | ------          | ----     |
| 0	|      0	|      0    |     0	    |  0    | 10000	    | -10000  |
| 1	| 29.167	| 29.167	|     0	    | 30    | 10000	    | 59.167  |
| 2	| 29.167	| 29.167    |     0	    | 30    | 10000	    | 59.167  |
| 3	| 29.167    | 29.167	|     0	    | 30    | 10000	    | 59.167  |
| 4	| 29.167    | 29.167    |	  0	    | 30    | 10000	    | 59.167  |
| 5	| 29.167	| 29.167	|     0	    | 30    | 10000	    | 59.167  |
| 6	| 849.216	| 29.167	| 820.05    | 30    | 9179.95   | 879.216 |
| 7	|  849.216	| 26.775    | 822.441	| 30    | 8357.509  | 879.216 |
| 8	| 849.216	| 24.376	| 824.84	| 30	| 7532.669	| 879.216 |
| 9	| 849.216	|  21.97	| 827.246	| 30	| 6705.423	| 879.216 |
|10	| 849.216	| 19.557	| 829.659	| 30	| 5875.764	| 879.216 |
|11	| 849.216	| 17.138	| 832.079	| 30	| 5043.685	| 879.216 | 
|12	| 849.216	| 14.711	| 834.506	| 30	|  4209.18	| 879.216 |
|13	| 849.216	| 12.277	|  836.94	| 30	|  3372.24	| 879.216 |
|14	| 849.216	|  9.836	| 839.381	| 30	|  2532.86	| 879.216 |
|15	| 849.216	|  7.388	| 841.829	| 30	| 1691.031	| 879.216 |
|16	| 849.216	|  4.932	| 844.284	| 30	|  846.747	| 879.216 |
|17	| 849.216	|   2.47	| 846.747	| 30	|        0	| 879.216 |
|18	| 849.216	|      0	| 849.216	| 30	| -849.216	| 879.216 |

### Total Payment amount
`totalPaymentAmount <- function(loanAmount, interestRate, tenureMonths, amortizationFreeMonths = 0, invoiceFee = 0)`

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

```r
loanAmt <- 10000
tenureMonths <- 1.5 * 12
amortizationFreeMonths <- 6
interest <- 3.5 / 100
invoiceFee <- 30

totalAmt <- totalPaymentAmount(loanAmt, interest, tenureMonths, amortizationFreeMonths, invoiceFee)
print(totalAmt)
```
```
[1] 11725.645213062116
```

### Internal Rate of Return
`irr <- function(cf, precision = 1e-6)`

#### Parameters
- _cf_ a vector of the cash flow (see the cashFlow or paymentPlan functions)
- _precision_ The desired accuracy to calculate the irr with (optional, default 1e-6) precision is used in the
  `uniroot` function and is assigned to the `tol` parameter of uniroot.
#### Value
_returns_ a double containing the internal return rate

#### Example
Given the cache flow above

```r
internalReturn <- irr(ppdf$cashFlow)
print(internalReturn)
```

```
[1] 0.00291665871251
```

### Annual Percentage Rate (a.k.a. effective interest)

`apr <- function(monthlyIrr)`

#### Parameters
- _monthlyIrr_ the MONTHLY internal rate of return (monthly irr)

#### Value
_Returns_ a double with the annual percentage rate

#### Example
```r
annualPercentage <- apr(internalReturn)
print(annualPercentage)
```
```
[1] 0.03556685438873
```

### Net present value

`npv <- function(i, cf, t=seq(along=cf))`

Net present value (NPV) is the difference between the present value
of cash inflows and the present value of cash outflows over a period of time.
This function produces the same results as Excel does which differs from packages such as FinCal.

#### Parameters
- _i_ interest rate
- _cf_ cache flow e.g. the cashFlow vector of the payment plan
- _t_ time series (optional)

#### Value
_Returns_ a double with the net present value

#### Examples
```r
print(npv(cf = c(-123400, 36200, 54800, 48100), i = 0.035))
```
```
[1] 5908.865636076123
```