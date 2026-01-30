package gitgud.pfm.cli;

import gitgud.pfm.Models.*;
import gitgud.pfm.services.*;
import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

/**
 * End-to-End tests for CLI application flow.
 * Tests complete user workflows and data persistence.
 * Note: These tests verify service layer integration rather than full CLI interaction.
 */
@DisplayName("CLI End-to-End Service Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CLIEndToEndTest {

    private CategoryService categoryService;
    private WalletService walletService;
    private TransactionService transactionService;
    private BudgetService budgetService;
    private GoalService goalService;

    @BeforeEach
    void setUp() {
        // Initialize services
        categoryService = new CategoryService();
        walletService = new WalletService();
        transactionService = new TransactionService();
        budgetService = new BudgetService();
        goalService = new GoalService();
        
        // Clean up any existing test data
        cleanupTestData();
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        cleanupTestData();
    }

    private void cleanupTestData() {
        // Delete all test data in reverse dependency order
        try {
            transactionService.readAll().forEach(t -> transactionService.delete(t.getId()));
            budgetService.readAll().forEach(b -> budgetService.delete(b.getId()));
            goalService.readAll().forEach(g -> goalService.delete(g.getId()));
            walletService.readAll().forEach(w -> walletService.delete(w.getId()));
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @Test
    @Order(1)
    @DisplayName("E2E: Complete transaction workflow - create, read, persistence")
    void shouldHandleCompleteTransactionFlow() {
        // Setup: Create a wallet first
        Wallet cashWallet = new Wallet("Green", 0.0, "Cash");
        walletService.create(cashWallet);
        
        // Act: Create a transaction (simulating CLI add transaction flow)
        Transaction transaction = new Transaction(
            "CAT_001",     // Food category
            150.50,
            "Grocery Shopping",
            0.0,           // Expense
            cashWallet.getId(),
            "2026-01-31T10:30:00"
        );
        transactionService.create(transaction);
        
        // Assert: Verify transaction was persisted
        var transactions = transactionService.readAll();
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getName()).isEqualTo("Grocery Shopping");
        assertThat(transactions.get(0).getAmount()).isEqualTo(150.50);
        assertThat(transactions.get(0).getWalletId()).isEqualTo(cashWallet.getId());
        
        // Assert: Verify we can read it back by ID
        Transaction retrieved = transactionService.read(transaction.getId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getName()).isEqualTo("Grocery Shopping");
    }

    @Test
    @Order(2)
    @DisplayName("E2E: Budget management complete workflow")
    void shouldHandleCompleteBudgetFlow() {
        // Act: Create a budget (simulating CLI add budget flow)
        Budget budget = new Budget(
            "Monthly Food Budget",
            1000.00,
            0.0,
            "2026-02-01",
            "2026-02-28"
        );
        budgetService.create(budget);
        
        // Assert: Verify budget was persisted
        var budgets = budgetService.readAll();
        assertThat(budgets).hasSize(1);
        assertThat(budgets.get(0).getName()).isEqualTo("Monthly Food Budget");
        assertThat(budgets.get(0).getLimitAmount()).isEqualTo(1000.00);
        assertThat(budgets.get(0).getStartDate()).isEqualTo("2026-02-01");
        assertThat(budgets.get(0).getEndDate()).isEqualTo("2026-02-28");
        
        // Act: Update budget balance (simulating spending)
        budget.setBalance(250.50);
        budgetService.update(budget);
        
        // Assert: Verify update was persisted
        Budget updated = budgetService.read(budget.getId());
        assertThat(updated.getBalance()).isEqualTo(250.50);
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Goal creation, tracking, and completion workflow")
    void shouldHandleCompleteGoalFlow() {
        // Act: Create a goal (simulating CLI add goal flow)
        Goal goal = new Goal(
            "Emergency Fund",
            10000.00,      // Target
            2000.00,       // Current
            "2026-12-31",  // Deadline
            1.0,           // High priority
            "2026-01-31T00:00:00"
        );
        goalService.create(goal);
        
        // Assert: Verify goal was persisted
        var goals = goalService.readAll();
        assertThat(goals).hasSize(1);
        assertThat(goals.get(0).getName()).isEqualTo("Emergency Fund");
        assertThat(goals.get(0).getTarget()).isEqualTo(10000.00);
        assertThat(goals.get(0).getBalance()).isEqualTo(2000.00);
        assertThat(goals.get(0).getPriority()).isEqualTo(1.0);
        
        // Act: Update goal progress (simulating contribution)
        goal.setBalance(5000.00);
        goalService.update(goal);
        
        // Assert: Verify progress was persisted
        Goal updated = goalService.read(goal.getId());
        assertThat(updated.getBalance()).isEqualTo(5000.00);
        
        // Assert: Calculate progress percentage
        double progressPercentage = (updated.getBalance() / updated.getTarget()) * 100;
        assertThat(progressPercentage).isEqualTo(50.0);
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Multiple transactions with different wallets and categories")
    void shouldHandleMultipleTransactionsAcrossWallets() {
        // Setup: Create two wallets
        Wallet cashWallet = new Wallet("Green", 0.0, "Cash");
        Wallet cardWallet = new Wallet("Blue", 0.0, "Card");
        walletService.create(cashWallet);
        walletService.create(cardWallet);
        
        // Act: Create income transaction
        Transaction income = new Transaction(
            "CAT_002",     // Salary category
            3000.00,
            "Monthly Salary",
            1.0,           // Income
            cardWallet.getId(),
            "2026-01-31T09:00:00"
        );
        transactionService.create(income);
        
        // Act: Create expense transactions
        Transaction rent = new Transaction(
            "CAT_003",     // Housing category
            1200.00,
            "Rent Payment",
            0.0,           // Expense
            cashWallet.getId(),
            "2026-01-31T10:00:00"
        );
        transactionService.create(rent);
        
        Transaction groceries = new Transaction(
            "CAT_001",     // Food category
            250.75,
            "Weekly Groceries",
            0.0,           // Expense
            cashWallet.getId(),
            "2026-01-31T11:00:00"
        );
        transactionService.create(groceries);
        
        // Assert: Verify all transactions were persisted
        var transactions = transactionService.readAll();
        assertThat(transactions).hasSize(3);
        
        // Assert: Calculate financial summary
        double totalIncome = transactions.stream()
            .filter(t -> t.getIncome() > 0)
            .mapToDouble(Transaction::getAmount)
            .sum();
        double totalExpenses = transactions.stream()
            .filter(t -> t.getIncome() == 0)
            .mapToDouble(Transaction::getAmount)
            .sum();
        double netBalance = totalIncome - totalExpenses;
        
        assertThat(totalIncome).isEqualTo(3000.00);
        assertThat(totalExpenses).isEqualTo(1450.75);
        assertThat(netBalance).isEqualTo(1549.25);
        
        // Assert: Verify transactions per wallet
        long cashTransactions = transactions.stream()
            .filter(t -> t.getWalletId().equals(cashWallet.getId()))
            .count();
        long cardTransactions = transactions.stream()
            .filter(t -> t.getWalletId().equals(cardWallet.getId()))
            .count();
        
        assertThat(cashTransactions).isEqualTo(2);
        assertThat(cardTransactions).isEqualTo(1);
    }

    @Test
    @Order(5)
    @DisplayName("E2E: Budget tracking with actual spending")
    void shouldTrackBudgetSpendingWithTransactions() {
        // Setup: Create budget and wallet
        Budget foodBudget = new Budget("Food Budget", 500.00, 0.0, "2026-02-01", "2026-02-28");
        budgetService.create(foodBudget);
        
        Wallet cashWallet = new Wallet("Green", 0.0, "Cash");
        walletService.create(cashWallet);
        
        // Act: Create food transactions
        Transaction groceries1 = new Transaction(
            "CAT_001", 150.00, "Groceries", 0.0, cashWallet.getId(), "2026-02-05T10:00:00"
        );
        transactionService.create(groceries1);
        
        Transaction restaurant = new Transaction(
            "CAT_001", 75.50, "Restaurant", 0.0, cashWallet.getId(), "2026-02-10T19:00:00"
        );
        transactionService.create(restaurant);
        
        // Assert: Calculate total spending
        var transactions = transactionService.readAll();
        double totalSpent = transactions.stream()
            .filter(t -> t.getCategoryId().equals("CAT_001"))
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        assertThat(totalSpent).isEqualTo(225.50);
        
        // Act: Update budget balance with spending
        foodBudget.setBalance(totalSpent);
        budgetService.update(foodBudget);
        
        // Assert: Verify budget tracking
        Budget updated = budgetService.read(foodBudget.getId());
        assertThat(updated.getBalance()).isEqualTo(225.50);
        
        double remainingBudget = updated.getLimitAmount() - updated.getBalance();
        assertThat(remainingBudget).isEqualTo(274.50);
        assertThat(updated.getBalance() < updated.getLimitAmount()).isTrue();
    }

    @Test
    @Order(6)
    @DisplayName("E2E: Wallet initialization and management")
    void shouldManageWalletsCorrectly() {
        // Act: Create default wallets (simulating CLI initialization)
        Wallet cashWallet = new Wallet("Green", 0.0, "Cash");
        Wallet cardWallet = new Wallet("Blue", 0.0, "Card");
        Wallet savingsWallet = new Wallet("Yellow", 0.0, "Savings");
        
        walletService.create(cashWallet);
        walletService.create(cardWallet);
        walletService.create(savingsWallet);
        
        // Assert: Verify wallets were created
        var wallets = walletService.readAll();
        assertThat(wallets).hasSize(3);
        assertThat(wallets).extracting(Wallet::getName)
            .containsExactlyInAnyOrder("Cash", "Card", "Savings");
        
        // Act: Modify wallet balance in memory
        cashWallet.setBalance(500.00);
        
        // Assert: Verify in-memory modification
        assertThat(cashWallet.getBalance()).isEqualTo(500.00);
    }

    @Test
    @Order(7)
    @DisplayName("E2E: Transaction update and deletion workflow")
    void shouldUpdateAndDeleteTransactions() {
        // Setup: Create wallet and transaction
        Wallet wallet = new Wallet("Green", 0.0, "Cash");
        walletService.create(wallet);
        
        Transaction transaction = new Transaction(
            "CAT_001", 100.00, "Original Name", 0.0, wallet.getId(), "2026-01-31T10:00:00"
        );
        transactionService.create(transaction);
        
        // Act: Update transaction
        transaction.setName("Updated Name");
        transaction.setAmount(150.00);
        transactionService.update(transaction);
        
        // Assert: Verify update
        Transaction updated = transactionService.read(transaction.getId());
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getAmount()).isEqualTo(150.00);
        
        // Act: Delete transaction
        transactionService.delete(transaction.getId());
        
        // Assert: Verify deletion
        var transactions = transactionService.readAll();
        assertThat(transactions).isEmpty();
    }

    @Test
    @Order(8)
    @DisplayName("E2E: Complete financial session - income, expenses, budgets, goals")
    void shouldHandleCompleteFinancialSession() {
        // Setup: Initialize wallets
        Wallet cashWallet = new Wallet("Green", 0.0, "Cash");
        Wallet cardWallet = new Wallet("Blue", 0.0, "Card");
        walletService.create(cashWallet);
        walletService.create(cardWallet);
        
        // Act: Add monthly budget
        Budget monthlyBudget = new Budget("February Budget", 2000.00, 0.0, "2026-02-01", "2026-02-28");
        budgetService.create(monthlyBudget);
        
        // Act: Add savings goal
        Goal savingsGoal = new Goal("Emergency Fund", 5000.00, 1000.00, "2026-06-30", 1.0, "2026-01-31T00:00:00");
        goalService.create(savingsGoal);
        
        // Act: Add income
        Transaction salary = new Transaction(
            "CAT_002", 3000.00, "Salary", 1.0, cardWallet.getId(), "2026-02-01T09:00:00"
        );
        transactionService.create(salary);
        
        // Act: Add expenses
        Transaction rent = new Transaction(
            "CAT_003", 1200.00, "Rent", 0.0, cashWallet.getId(), "2026-02-01T10:00:00"
        );
        Transaction groceries = new Transaction(
            "CAT_001", 300.00, "Groceries", 0.0, cashWallet.getId(), "2026-02-05T14:00:00"
        );
        Transaction utilities = new Transaction(
            "CAT_003", 150.00, "Utilities", 0.0, cardWallet.getId(), "2026-02-10T16:00:00"
        );
        transactionService.create(rent);
        transactionService.create(groceries);
        transactionService.create(utilities);
        
        // Assert: Verify all data persisted
        assertThat(walletService.readAll()).hasSize(2);
        assertThat(budgetService.readAll()).hasSize(1);
        assertThat(goalService.readAll()).hasSize(1);
        assertThat(transactionService.readAll()).hasSize(4);
        
        // Assert: Calculate financial summary
        var transactions = transactionService.readAll();
        double totalIncome = transactions.stream()
            .filter(t -> t.getIncome() > 0)
            .mapToDouble(Transaction::getAmount)
            .sum();
        double totalExpenses = transactions.stream()
            .filter(t -> t.getIncome() == 0)
            .mapToDouble(Transaction::getAmount)
            .sum();
        double netSavings = totalIncome - totalExpenses;
        
        assertThat(totalIncome).isEqualTo(3000.00);
        assertThat(totalExpenses).isEqualTo(1650.00);
        assertThat(netSavings).isEqualTo(1350.00);
        
        // Assert: Update budget with spending
        double budgetSpent = transactions.stream()
            .filter(t -> t.getIncome() == 0)
            .mapToDouble(Transaction::getAmount)
            .sum();
        monthlyBudget.setBalance(budgetSpent);
        budgetService.update(monthlyBudget);
        
        Budget updatedBudget = budgetService.read(monthlyBudget.getId());
        assertThat(updatedBudget.getBalance()).isEqualTo(1650.00);
        assertThat(updatedBudget.getBalance() < updatedBudget.getLimitAmount()).isTrue();
        
        // Assert: Goal progress
        Goal updatedGoal = goalService.read(savingsGoal.getId());
        double goalProgress = (updatedGoal.getBalance() / updatedGoal.getTarget()) * 100;
        assertThat(goalProgress).isEqualTo(20.0);
    }

    @Test
    @Order(9)
    @DisplayName("E2E: Data persistence and service layer integration")
    void shouldPersistDataAcrossServiceReloads() {
        // Act: Create a transaction
        Wallet wallet = new Wallet("Green", 100.0, "Test Wallet");
        walletService.create(wallet);
        
        Transaction transaction = new Transaction(
            "CAT_001",
            250.00,
            "Test Transaction",
            0.0,
            wallet.getId(),
            "2026-01-31T10:00:00"
        );
        transactionService.create(transaction);
        String transactionId = transaction.getId();
        
        // Act: Create new service instance (simulating app restart)
        TransactionService newTransactionService = new TransactionService();
        
        // Assert: Verify transaction persisted
        Transaction retrieved = newTransactionService.read(transactionId);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getName()).isEqualTo("Test Transaction");
        assertThat(retrieved.getAmount()).isEqualTo(250.00);
    }
}
