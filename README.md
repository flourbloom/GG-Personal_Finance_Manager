# Personal Finance Manager

Modern JavaFX + CLI application for managing wallets, transactions, budgets, goals, and reports on top of a lightweight SQLite store.

## Feature Highlights
- Visual JavaFX dashboard with animated cards, charts, and quick navigation.
- Wallet, transaction, budget, and goal management with double-click editing and contextual dialogs.
- CLI companion (`CLIApp`) for scripted workflows or headless environments.
- Shared service layer backed by SQLite, keeping GUI and CLI in sync.

## Prerequisites
- JDK 21 or newer.
- Maven 3.9+ with `mvn` on your `PATH`.
- No external database required; an embedded SQLite file (`GG_Personal_Finance.db`) is created automatically.

## Running the Project

### JavaFX GUI
```bash
mvn clean javafx:run
```
Use `mvn javafx:run` for quicker iterations when you do not need a clean build. A VS Code task named **Run GUI (JavaFX)** is also available.

### CLI Application
```bash
mvn clean exec:java -Dexec.mainClass="gitgud.pfm.CLIApp"
```
Subsequent runs can drop the `clean` phase. The **Run CLI** VS Code task mirrors this command.

### Building & Packaging
```bash
mvn clean package
```
Produces two runnable artifacts in `target/`:
- `pfm-gui.jar` – JavaFX desktop app.
- `pfm-cli.jar` – CLI application.

Run the packaged binaries with:
```bash
java -jar target/pfm-gui.jar
java -jar target/pfm-cli.jar
```

## User Guide

### Launch & Navigation
1. Start the GUI and log in to the dashboard landing page.
2. Use the sidebar to switch between Dashboard, Transactions, Reports, Goals, Wallets, and Budget views.
3. Contextual buttons (e.g., **Add Wallet**, **Add Goal**) open dialogs sized for that workflow.

### Wallets
1. Go to **Wallets** and click **Add Wallet** to define name, color, and opening balance.
2. Double-click a wallet card to adjust balances, rename, or delete the wallet.
3. Deleting a wallet permanently removes its linked transactions—the dialog highlights this in red so you can proceed confidently.

### Transactions
1. In **Transactions**, press **Add Transaction** to categorize income/expenses against a wallet.
2. The ledger displays wallet names alongside amounts; double-click any entry to edit.
3. Filters and search inputs narrow the list; totals update automatically after each change.

### Budgets
1. Open **Budget**, then click **New Budget**.
2. Choose a period (weekly/monthly/yearly/custom), optionally bind it to a wallet or category, set spending limits, and confirm.
3. Double-click a budget row to edit its dates, targets, or associations; use the delete action to retire obsolete budgets.
4. Progress indicators compare actual transaction totals against the budget limit so you can react before overspending.

### Goals
1. Navigate to **Goals** and pick **Add Goal** to define the target amount, starting balance, deadline, and priority.
2. Assigning a wallet keeps progress tied to a funding source; otherwise goals stay account-wide.
3. Double-click to edit; the controller recomputes progress from recorded transactions, so the status bars remain accurate.

### Reports
Use the **Reports** view to visualize income vs. expenses, category splits, and wallet health. Most charts support hover tooltips for raw values.

### CLI Highlights
Launch the CLI binary or `mvn exec:java -Dexec.mainClass="gitgud.pfm.CLIApp"` to:
- List wallets, budgets, goals, categories, and transactions.
- Add or edit data interactively with prompts mirroring the GUI fields.
- Export totals for scripting or automated testing.

## Technical Overview

| Layer | Responsibilities | Key Classes |
| --- | --- | --- |
| JavaFX UI | FXML-defined layouts (`dashboard.fxml`, `wallets.fxml`, etc.) plus `main.css` styling. | `App`, FXML files, CSS |
| Controllers | Handle user events, animations, and dialog flows for each screen. | `DashboardController`, `WalletsController`, `TransactionsController`, `BudgetController`, `GoalsController`, `SidebarController`, etc. |
| Services | CRUD abstraction for each aggregate plus shared `AccountDataLoader` facade. | `WalletService`, `TransactionService`, `GoalService`, `BudgetService`, `AccountDataLoader` |
| Models | POJOs representing domain entities. Wallets, budgets, and goals inherit from `FinancialEntity`. | `Wallet`, `Budget`, `Goal`, `Transaction`, `Category` |
| Persistence | SQLite connection + schema bootstrapper. | `Database`, `DatabaseInitializer`

Data Flow:
1. UI actions bubble to controllers (e.g., `WalletsController`).
2. Controllers call `AccountDataLoader`, which coordinates the specialized services.
3. Services execute SQL via shared `Database` connection; schema is created on first launch.
4. Observer callbacks in `AccountDataLoader` notify controllers so the UI refreshes after mutations.

CLI flows reuse the same loader/services so both interfaces stay consistent.

## FinancialEntity API

### Base Class: `FinancialEntity`
- **Fields:** `id`, `name`, `balance`.
- **Constructors:**
	- `FinancialEntity(String id, String name, double balance)` – assigns the base state.
- **Core Methods:**
	- `getId()/setId()` – unique identifier (UUID-like helper via `IdGenerator`).
	- `getName()/setName()` – display label for UI components.
	- `getBalance()/setBalance()` – current numeric value in the entity’s native currency.
	- `addToBalance(double amount)` / `subtractFromBalance(double amount)` – utility helpers used when reconciling transactions.
	- `toString()` – debug-friendly summary (`ClassName{id='...', ...}`).

### `Wallet extends FinancialEntity`
- **Extra Fields:** `color` (hex string used for card gradients).
- **Constructors:**
	- No-arg default for ORM mappers.
	- `Wallet(String color, double balance, String name)` – auto-generates an ID and stores UI tint + starting balance.
- **Methods:** `getColor()/setColor()`.

### `Budget extends FinancialEntity`
- **Extra Fields:** `limitAmount`, `startDate`, `endDate`, `PeriodType` (`WEEKLY`, `MONTHLY`, `YEARLY`, `CUSTOM`), optional `walletId`, optional `categoryId`.
- **Constructors:** overloads for account-wide budgets, wallet-specific budgets, or category-focused budgets. All generate IDs through `IdGenerator` and default to `PeriodType.MONTHLY` when unspecified.
- **Methods:** standard getters/setters plus `isAccountWide()` and `isCategoryBudget()` convenience checks.

### `Goal extends FinancialEntity`
- **Extra Fields:** `target`, `priority`, `createTime`, `deadline`, optional `walletId`, computed `txCount` and `progress` fields populated by `GoalService`.
- **Constructors:**
	- Default reflection constructor.
	- `Goal(String name, double target, double current, String deadline, double priority, String createTime)` for GUI flows.
	- `Goal(String name, double target, String deadline, double priority, String createTime)` for CLI flows where current balance is derived later.
- **Methods:** getters/setters for each field, enabling controllers to display priority sorting and progress bars.

## Additional Notes
- Maven’s `clean` goal wipes the `target/` directory to prevent stale class files from polluting a build; omit it to speed up iterative development.
- FXML `fx:id` and `onAction` bindings wire view elements to their controller methods (e.g., `onAction="#switchToSecondary"` calls `switchToSecondary()` in `PrimaryController`).

For questions or enhancements, open an issue or reach out to the maintainers.
