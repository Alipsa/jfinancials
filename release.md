# jfinancials, release history

## v1.1.0, in progress
- Remove dependency on slf4j in favor of the built-in `System.Logger` (java.lang, no extra dependency)
- Upgrade JUnit dependency to use the BOM (`org.junit:junit-bom:6.0.3`)
- Upgrade plugins: maven-enforcer-plugin 3.6.2, maven-gpg-plugin 3.2.8, maven-javadoc-plugin 3.12.0, maven-site-plugin 3.21.0, maven-source-plugin 3.4.0, maven-surefire-plugin 3.5.5, versions-maven-plugin 2.21.0
- Fix `cacheFlow` → `cashFlow` typo in `Payment` field, getter (`getCashFlow`), setter (`setCashFlow`), and `toString`
- Fix `montlyInterest` → `monthlyInterest` typo in `Financials.monthlyAnnuityAmount`
- `Payment.get(int)` now throws `IndexOutOfBoundsException` for unknown indices instead of returning `null`
- Replace `maven.compiler.source`/`maven.compiler.target` with `maven.compiler.release` for correct cross-compilation
- Fix readme: version, `tenureYears` → `tenureMonths` in cashFlow parameter docs, `cachFlow` typo

## v1.0.0, 2024-09-25
Initial version of the library with the following features:
- `pmt` method for calculating loan payments and future value of investments
- `monthlyAnnuityAmount` method for calculating the monthly annuity amount for a loan
- `cashFlow` method for calculating the cash flow of a loan over time, including amortization and interest amounts
- `paymentPlan` method for generating a payment plan for a loan, showing the breakdown of each payment into interest and principal components
- `totalPaymentAmount` method for calculating the total amount paid over the life of a loan, including interest
- `irr` method for calculating the internal rate of return (IRR) of a series of cash flows, such as an investment or project
- `apr` method for calculating the annual percentage rate (APR) of a loan based on its monthly IRR
- `npv` method for calculating the net present value (NPV) of a series of cash flows given a discount rate
- 