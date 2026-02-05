# Quick Migration Guide - Controller Refactoring

## 🎯 Goal
Migrate each controller to follow SOLID principles and MVC pattern.

---

## 📋 Step-by-Step Migration Process

### For Each Controller: Follow This Pattern

#### Step 1: Analyze Current Controller
Identify:
- [ ] What business logic exists?
- [ ] What data access happens?
- [ ] What navigation callbacks exist?
- [ ] What calculations are performed?

#### Step 2: Extract Business Logic to Services
Move to appropriate service:
- Budget calculations → `BudgetCalculationService`
- Goal calculations → `GoalProgressService`
- Transaction filtering → `TransactionFilterService`
- New logic → Create new service

#### Step 3: Create ViewModel
```java
public class [Feature]ViewModel {
    // Services (injected)
    private final [Required]Service service;
    
    // Observable properties for UI binding
    private final ObservableList<Model> items = FXCollections.observableArrayList();
    private final StringProperty statusText = new SimpleStringProperty();
    
    public [Feature]ViewModel(RequiredService service) {
        this.service = service;
    }
    
    public void loadData() {
        // Load and prepare data
        items.setAll(service.getItems());
    }
    
    // Getters for binding
    public ObservableList<Model> getItems() { return items; }
    public StringProperty statusTextProperty() { return statusText; }
}
```

#### Step 4: Register ViewModel in ApplicationContext
```java
// In ApplicationContext.registerViewModels()
ServiceLocator.registerFactory([Feature]ViewModel.class,
    () -> new [Feature]ViewModel(
        ServiceLocator.get(RequiredService.class)
    )
);
```

#### Step 5: Update Controller
```java
public class [Feature]Controller implements Initializable {
    
    private [Feature]ViewModel viewModel;
    private NavigationService navigationService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get dependencies
        viewModel = ServiceLocator.get([Feature]ViewModel.class);
        navigationService = ServiceLocator.get(NavigationService.class);
        
        // Setup
        bindViewModel();
        setupEventHandlers();
        
        // Load data
        viewModel.loadData();
    }
    
    private void bindViewModel() {
        // Bind UI to ViewModel properties
        someLabel.textProperty().bind(viewModel.statusTextProperty());
    }
    
    private void setupEventHandlers() {
        // Setup button clicks, etc.
        addButton.setOnAction(e -> handleAdd());
    }
    
    @FXML
    private void handleAdd() {
        // Delegate to ViewModel or Service
        viewModel.addItem(...);
    }
}
```

#### Step 6: Test
```bash
mvn clean compile
mvn javafx:run
```

---

## 🔥 Priority Order

### 1. TransactionsController (HIGH PRIORITY)
**Why:** Large, complex, frequently used

**Current Issues:**
- Direct DataStore access
- Filtering logic in controller
- Pagination logic mixed with UI

**Required Services:**
- ✅ TransactionFilterService (already created)
- TransactionService (already exists)
- CategoryService (already exists)

**Steps:**
1. Create `TransactionsViewModel`
2. Move filtering logic to ViewModel (use TransactionFilterService)
3. Move pagination logic to ViewModel
4. Update controller to use ViewModel
5. Replace navigation callbacks with NavigationService

**Estimated Time:** 30-45 minutes

---

### 2. GoalsController (HIGH PRIORITY)
**Why:** Complex goal management

**Current Issues:**
- Direct DataStore access
- Goal calculation in controller
- Dialog management in controller

**Required Services:**
- ✅ GoalProgressService (already created)
- GoalService (already exists)

**Steps:**
1. Create `GoalsViewModel`
2. Move goal calculations to ViewModel
3. Use GoalProgressService for progress calculations
4. Update controller to use ViewModel

**Estimated Time:** 30 minutes

---

### 3. BudgetController (MEDIUM PRIORITY)
**Why:** Budget management

**Required Services:**
- ✅ BudgetCalculationService (already created)
- BudgetService (already exists)

**Steps:**
1. Create `BudgetViewModel`
2. Use BudgetCalculationService
3. Update controller

**Estimated Time:** 20 minutes

---

### 4. ReportsController (MEDIUM PRIORITY)
**Why:** Report generation

**Current Issues:**
- Chart generation in controller
- Data aggregation in controller

**Required Services:**
- Create `ReportGenerationService`

