# Goal Refactor - Test Execution Guide

## Prerequisites ✅

Before starting tests, ensure:

1. ✅ Database migration completed (balance removed from Goal table, goalId added to transaction table)
2. ✅ Goal.java constructor updated (removed "current" parameter)
3. ✅ Transaction.java has `goalId` field with getters/setters
4. ✅ CliController.java code changes implemented (this document's changes)
5. ✅ Project compiles without errors
6. ✅ All services (GoalService, TransactionService) can handle goalId

---

## Test Setup

### Start the CLI
```bash
mvn compile exec:java
```

You should see:
```
Welcome to the Personal Finance Manager CLI!
-------------------------------------------
Main Menu:
1. View Account Summary
...
```

### Clear Previous Data (Optional)
If testing fresh, delete or clear the database to start with clean state.

---

## Test 1: Create Goal Without Balance Field ✅

**Objective:** Verify Goal creation no longer asks for current amount

**Steps:**
1. Select **Option 11** (Add Goal)
2. Enter `Emergency Fund` as name
3. Enter `5000` as target amount
4. **Verify:** No prompt for "current amount"
5. Enter `2026-12-31` as deadline
6. Enter `1` as priority
7. Press Enter for creation time (use current time)

**Expected Result:**
```
=== Add Goal ===
Enter goal name: Emergency Fund
Enter target amount: 5000
[NO PROMPT HERE for current amount!]
Enter deadline (YYYY-MM-DD): 2026-12-31
Enter priority (numeric): 1
Enter creation time (YYYY-MM-DD or leave blank for now): [Enter]

Goal created: Emergency Fund
Target: $5,000.00
Current Balance: $0.00 (will update as transactions are allocated)
```

**✅ PASS** if:
- [ ] No prompt for "current amount"
- [ ] Goal created with $0.00 balance
- [ ] Confirmation message shows target amount
- [ ] Menu returns to main

---

## Test 2: Add Transaction Without Goal Allocation ✅

**Objective:** Verify transaction can be created without allocating to goal

**Steps:**
1. Select **Option 3** (Add Transaction)
2. Select **Income** category (e.g., Salary)
3. Enter `Freelance Work` as name
4. Enter `500` as amount
5. Select wallet `1` (Cash)
6. At "Allocate to goal?" prompt, enter `n`
7. Verify transaction created

**Expected Result:**
```
=== Add Transaction ===
[... category selection ...]
Enter transaction name: Freelance Work
Enter the amount: 500
Income set to 1 (income).
Pick an account:
  1. Cash (Balance: $0.00)
  2. Card (Balance: $0.00)
Enter the number of the account: 1
You selected: [wallet_id]

Allocate this transaction to a goal? (y/n): n
[No goal selection shown]

✓ Transaction created: Freelance Work
```

**✅ PASS** if:
- [ ] Goal allocation skipped when user enters 'n'
- [ ] Transaction created without goal
- [ ] No goal update message shown

---

## Test 3: Add Transaction WITH Goal Allocation ✅

**Objective:** Verify transaction allocation to goal and automatic balance update

**Steps:**
1. Select **Option 3** (Add Transaction)
2. Select **Income** category
3. Enter `Monthly Salary` as name
4. Enter `1000` as amount
5. Select wallet `1` (Cash)
6. At "Allocate to goal?" prompt, enter `y`
7. Select goal `1` (Emergency Fund)
8. Verify transaction created and goal updated

**Expected Result:**
```
Allocate this transaction to a goal? (y/n): y

Available Goals:
─────────────────────────────────────────────────────────────────
#    Name                 Target       Current      Progress
─────────────────────────────────────────────────────────────────
1    Emergency Fund       $5,000.00    $0.00        0.0%
─────────────────────────────────────────────────────────────────

Select goal number (or 0 to skip): 1
✓ Transaction will be allocated to: Emergency Fund

✓ Transaction created: Monthly Salary

=== Goal Updated ===
Goal: Emergency Fund
New Balance: $1,000.00 ← Automatically computed!
Target: $5,000.00
Progress: 20.0%
[██████████░░░░░░░░░░░░░░░░░░]
```

**✅ PASS** if:
- [ ] Shows available goals with target and current balance
- [ ] Displays goal selection prompt
- [ ] Confirms goal allocation
- [ ] Transaction created successfully
- [ ] Shows "Goal Updated" message
- [ ] New balance = $1,000.00 (automatically computed!)
- [ ] Progress shows 20.0% (1000/5000)
- [ ] Progress bar displays correctly

---

## Test 4: View Goals with Computed Balance ✅

**Objective:** Verify goal balance is computed from allocated transactions

**Steps:**
1. Select **Option 10** (View All Goals)
2. Verify Emergency Fund shows balance of $1,000.00
3. Count transactions: should show 1 allocated

**Expected Result:**
```
=== All Goals ===

Goals (Balance computed from allocated transactions):
─────────────────────────────────────────────────────────────────
ID              Name                 Target       Current    Tx Count  Priority  Deadline
─────────────────────────────────────────────────────────────────
goal_001        Emergency Fund       $5,000.00    $1,000.00  1         1.0       2026-12-31 (20.0%)
─────────────────────────────────────────────────────────────────
Total goals: 1

💡 Tip: Allocate transactions to goals when adding them (Option 3)
```

**✅ PASS** if:
- [ ] Balance shows $1,000.00 (computed from transaction)
- [ ] Tx Count shows 1
- [ ] Progress shows 20.0%
- [ ] Helpful tip is displayed

---

## Test 5: Add Another Transaction to Same Goal ✅

**Objective:** Verify multiple transactions can be allocated to same goal and balance updates

**Steps:**
1. Select **Option 3** (Add Transaction)
2. Select **Income** category
3. Enter `Performance Bonus` as name
4. Enter `500` as amount
5. Select wallet
6. Allocate to goal `1` (Emergency Fund)

**Expected Result:**
```
✓ Transaction created: Performance Bonus

=== Goal Updated ===
Goal: Emergency Fund
New Balance: $1,500.00 ← Updated!
Target: $5,000.00
Progress: 30.0%
[███████████░░░░░░░░░░░░░░░░░]
```

Then check Option 10 again:
```
Emergency Fund    $5,000.00    $1,500.00   2    1.0    2026-12-31  (30.0%)
                                           ↑
                                        Now shows 2 transactions
```

**✅ PASS** if:
- [ ] New balance = $1,500.00 (automatic!)
- [ ] Progress shows 30.0%
- [ ] Tx Count increased to 2
- [ ] No manual balance updates needed

---

## Test 6: View Transactions with Goal ID Column ✅

**Objective:** Verify transaction list shows goal allocation status

**Steps:**
1. Select **Option 2** (View All Transactions)
2. Verify "Goal ID" column shows
3. Monthly Salary and Bonus should show goal_001
4. Freelance Work should show `-`

**Expected Result:**
```
=== All Transactions ===

Transactions (most recent first):
───────────────────────────────────────────────────────────────────────────
ID        Name                 Category    Wallet   Amount    Goal ID       Date
───────────────────────────────────────────────────────────────────────────
tx_003    Performance Bonus    Salary      Cash     +$500.00  goal_001      2026-02-03
tx_002    Monthly Salary       Salary      Cash     +$1,000.00 goal_001     2026-02-03
tx_001    Freelance Work       Salary      Cash     +$500.00  -             2026-02-03
───────────────────────────────────────────────────────────────────────────
```

**✅ PASS** if:
- [ ] Goal ID column displays
- [ ] Allocated transactions show goal_001
- [ ] Unallocated transaction shows `-`
- [ ] All transactions visible

---

## Test 7: Update Transaction - Try to Edit Balance (Should Fail) ✅

**Objective:** Verify balance field is read-only in Update Goal

**Steps:**
1. Select **Option 12** (Update Goal)
2. View current goals
3. Enter `Emergency Fund` as goal name
4. When asked for field, enter `balance`

**Expected Result:**
```
⚠️  Note: 'balance' is READ-ONLY (computed from allocated transactions)
Fields you can edit: name, target, deadline, priority, createat
Enter field to update: balance

❌ ERROR: Balance is READ-ONLY and computed from transactions.
To update balance:
  1. Add new transactions and allocate them to this goal (Option 3)
  2. Edit existing transactions to link/unlink them (Option 4)

Current balance: $1,500.00
Transactions allocated: 2
```

**✅ PASS** if:
- [ ] Warning message displays
- [ ] Balance field NOT in editable fields list
- [ ] Error message explains why
- [ ] Shows current balance
- [ ] Shows transaction count
- [ ] Returns to main menu (doesn't crash)

---

## Test 8: Update Transaction - Link/Unlink from Goal ✅

**Objective:** Verify transactions can be allocated/unlinked from goals via update

**Steps:**
1. Select **Option 4** (Update Transaction)
2. View current transactions
3. Select `Freelance Work` transaction
4. Enter `goalid` as field to update

**Expected Result:**
```
Fields: name, amount, category, income, walletid, goalid, createtime
Enter field to update: goalid

Available Goals (or enter 'null' to unlink):
  1. Emergency Fund (Balance: $1,500.00 / Target: $5,000.00)
Enter goal number (or 'null' to unlink): 1
You selected: Emergency Fund
Transaction updated.
```

Check Option 10 - Emergency Fund balance should now be $2,000.00 with 3 transactions

**✅ PASS** if:
- [ ] Shows available goals
- [ ] Can select goal number
- [ ] Transaction linked to goal
- [ ] Balance automatically updates

### Test 8b: Unlink Transaction
**Steps:**
1. Select **Option 4** again
2. Select `Freelance Work`
3. Enter `goalid`
4. Enter `null` to unlink

**Expected Result:**
```
Enter goal number (or 'null' to unlink): null
Transaction unlinked from goal.
Transaction updated.
```

Check Option 10 - Emergency Fund balance should return to $1,500.00 with 2 transactions

**✅ PASS** if:
- [ ] Accepts `null` to unlink
- [ ] Transaction unlinked
- [ ] Balance automatically decreased
- [ ] Tx Count decreased

---

## Test 9: Delete Goal with Allocated Transactions ✅

**Objective:** Verify warning when deleting goal with transactions

**Steps:**
1. Select **Option 16** (Delete Goal)
2. Enter `Emergency Fund`

**Expected Result:**
```
⚠️  WARNING: This goal has 2 transaction(s) allocated to it.
These transactions will be unlinked. Continue? (y/n): y

Confirming deletion of Goal: Emergency Fund (Target Amount: 5000.0)
SUCCESS: Goal 'Emergency Fund' has been deleted successfully.
```

Then check **Option 2** - transactions should still exist but Goal ID shows `-`

**✅ PASS** if:
- [ ] Shows warning with transaction count
- [ ] Asks for confirmation
- [ ] Accepts `y` to confirm
- [ ] Goal deleted
- [ ] Transactions NOT deleted (still in system)
- [ ] Transactions unlinked (goalId = null)

### Test 9b: Cancel Delete
**Steps:**
1. Select **Option 16** again
2. Create another goal first (Option 11)
3. Try to delete it
4. At warning, enter `n`

**Expected Result:**
```
Deletion cancelled.
[Returns to main menu without deleting]
```

**✅ PASS** if:
- [ ] Cancellation works with `n` input
- [ ] Goal still exists
- [ ] No deletion occurs

---

## Test 10: Complete Workflow - Empty Goal ✅

**Objective:** Verify goal with no transactions shows $0.00 balance

**Steps:**
1. Create new goal: `Vacation Fund`
2. Target: 3000
3. Don't allocate any transactions
4. View goals (Option 10)

**Expected Result:**
```
vacation_001    Vacation Fund         $3,000.00    $0.00          0         1.0  2026-12-31 (0.0%)
```

**✅ PASS** if:
- [ ] Shows $0.00 balance (correct!)
- [ ] Shows 0 transactions
- [ ] Shows 0.0% progress
- [ ] No errors

---

## Test 11: Add Expense to Goal ✅

**Objective:** Verify income and expense transactions can be allocated to goals

**Steps:**
1. Select **Option 3** (Add Transaction)
2. Select **Expense** category (e.g., Food)
3. Enter `Groceries` as name
4. Enter `100` as amount
5. Allocate to Emergency Fund (if still exists) or Vacation Fund

**Expected Result:**
```
Income set to 0 (expense).
...
Allocate this transaction to a goal? (y/n): y
Select goal: 1. Vacation Fund

✓ Transaction created: Groceries

=== Goal Updated ===
Goal: Vacation Fund
New Balance: $-100.00  ← Can be negative!
Progress: -3.3%
```

**✅ PASS** if:
- [ ] Expense transactions can be allocated
- [ ] Balance can go negative (if allocating expenses)
- [ ] No error handling needed for negative balance

---

## Test 12: Stress Test - Multiple Goals ✅

**Objective:** Verify system handles multiple goals correctly

**Steps:**
1. Create 3 goals:
   - Emergency Fund ($5000)
   - Vacation Fund ($3000)
   - Car Fund ($10000)
2. Add 6 transactions, allocate to different goals:
   - $1000 → Emergency Fund
   - $500 → Emergency Fund
   - $1500 → Vacation Fund
   - $800 → Vacation Fund
   - $2000 → Car Fund
   - $1000 → Car Fund
3. View goals (Option 10)

**Expected Result:**
```
Emergency Fund    $5,000.00    $1,500.00   2    ...   (30.0%)
Vacation Fund     $3,000.00    $2,300.00   2    ...   (76.7%)
Car Fund         $10,000.00   $3,000.00   2    ...   (30.0%)
```

**✅ PASS** if:
- [ ] Each goal shows correct balance (sum of allocated)
- [ ] Tx Count accurate for each
- [ ] Progress calculated correctly
- [ ] No cross-contamination between goals

---

## Test 13: Edge Cases ✅

### Test 13a: Update Goal Fields (Except Balance)
**Objective:** Verify other goal fields can be updated

**Steps:**
1. Select **Option 12** (Update Goal)
2. Select Emergency Fund
3. Update `name` to "Emergency Fund 2024"
4. Verify it updated

**✅ PASS** if:
- [ ] Name updated successfully
- [ ] Other fields (target, deadline, priority) also work
- [ ] Only balance is read-only

### Test 13b: Allocate to Non-Existent Goal
**Steps:**
1. Select **Option 3** (Add Transaction)
2. Try to allocate to a goal that was deleted
3. Should not show in list

**✅ PASS** if:
- [ ] Deleted goals don't appear in allocation list
- [ ] No referential integrity errors

### Test 13c: View Empty System
**Steps:**
1. Start fresh (no goals)
2. Option 10 (View Goals)
3. Should show "No goals found"

**✅ PASS** if:
- [ ] Handles empty state gracefully

---

## Data Verification Checklist

After completing all tests, verify database integrity:

```sql
-- Check Goal table structure (no balance column)
PRAGMA table_info(Goal);
-- Should have: id, name, target, deadline, priority, createAt
-- Should NOT have: balance

-- Check transaction_records has goalId
PRAGMA table_info(transaction_records);
-- Should have: ..., goalId, ...

-- Verify computed balances
SELECT 
    g.id,
    g.name,
    g.target,
    (SELECT SUM(t.amount) FROM transaction_records t WHERE t.goalId = g.id) as computed_balance
FROM Goal g;

-- Verify transaction allocations
SELECT id, name, goalId FROM transaction_records WHERE goalId IS NOT NULL;
```

---

## Test Summary Report Template

```
=== GOAL REFACTOR TEST REPORT ===

Date: _______________
Tester: _______________

Test Results:
[ ] Test 1:  Create Goal Without Balance - PASS/FAIL
[ ] Test 2:  Add Transaction Without Goal - PASS/FAIL
[ ] Test 3:  Add Transaction With Goal - PASS/FAIL
[ ] Test 4:  View Goals Computed Balance - PASS/FAIL
[ ] Test 5:  Multiple Transactions/Goal - PASS/FAIL
[ ] Test 6:  View Transactions Goal ID - PASS/FAIL
[ ] Test 7:  Balance Read-Only - PASS/FAIL
[ ] Test 8:  Link/Unlink Transaction - PASS/FAIL
[ ] Test 9:  Delete Goal Warning - PASS/FAIL
[ ] Test 10: Empty Goal Balance - PASS/FAIL
[ ] Test 11: Expense to Goal - PASS/FAIL
[ ] Test 12: Multiple Goals - PASS/FAIL
[ ] Test 13: Edge Cases - PASS/FAIL

Data Integrity:
[ ] No balance column in Goal
[ ] goalId in transaction_records
[ ] Computed balances match
[ ] No orphaned transactions

Overall Result: _______________
Issues Found: (list any bugs)
_______________
_______________

Approved By: _______________
Date: _______________
```

---

## Success Criteria

✅ **All tests pass if:**
1. Goal balance never asked when creating
2. Balance automatically computed from allocated transactions
3. Multiple transactions sum correctly
4. Balance cannot be manually edited
5. Transactions can be allocated/unlinked
6. Goal deletion warns about allocated transactions
7. Deleted goals unlink (don't cascade delete) transactions
8. All UI displays correct computed values
9. No database balance column
10. goalId column exists and works

---

Status: Ready for Testing  
Last Updated: February 3, 2026
