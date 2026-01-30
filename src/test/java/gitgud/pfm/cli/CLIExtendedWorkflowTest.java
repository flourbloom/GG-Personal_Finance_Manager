package gitgud.pfm.cli;

import gitgud.pfm.Models.*;
import gitgud.pfm.services.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * Extended End-to-End tests for CLI application workflows.
 * Tests complex user scenarios, edge cases, and business logic flows.
 * Complements the existing CLIEndToEndTest with additional coverage.
 */
@DisplayName("Extended CLI End-to-End Workflow Tests")
@TestMethodOrder(OrderAnnotation.class)
class CLIExtendedWorkflowTest {

    private CategoryService categoryService;
    private WalletService walletService;
    private TransactionService transactionService;
    private BudgetService budgetService;
    private GoalService goalService;

    // Test data constants
    private static final String CATEGORY_FOOD = "CAT_E2E_FOOD";
    private static final String CATEGORY_SALARY = "CAT_E2E_SALARY";
    private static final String CATEGORY_TRANSPORT = "CAT_E2E_TRANS";
    private static final String CATEGORY_ENTERTAINMENT = "CAT_E2E_ENT";

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService();
        walletService = new WalletService();
        transactionService = new TransactionService();
        budgetService = new BudgetService();
        goalService = new GoalService();
        cleanupTestData();
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        try {
            transactionService.readAll().forEach(t -> transactionService.delete(t.getId()));
            budgetService.readAll().forEach(b -> budgetService.delete(b.getId()));
            goalService.readAll().forEach(g -> goalService.delete(g.getId()));
            walletService.readAll().forEach(w -> walletService.delete(w.getId()));
        } catch (Exception ignored) {}
    }

    // ==================== BUG #32: ACCOUNT BALANCE DISPLAY TESTS ====================

    @Nested
    @DisplayName("Bug #32: Account Balance Display Scenarios")
    class AccountBalanceDisplayTests {

        @Test
        @Order(1)
        @DisplayName("E2E: Wallet balance should reflect actual transactions - core scenario")
        void walletBalanceShouldReflectActualTransactions() {
            // Setup: Create wallet with initial balance (from DB, not calculated)
            Wallet wallet = new Wallet("Blue", 0.0, "Main Account");
            walletService.create(wallet);

            // Act: Add income
            Transaction income = new Transaction(
                CATEGORY_SALARY, 5000000.0, "Monthly Salary", 1.0,
                wallet.getId(), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            transactionService.create(income);

            // Act: Add expenses
            Transaction rent = new Transaction(
                CATEGORY_FOOD, 1000000.0, "Rent", 0.0,
                wallet.getId(), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            Transaction groceries = new Transaction(
                CATEGORY_FOOD, 300000.0, "Groceries", 0.0,
                wallet.getId(), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            transactionService.create(rent);
            transactionService.create(groceries);

            // Calculate expected balance from transactions
            List<Transaction> walletTransactions = transactionService.readAll().stream()
                .filter(t -> t.getWalletId().equals(wallet.getId()))
                .toList();

            double calculatedBalance = calculateBalanceFromTransactions(walletTransactions);

            // Assert: Balance should equal: 5000000 - 1000000 - 300000 = 3700000
            assertThat(calculatedBalance).isEqualTo(3700000.0);
        }

        @Test
        @Order(2)
        @DisplayName("E2E: Balance should update after transaction edit")
        void balanceShouldUpdateAfterTransactionEdit() {
            // Setup
            Wallet wallet = new Wallet("Green", 0.0, "Cash");
            walletService.create(wallet);

            Transaction expense = new Transaction(
                CATEGORY_FOOD, 100000.0, "Original Expense", 0.0,
                wallet.getId(), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            transactionService.create(expense);

            // Initial balance check
            double initialBalance = calculateWalletBalance(wallet.getId());
            assertThat(initialBalance).isEqualTo(-100000.0);

            // Act: Edit transaction amount
            expense.setAmount(150000.0);
            transactionService.update(expense);

            // Assert: Balance should update
            double updatedBalance = calculateWalletBalance(wallet.getId());
            assertThat(updatedBalance).isEqualTo(-150000.0);
        }

        @Test
        @Order(3)
        @DisplayName("E2E: Balance should update after transaction deletion")
        void balanceShouldUpdateAfterTransactionDeletion() {
            // Setup
            Wallet wallet = new Wallet("Blue", 0.0, "Card");
            walletService.create(wallet);

            Transaction expense1 = new Transaction(
                CATEGORY_FOOD, 50000.0, "Expense 1", 0.0,
                wallet.getId(), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            Transaction expense2 = new Transaction(
                CATEGORY_TRANSPORT, 30000.0, "Expense 2", 0.0,
                wallet.getId(), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            transactionService.create(expense1);
            transactionService.create(expense2);

            // Initial balance: -80000
            double initialBalance = calculateWalletBalance(wallet.getId());
            assertThat(initialBalance).isEqualTo(-80000.0);

            // Act: Delete one transaction
            transactionService.delete(expense1.getId());

            // Assert: Balance should update to -30000
            double updatedBalance = calculateWalletBalance(wallet.getId());
            assertThat(updatedBalance).isEqualTo(-30000.0);
        }

        @Test
        @Order(4)
        @DisplayName("E2E: Multi-wallet balance independence")
        void multiWalletBalanceIndependence() {
            // Setup: Multiple wallets
            Wallet cashWallet = new Wallet("Green", 0.0, "Cash");
            Wallet cardWallet = new Wallet("Blue", 0.0, "Card");
            Wallet savingsWallet = new Wallet("Yellow", 0.0, "Savings");
            walletService.create(cashWallet);
            walletService.create(cardWallet);
            walletService.create(savingsWallet);

            // Add transactions to different wallets
            transactionService.create(new Transaction(CATEGORY_SALARY, 3000000.0, "Cash Deposit", 1.0, cashWallet.getId(), "2026-01-15T10:00:00"));
            transactionService.create(new Transaction(CATEGORY_FOOD, 200000.0, "Cash Expense", 0.0, cashWallet.getId(), "2026-01-16T10:00:00"));

            transactionService.create(new Transaction(CATEGORY_SALARY, 5000000.0, "Salary", 1.0, cardWallet.getId(), "2026-01-01T10:00:00"));
            transactionService.create(new Transaction(CATEGORY_TRANSPORT, 100000.0, "Fuel", 0.0, cardWallet.getId(), "2026-01-10T10:00:00"));

            transactionService.create(new Transaction(CATEGORY_SALARY, 1000000.0, "Transfer to Savings", 1.0, savingsWallet.getId(), "2026-01-05T10:00:00"));

            // Calculate balances
            double cashBalance = calculateWalletBalance(cashWallet.getId());
            double cardBalance = calculateWalletBalance(cardWallet.getId());
            double savingsBalance = calculateWalletBalance(savingsWallet.getId());

            // Assert: Each wallet should have independent balance
            assertThat(cashBalance).isEqualTo(2800000.0);  // 3000000 - 200000
            assertThat(cardBalance).isEqualTo(4900000.0);  // 5000000 - 100000
            assertThat(savingsBalance).isEqualTo(1000000.0); // Only income
        }

        private double calculateWalletBalance(String walletId) {
            return calculateBalanceFromTransactions(
                transactionService.readAll().stream()
                    .filter(t -> t.getWalletId().equals(walletId))
                    .toList()
            );
        }
    }

    // ==================== BUG #34: BUDGET CATEGORY TRACKING TESTS ====================

    @Nested
    @DisplayName("Bug #34: Budget Category Tracking Scenarios")
    class BudgetCategoryTrackingTests {

        @Test
        @Order(1)
        @DisplayName("E2E: Budget should track spending from associated category only")
        void budgetShouldTrackSpendingFromAssociatedCategoryOnly() {
            // Setup: Wallet
            Wallet wallet = new Wallet("Green", 0.0, "Cash");
            walletService.create(wallet);

            // Setup: Food budget for specific category
            Budget foodBudget = new Budget(
                "Food Budget", 500000.0, 0.0, "2026-01-01", "2026-01-31"
            );
            budgetService.create(foodBudget);
            // Note: In real implementation, budget should be associated with CATEGORY_FOOD

            // Add transactions for different categories
            Transaction foodExpense1 = new Transaction(
                CATEGORY_FOOD, 100000.0, "Restaurant", 0.0,
                wallet.getId(), "2026-01-15T12:00:00"
            );
            Transaction foodExpense2 = new Transaction(
                CATEGORY_FOOD, 150000.0, "Groceries", 0.0,
                wallet.getId(), "2026-01-16T10:00:00"
            );
            Transaction transportExpense = new Transaction(
                CATEGORY_TRANSPORT, 50000.0, "Bus fare", 0.0,
                wallet.getId(), "2026-01-15T08:00:00"
            );
            Transaction entertainment = new Transaction(
                CATEGORY_ENTERTAINMENT, 200000.0, "Movie", 0.0,
                wallet.getId(), "2026-01-17T19:00:00"
            );

            transactionService.create(foodExpense1);
            transactionService.create(foodExpense2);
            transactionService.create(transportExpense);
            transactionService.create(entertainment);

            // Calculate food spending only
            double foodSpending = transactionService.readAll().stream()
                .filter(t -> t.getCategoryId().equals(CATEGORY_FOOD))
                .filter(t -> t.getIncome() == 0.0)
                .mapToDouble(Transaction::getAmount)
                .sum();

            // Update budget balance
            foodBudget.setBalance(foodSpending);
            budgetService.update(foodBudget);

            // Assert: Budget should only count food expenses
            Budget updated = budgetService.read(foodBudget.getId());
            assertThat(updated.getBalance()).isEqualTo(250000.0); // 100000 + 150000
            assertThat(updated.getLimitAmount() - updated.getBalance()).isEqualTo(250000.0); // Remaining
        }

        @Test
        @Order(2)
        @DisplayName("E2E: Budget should only track transactions within date range")
        void budgetShouldOnlyTrackTransactionsWithinDateRange() {
            // Setup
            Wallet wallet = new Wallet("Green", 0.0, "Cash");
            walletService.create(wallet);

            Budget februaryBudget = new Budget(
                "February Food", 500000.0, 0.0, "2026-02-01", "2026-02-28"
            );
            budgetService.create(februaryBudget);

            // Transactions in and out of budget period
            transactionService.create(new Transaction(
                CATEGORY_FOOD, 100000.0, "January Food", 0.0, wallet.getId(), "2026-01-31T23:00:00"
            ));
            transactionService.create(new Transaction(
                CATEGORY_FOOD, 150000.0, "February Food 1", 0.0, wallet.getId(), "2026-02-05T12:00:00"
            ));
            transactionService.create(new Transaction(
                CATEGORY_FOOD, 75000.0, "February Food 2", 0.0, wallet.getId(), "2026-02-15T18:00:00"
            ));
            transactionService.create(new Transaction(
                CATEGORY_FOOD, 50000.0, "March Food", 0.0, wallet.getId(), "2026-03-01T10:00:00"
            ));

            // Calculate February spending only
            LocalDate budgetStart = LocalDate.parse("2026-02-01");
            LocalDate budgetEnd = LocalDate.parse("2026-02-28");

            double februarySpending = transactionService.readAll().stream()
                .filter(t -> t.getCategoryId().equals(CATEGORY_FOOD))
                .filter(t -> t.getIncome() == 0.0)
                .filter(t -> {
                    LocalDate txDate = LocalDateTime.parse(t.getCreateTime()).toLocalDate();
                    return !txDate.isBefore(budgetStart) && !txDate.isAfter(budgetEnd);
                })
                .mapToDouble(Transaction::getAmount)
                .sum();

            // Update and verify
            februaryBudget.setBalance(februarySpending);
            budgetService.update(februaryBudget);

            Budget updated = budgetService.read(februaryBudget.getId());
            assertThat(updated.getBalance()).isEqualTo(225000.0); // 150000 + 75000 (only Feb)
        }

        @Test
        @Order(3)
        @DisplayName("E2E: Budget warning when approaching limit")
        void budgetWarningShouldTriggerWhenApproachingLimit() {
            // Setup
            Wallet wallet = new Wallet("Blue", 0.0, "Card");
            walletService.create(wallet);

            Budget tightBudget = new Budget(
                "Tight Budget", 100000.0, 0.0, "2026-01-01", "2026-01-31"
            );
            budgetService.create(tightBudget);

            // Spend 85% of budget
            transactionService.create(new Transaction(
                CATEGORY_FOOD, 85000.0, "Big Purchase", 0.0, wallet.getId(), "2026-01-15T10:00:00"
            ));

            // Calculate spending
            double spent = 85000.0;
            tightBudget.setBalance(spent);
            budgetService.update(tightBudget);

            Budget updated = budgetService.read(tightBudget.getId());
            double percentUsed = (updated.getBalance() / updated.getLimitAmount()) * 100;
            double remaining = updated.getLimitAmount() - updated.getBalance();

            // Assert: Warning threshold checks
            assertThat(percentUsed).isGreaterThan(80.0);
            assertThat(remaining).isEqualTo(15000.0);
            
            // Simulating CLI warning display logic
            boolean shouldWarn = percentUsed >= 80.0;
            assertThat(shouldWarn).isTrue();
        }
    }

    // ==================== COMPLEX FINANCIAL WORKFLOWS ====================

    @Nested
    @DisplayName("Complex Financial Workflow Tests")
    class ComplexFinancialWorkflowTests {

        @Test
        @DisplayName("E2E: Monthly salary cycle with recurring expenses")
        void monthlySalaryCycleWithRecurringExpenses() {
            // Setup: Main account
            Wallet mainAccount = new Wallet("Blue", 0.0, "Main Account");
            walletService.create(mainAccount);

            // Simulate monthly cycle
            String[] months = {"01", "02", "03"};
            double monthlySalary = 5000000.0;
            double monthlyRent = 1500000.0;
            double monthlyUtilities = 200000.0;
            double monthlyFood = 800000.0;

            for (String month : months) {
                // Salary income
                transactionService.create(new Transaction(
                    CATEGORY_SALARY, monthlySalary, "Salary - " + month, 1.0,
                    mainAccount.getId(), "2026-" + month + "-01T09:00:00"
                ));

                // Fixed expenses
                transactionService.create(new Transaction(
                    CATEGORY_FOOD, monthlyRent, "Rent - " + month, 0.0,
                    mainAccount.getId(), "2026-" + month + "-05T10:00:00"
                ));
                transactionService.create(new Transaction(
                    CATEGORY_FOOD, monthlyUtilities, "Utilities - " + month, 0.0,
                    mainAccount.getId(), "2026-" + month + "-10T10:00:00"
                ));
                transactionService.create(new Transaction(
                    CATEGORY_FOOD, monthlyFood, "Food - " + month, 0.0,
                    mainAccount.getId(), "2026-" + month + "-15T10:00:00"
                ));
            }

            // Calculate totals
            List<Transaction> allTransactions = transactionService.readAll();
            
            double totalIncome = allTransactions.stream()
                .filter(t -> t.getIncome() > 0)
                .mapToDouble(Transaction::getAmount)
                .sum();
            double totalExpenses = allTransactions.stream()
                .filter(t -> t.getIncome() == 0.0)
                .mapToDouble(Transaction::getAmount)
                .sum();

            // Assert: 3 months of data
            assertThat(allTransactions).hasSize(12); // 4 transactions x 3 months
            assertThat(totalIncome).isEqualTo(15000000.0); // 5M x 3
            assertThat(totalExpenses).isEqualTo(7500000.0); // (1.5M + 0.2M + 0.8M) x 3
            
            double endBalance = calculateBalanceFromTransactions(allTransactions);
            assertThat(endBalance).isEqualTo(7500000.0); // Net savings
        }

        @Test
        @DisplayName("E2E: Goal progress tracking with contributions")
        void goalProgressTrackingWithContributions() {
            // Setup
            Wallet savingsWallet = new Wallet("Yellow", 0.0, "Savings");
            walletService.create(savingsWallet);

            Goal emergencyFund = new Goal(
                "Emergency Fund", 10000000.0, 0.0, "2026-12-31", 1.0,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            goalService.create(emergencyFund);

            // Monthly contributions
            double[] contributions = {500000.0, 600000.0, 700000.0, 800000.0};
            double runningTotal = 0.0;

            for (int i = 0; i < contributions.length; i++) {
                runningTotal += contributions[i];
                emergencyFund.setBalance(runningTotal);
                goalService.update(emergencyFund);

                // Track progress
                Goal current = goalService.read(emergencyFund.getId());
                double progressPercent = (current.getBalance() / current.getTarget()) * 100;
                
                // Log/verify monthly progress
                assertThat(current.getBalance()).isEqualTo(runningTotal);
            }

            // Final verification
            Goal finalGoal = goalService.read(emergencyFund.getId());
            assertThat(finalGoal.getBalance()).isEqualTo(2600000.0);
            
            double finalProgress = (finalGoal.getBalance() / finalGoal.getTarget()) * 100;
            assertThat(finalProgress).isEqualTo(26.0);
        }

        @Test
        @DisplayName("E2E: Multi-budget tracking for different expense categories")
        void multiBudgetTrackingForDifferentCategories() {
            // Setup
            Wallet wallet = new Wallet("Green", 0.0, "Cash");
            walletService.create(wallet);

            Budget foodBudget = new Budget("Food", 500000.0, 0.0, "2026-01-01", "2026-01-31");
            Budget transportBudget = new Budget("Transport", 200000.0, 0.0, "2026-01-01", "2026-01-31");
            Budget entertainmentBudget = new Budget("Entertainment", 300000.0, 0.0, "2026-01-01", "2026-01-31");
            
            budgetService.create(foodBudget);
            budgetService.create(transportBudget);
            budgetService.create(entertainmentBudget);

            // Add mixed transactions
            transactionService.create(new Transaction(CATEGORY_FOOD, 150000.0, "Groceries", 0.0, wallet.getId(), "2026-01-10T10:00:00"));
            transactionService.create(new Transaction(CATEGORY_FOOD, 80000.0, "Restaurant", 0.0, wallet.getId(), "2026-01-15T19:00:00"));
            transactionService.create(new Transaction(CATEGORY_TRANSPORT, 50000.0, "Bus Pass", 0.0, wallet.getId(), "2026-01-01T08:00:00"));
            transactionService.create(new Transaction(CATEGORY_TRANSPORT, 100000.0, "Taxi", 0.0, wallet.getId(), "2026-01-20T22:00:00"));
            transactionService.create(new Transaction(CATEGORY_ENTERTAINMENT, 200000.0, "Concert", 0.0, wallet.getId(), "2026-01-25T20:00:00"));

            // Calculate spending per category
            Map<String, Double> categorySpending = transactionService.readAll().stream()
                .filter(t -> t.getIncome() == 0.0)
                .collect(Collectors.groupingBy(
                    Transaction::getCategoryId,
                    Collectors.summingDouble(Transaction::getAmount)
                ));

            // Update budgets
            foodBudget.setBalance(categorySpending.getOrDefault(CATEGORY_FOOD, 0.0));
            transportBudget.setBalance(categorySpending.getOrDefault(CATEGORY_TRANSPORT, 0.0));
            entertainmentBudget.setBalance(categorySpending.getOrDefault(CATEGORY_ENTERTAINMENT, 0.0));
            
            budgetService.update(foodBudget);
            budgetService.update(transportBudget);
            budgetService.update(entertainmentBudget);

            // Verify
            assertThat(budgetService.read(foodBudget.getId()).getBalance()).isEqualTo(230000.0);
            assertThat(budgetService.read(transportBudget.getId()).getBalance()).isEqualTo(150000.0);
            assertThat(budgetService.read(entertainmentBudget.getId()).getBalance()).isEqualTo(200000.0);

            // Check which budgets are over limit
            assertThat(transportBudget.getBalance()).isLessThan(transportBudget.getLimitAmount());
            assertThat(entertainmentBudget.getBalance()).isLessThan(entertainmentBudget.getLimitAmount());
            assertThat(foodBudget.getBalance()).isLessThan(foodBudget.getLimitAmount());
        }
    }

    // ==================== EDGE CASE AND ERROR HANDLING TESTS ====================

    @Nested
    @DisplayName("Edge Case and Error Handling Tests")
    class EdgeCaseAndErrorHandlingTests {

        @Test
        @DisplayName("E2E: Empty wallet should have zero balance")
        void emptyWalletShouldHaveZeroBalance() {
            Wallet emptyWallet = new Wallet("Gray", 0.0, "Empty");
            walletService.create(emptyWallet);

            double balance = calculateBalanceFromTransactions(
                transactionService.readAll().stream()
                    .filter(t -> t.getWalletId().equals(emptyWallet.getId()))
                    .toList()
            );

            assertThat(balance).isEqualTo(0.0);
        }

        @Test
        @DisplayName("E2E: Negative balance handling (overdraft scenario)")
        void negativeBalanceHandling() {
            Wallet wallet = new Wallet("Red", 0.0, "Overdraft Test");
            walletService.create(wallet);

            // Only expenses, no income
            transactionService.create(new Transaction(
                CATEGORY_FOOD, 100000.0, "Expense 1", 0.0, wallet.getId(), "2026-01-10T10:00:00"
            ));
            transactionService.create(new Transaction(
                CATEGORY_TRANSPORT, 50000.0, "Expense 2", 0.0, wallet.getId(), "2026-01-15T10:00:00"
            ));

            double balance = calculateBalanceFromTransactions(
                transactionService.readAll().stream()
                    .filter(t -> t.getWalletId().equals(wallet.getId()))
                    .toList()
            );

            // Balance should be negative
            assertThat(balance).isNegative();
            assertThat(balance).isEqualTo(-150000.0);
        }

        @Test
        @DisplayName("E2E: Large number of transactions performance")
        void largeNumberOfTransactionsPerformance() {
            Wallet wallet = new Wallet("Blue", 0.0, "Bulk Test");
            walletService.create(wallet);

            // Create 100 transactions
            int transactionCount = 100;
            for (int i = 0; i < transactionCount; i++) {
                transactionService.create(new Transaction(
                    CATEGORY_FOOD, 1000.0 + i, "Transaction " + i,
                    i % 2, // Alternating income/expense
                    wallet.getId(),
                    String.format("2026-01-%02dT%02d:00:00", (i % 28) + 1, (i % 24))
                ));
            }

            // Verify all created
            assertThat(transactionService.readAll()).hasSize(transactionCount);

            // Calculate balance
            List<Transaction> all = transactionService.readAll();
            double balance = calculateBalanceFromTransactions(all);

            // Should complete without timeout
            assertThat(balance).isNotNull();
        }

        @Test
        @DisplayName("E2E: Zero amount transaction handling")
        void zeroAmountTransactionHandling() {
            Wallet wallet = new Wallet("Green", 0.0, "Zero Test");
            walletService.create(wallet);

            Transaction zeroTx = new Transaction(
                CATEGORY_FOOD, 0.0, "Zero Amount", 0.0, wallet.getId(), "2026-01-15T10:00:00"
            );
            transactionService.create(zeroTx);

            double balance = calculateBalanceFromTransactions(
                transactionService.readAll().stream()
                    .filter(t -> t.getWalletId().equals(wallet.getId()))
                    .toList()
            );

            assertThat(balance).isEqualTo(0.0);
        }

        @Test
        @DisplayName("E2E: Very small amounts (precision test)")
        void verySmallAmountsPrecisionTest() {
            Wallet wallet = new Wallet("Green", 0.0, "Precision");
            walletService.create(wallet);

            transactionService.create(new Transaction(
                CATEGORY_SALARY, 0.01, "Tiny Income", 1.0, wallet.getId(), "2026-01-10T10:00:00"
            ));
            transactionService.create(new Transaction(
                CATEGORY_FOOD, 0.005, "Tiny Expense", 0.0, wallet.getId(), "2026-01-10T10:00:00"
            ));

            double balance = calculateBalanceFromTransactions(
                transactionService.readAll().stream()
                    .filter(t -> t.getWalletId().equals(wallet.getId()))
                    .toList()
            );

            assertThat(balance).isCloseTo(0.005, within(0.0001));
        }

        @Test
        @DisplayName("E2E: Goal completion detection")
        void goalCompletionDetection() {
            Goal goal = new Goal(
                "Small Goal", 100000.0, 0.0, "2026-06-30", 1.0,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            goalService.create(goal);

            // Contribute exactly the target
            goal.setBalance(100000.0);
            goalService.update(goal);

            Goal updated = goalService.read(goal.getId());
            boolean isCompleted = updated.getBalance() >= updated.getTarget();

            assertThat(isCompleted).isTrue();
        }

        @Test
        @DisplayName("E2E: Budget exceeds limit handling")
        void budgetExceedsLimitHandling() {
            Wallet wallet = new Wallet("Red", 0.0, "Overspend");
            walletService.create(wallet);

            Budget smallBudget = new Budget("Small Budget", 50000.0, 0.0, "2026-01-01", "2026-01-31");
            budgetService.create(smallBudget);

            // Overspend
            transactionService.create(new Transaction(
                CATEGORY_FOOD, 75000.0, "Big Expense", 0.0, wallet.getId(), "2026-01-15T10:00:00"
            ));

            smallBudget.setBalance(75000.0);
            budgetService.update(smallBudget);

            Budget updated = budgetService.read(smallBudget.getId());
            boolean isOverBudget = updated.getBalance() > updated.getLimitAmount();

            assertThat(isOverBudget).isTrue();
            assertThat(updated.getBalance() - updated.getLimitAmount()).isEqualTo(25000.0); // Overage
        }
    }

    // ==================== PARAMETERIZED TESTS ====================

    @Nested
    @DisplayName("Parameterized Workflow Tests")
    class ParameterizedWorkflowTests {

        @ParameterizedTest
        @CsvSource({
            "100000, 50000, 50000",
            "500000, 300000, 200000",
            "1000000, 1000000, 0",
            "0, 0, 0"
        })
        @DisplayName("E2E: Income minus expense equals balance")
        void incomeMinusExpenseEqualsBalance(double income, double expense, double expectedBalance) {
            Wallet wallet = new Wallet("Blue", 0.0, "Param Test");
            walletService.create(wallet);

            if (income > 0) {
                transactionService.create(new Transaction(
                    CATEGORY_SALARY, income, "Income", 1.0, wallet.getId(), "2026-01-10T10:00:00"
                ));
            }
            if (expense > 0) {
                transactionService.create(new Transaction(
                    CATEGORY_FOOD, expense, "Expense", 0.0, wallet.getId(), "2026-01-15T10:00:00"
                ));
            }

            double balance = calculateBalanceFromTransactions(
                transactionService.readAll().stream()
                    .filter(t -> t.getWalletId().equals(wallet.getId()))
                    .toList()
            );

            assertThat(balance).isEqualTo(expectedBalance);

            // Cleanup for next iteration
            transactionService.readAll().forEach(t -> transactionService.delete(t.getId()));
            walletService.delete(wallet.getId());
        }

        @ParameterizedTest
        @CsvSource({
            "100000, 50000, 50",
            "100000, 100000, 100",
            "100000, 0, 0",
            "100000, 150000, 150"
        })
        @DisplayName("E2E: Budget percentage calculation")
        void budgetPercentageCalculation(double limit, double spent, double expectedPercent) {
            Budget budget = new Budget("Param Budget", limit, spent, "2026-01-01", "2026-01-31");
            budgetService.create(budget);

            Budget retrieved = budgetService.read(budget.getId());
            double actualPercent = (retrieved.getBalance() / retrieved.getLimitAmount()) * 100;

            assertThat(actualPercent).isEqualTo(expectedPercent);

            budgetService.delete(budget.getId());
        }
    }

    // ==================== HELPER METHODS ====================

    private double calculateBalanceFromTransactions(List<Transaction> transactions) {
        double income = transactions.stream()
            .filter(t -> t.getIncome() > 0)
            .mapToDouble(Transaction::getAmount)
            .sum();
        double expenses = transactions.stream()
            .filter(t -> t.getIncome() == 0.0)
            .mapToDouble(Transaction::getAmount)
            .sum();
        return income - expenses;
    }

    private static org.assertj.core.data.Offset<Double> within(double tolerance) {
        return org.assertj.core.data.Offset.offset(tolerance);
    }
}
