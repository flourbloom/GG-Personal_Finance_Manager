# Goal Refactor - Quick Reference Guide

## What Changed?

Goals no longer store a "balance" value. Instead, balance is **computed** from transactions allocated to that goal.

### Old Way ❌
```
Goal: Emergency Fund
- User creates goal with target: $5000 and current: $0
- User manually updates current to $1000
- User manually updates current to $1500
❌ Balance can be inconsistent with actual allocated transactions
```

### New Way ✅
```
Goal: Emergency Fund (target: $5000)
- User creates goal (no current amount needed)
- User allocates transaction 1 ($1000) to goal
- Balance automatically becomes: $1000
- User allocates transaction 2 ($500) to goal
- Balance automatically becomes: $1500
✅ Balance always = sum of allocated transactions
```

---

## Menu Option Changes

### Option 3: Add Transaction
**NEW:** Asks if you want to allocate transaction to a goal
```
Enter transaction name: Monthly Salary
Enter the amount: 1000
...
Allocate this transaction to a goal? (y/n): y
→ Select from available goals
→ Transaction allocated automatically
```

### Option 10: View All Goals
**CHANGED:** Shows transaction count and computed balance
```
Before:
Emergency Fund   $5,000.00   $0.00    1.0  2026-12-31  (0.0%)

After:
Emergency Fund   $5,000.00   $1,500.00   2   1.0  2026-12-31  (30.0%)
                                        ↑
                             Tx Count column
```

### Option 11: Add Goal
**REMOVED:** "Enter current amount" prompt
```
Before:
Enter goal name: Emergency Fund
Enter target amount: 5000
Enter current amount: 0      ← REMOVED
Enter deadline: 2026-12-31

After:
Enter goal name: Emergency Fund
Enter target amount: 5000
Enter deadline: 2026-12-31
[Balance starts at $0.00 automatically]
```

### Option 12: Update Goal
**CHANGED:** Balance is now read-only
```
Fields: name, target, deadline, priority, createat
        [no "balance" field anymore]

User tries to edit balance?
→ ❌ ERROR: Balance is READ-ONLY
→ To update: Add/edit transactions allocated to goal
```

### Option 4: Update Transaction
**NEW:** Can now link/unlink transactions from goals
```
Fields: name, amount, category, income, walletid, goalid, createtime
                                                    ↑
                                            NEW goalid field

User selects "goalid":
→ Shows available goals
→ Can select goal to allocate
→ Can enter "null" to unlink
```

### Option 2: View All Transactions
**NEW:** Shows Goal ID column
```
Before:
ID    Name          Category   Wallet   Amount    Date

After:
ID    Name          Category   Wallet   Amount    Goal ID       Date
                                                  ↑ New column
                                                  goal_001 or -
```

### Option 16: Delete Goal
**NEW:** Warning if goal has allocated transactions
```
⚠️  WARNING: This goal has 2 transaction(s) allocated to it.
These transactions will be unlinked. Continue? (y/n): y
→ Goal deleted, transactions remain (unlinked)
```

---

## Field Names (for reference)

When updating Transaction or Goal, remember these field names:

### Transaction Fields
- `name` - Transaction name
- `amount` - Dollar amount
- `category` - Category type
- `income` - 0 for expense, 1 for income
- `walletid` - Which wallet/account
- `goalid` - Which goal (if any) ← **NEW**
- `createtime` - When created

### Goal Fields
- `name` - Goal name
- `target` - Target amount
- `deadline` - Target date
- `priority` - Importance level
- `createat` - When created
- ~~`balance`~~ - **REMOVED** (read-only now)

---

## Example Workflow

### Step 1: Create Goal
```
[Menu] → 11 [Add Goal]
Enter goal name: Emergency Fund
Enter target amount: 5000
Enter deadline: 2026-12-31
Enter priority: 1

Output:
Goal created: Emergency Fund
Target: $5,000.00
Current Balance: $0.00 (will update as transactions are allocated)
```

### Step 2: Add Income Transaction
```
[Menu] → 3 [Add Transaction]
Select category: Income
Enter transaction name: Monthly Salary
Enter amount: 1000
Select wallet: 1. Cash
Allocate to goal? y
Select goal: 1. Emergency Fund

Output:
✓ Transaction created: Monthly Salary

=== Goal Updated ===
Goal: Emergency Fund
New Balance: $1,000.00
Target: $5,000.00
Progress: 20.0%
[██████░░░░░░░░░░░░░░░░░░░░░░]
```

### Step 3: View Goals
```
[Menu] → 10 [View All Goals]

Goals (Balance computed from allocated transactions):
─────────────────────────────────────────────────
ID         Name            Target      Current     Tx Count  Deadline
─────────────────────────────────────────────────
goal_001   Emergency Fund  $5,000.00   $1,000.00   1         2026-12-31 (20.0%)
─────────────────────────────────────────────────
```

### Step 4: Unlink Transaction
```
[Menu] → 4 [Edit Transaction]
Enter transaction name: Monthly Salary
Enter field to update: goalid

Available Goals (or enter 'null' to unlink):
  1. Emergency Fund (Balance: $1,000.00)
Enter 'null': null

Output:
Transaction unlinked from goal.
Transaction updated.

[Now Emergency Fund balance = $0.00]
```

---

## Common Questions

### Q: How do I increase a goal's balance?
**A:** Add transactions and allocate them to the goal. Balance updates automatically.

### Q: How do I decrease a goal's balance?
**A:** Unlink transactions from the goal (Option 4 → goalid → null). Balance updates automatically.

### Q: What if I delete a goal?
**A:** Transactions stay in the system but are unlinked (goalId set to null). You'll be warned first.

### Q: What if I delete a transaction?
**A:** The transaction is removed. If it was allocated to a goal, that goal's balance automatically decreases.

### Q: Can I manually set goal balance?
**A:** No. Balance is computed automatically from allocated transactions. This ensures data consistency.

### Q: Why no "current amount" when creating goals?
**A:** Because it's redundant. The current amount is always the sum of allocated transactions, so asking for it upfront doesn't make sense.

---

## Important Reminders

⚠️ **Balance is computed, not stored**
- Every time you view a goal, its balance is recalculated
- Balance = SUM of all transactions where goalId matches
- Never manually edit balance in database

✅ **Transactions are separate from goals**
- Deleting a goal does NOT delete transactions
- Transactions can exist without a goal (goalId = null)
- One transaction can only belong to one goal (goalId is a single value)

✅ **Data stays consistent**
- You can't get out of sync between goal balance and transactions
- If a transaction is missing, recalculate will show the correct total
- No manual balance adjustments needed

---

## Testing Tips

1. **Create multiple goals** with different targets
2. **Allocate multiple transactions** to same goal
3. **View goals** - verify balance is sum of all allocated transactions
4. **Unlink transaction** - verify balance decreases by that amount
5. **Delete goal** - verify transactions still exist but are unlinked
6. **Delete transaction** - verify goal balance updates
7. **Update transaction goalid** - verify it can be allocated/unlinked

---

## Visual Progress Indicator

When allocating transactions to goals, you'll see a progress bar:

```
[████████░░░░░░░░░░░░░░░░░░░░]  20%
[████████████████░░░░░░░░░░░░░]  50%
[██████████████████████████████] 100%
```

- █ = Completed portion
- ░ = Remaining portion
- Updates in real-time as you add transactions

---

Last Updated: February 3, 2026  
Status: ✅ Ready for Testing