**Steps:**
1. Create `ReportGenerationService`
2. Create `ReportsViewModel`
3. Move data aggregation to service
4. Update controller

**Estimated Time:** 45 minutes

---

### 5. AccountsController (LOW PRIORITY)
**Why:** Simpler controller

**Estimated Time:** 15 minutes

---

## 🛠️ Template Files

### ViewModel Template
```java
package gitgud.pfm.viewmodels;

import gitgud.pfm.services.*;
import javafx.beans.property.*;
import javafx.collections.*;

public class [Feature]ViewModel {
    
    private final [Service] service;
    
    // Observable data
    private final ObservableList<Model> items = FXCollections.observableArrayList();
    
    public [Feature]ViewModel([Service] service) {
        this.service = service;
    }
    
    public void loadData() {
        items.setAll(service.getAll());
    }
    
    public ObservableList<Model> getItems() {
        return items;
    }
}
```

### Controller Template
```java
package gitgud.pfm.Controllers;

import gitgud.pfm.infrastructure.ServiceLocator;
import gitgud.pfm.viewmodels.*;
import gitgud.pfm.services.navigation.NavigationService;
import javafx.fxml.*;
import java.net.URL;
import java.util.ResourceBundle;

public class [Feature]Controller implements Initializable {
    
    private [Feature]ViewModel viewModel;
    private NavigationService navigationService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewModel = ServiceLocator.get([Feature]ViewModel.class);
        navigationService = ServiceLocator.get(NavigationService.class);
        
        setupUI();
        viewModel.loadData();
    }
    
    private void setupUI() {
        // Bind and setup
    }
}
```

---

## ✅ Migration Checklist Template

Copy this for each controller:

### [Controller Name] Migration

- [ ] Analyzed current code
- [ ] Identified business logic
- [ ] Created/Identified required services
- [ ] Created ViewModel
- [ ] Registered ViewModel in ApplicationContext
- [ ] Updated Controller code
- [ ] Removed DataStore access
- [ ] Replaced static callbacks with NavigationService
- [ ] Compiled successfully (`mvn compile`)
- [ ] Tested in running application
- [ ] Verified all features work
- [ ] Committed changes

---

## 🚨 Common Pitfalls

### 1. Forgetting to Register ViewModel
**Error:** `IllegalStateException: Service not registered`
**Fix:** Add to `ApplicationContext.registerViewModels()`

### 2. Circular Dependencies
**Error:** Services depend on each other
**Fix:** Restructure, create intermediate service, or use events

### 3. JavaFX Thread Issues
**Error:** UI updates fail
**Fix:** Use `Platform.runLater()` for async updates

### 4. Missing Module Exports
**Error:** Package not accessible
**Fix:** Add exports to `module-info.java`

---

## 📊 Progress Tracking

| Controller | Status | ViewModel | Services | Tested |
|------------|--------|-----------|----------|--------|
| Dashboard | ✅ Example | DashboardViewModel | Budget, Goal, Transaction | ✅ |
| Transactions | ⏳ TODO | - | TransactionFilter | - |
| Goals | ⏳ TODO | - | GoalProgress | - |
| Budget | ⏳ TODO | - | BudgetCalculation | - |
| Reports | ⏳ TODO | - | TBD | - |
| Accounts | ⏳ TODO | - | TBD | - |

---

## 🎓 Learning Resources

**SOLID Principles:**
- See: `SOLID_MVC_REFACTORING_PLAN.md`
- Focus on: Single Responsibility, Dependency Inversion

**MVVM Pattern:**
- Controllers = View coordinators
- ViewModels = Presentation logic
- Services = Business logic

**Best Practices:**
- Keep controllers thin (<200 lines)
- Put calculations in services
- Use observables for reactive UI
- Inject dependencies, don't create them

---

## 💡 Tips

1. **Migrate one controller at a time**
   - Don't try to do everything at once
   - Test after each migration

2. **Keep both old and new code temporarily**
   - Create `[Controller]Refactored.java` first
   - Test thoroughly
   - Then replace old controller

3. **Use the example as reference**
   - `DashboardControllerRefactored.java` shows the pattern
   - Copy the structure

4. **Don't over-engineer**
   - Start simple
   - Refine later if needed

---

**Total Estimated Time:** 2-3 hours for all controllers
**Recommended Approach:** One controller per day, test thoroughly
