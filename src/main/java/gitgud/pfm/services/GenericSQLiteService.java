package gitgud.pfm.services;

 
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * Generic CRUD - Map-Based API
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * All operations use a Map<String, Object> for configuration.
 * 
 * REQUIRED Map Keys:
 * - "table" : String - Table name (e.g., "products", "orders")
 * - "class" : Class<?> - Entity class (e.g., Product.class)
 * 
 * OPTIONAL Map Keys (depending on operation):
 * - "pk" : String - Primary key column name (default: "id")
 * - "entity" : Object - Entity object to insert/update
 * - "id" : Object - Primary key value for read/update/delete
 * - "updates" : Map<String, Object> - Column->value pairs for update
 * - "filters" : Map<String, Object> - WHERE clause filters (AND logic)
 * - "columns" : List<String> - Columns to select (default: all "*")
 * - "orderBy" : String - ORDER BY clause (e.g., "price DESC")
 * - "limit" : Integer - LIMIT clause (max rows to return)
 * 
 * ─────────────────────────────────────────────────────────────────────────
 * AUTO-SAVE TO DATABASE (Uncomment to enable)
 * ─────────────────────────────────────────────────────────────────────────
 * Map<String, Object> config = new HashMap<>();
 * config.put("class", replacewithclassname.class);
 * config.put("table", "replacewithtablename");
 * config.put("entity", this);
 * GenericSQLiteService.create(config);
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * USAGE EXAMPLES (CRUD Order)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * ───────────────────────────────────────────────────────────────────────────────
 * CREATE - Insert new record
 * ───────────────────────────────────────────────────────────────────────────────
 * 
 * Transaction t = new Transaction("txn_123", 100.50, "2026-01-28");
 * Map<String, Object> config = new HashMap<>();
 * config.put("class", Transaction.class);
 * config.put("table", "transactions");
 * config.put("entity", t);
 * GenericSQLiteService.create(config);
 * 
 * ★ AUTO-SAVE IN CONSTRUCTOR:
 * public Transaction(String id, double amount, String date) {
 * this.id = id;
 * this.amount = amount;
 * this.date = date;
 * 
 * Map<String, Object> config = new HashMap<>();
 * config.put("class", Transaction.class);
 * config.put("table", "transactions");
 * config.put("entity", this);
 * GenericSQLiteService.create(config);
 * }
 * 
 * ───────────────────────────────────────────────────────────────────────────────
 * READ - Get single record by ID
 * ───────────────────────────────────────────────────────────────────────────────
 * 
 * Map<String, Object> config = new HashMap<>();
 * config.put("class", Transaction.class);
 * config.put("table", "transactions");
 * config.put("id", "txn_123");
 * Transaction t = GenericSQLiteService.read(config);
 * 
 * ───────────────────────────────────────────────────────────────────────────────
 * READ - Get multiple records (filtered, ordered, limited)
 * ───────────────────────────────────────────────────────────────────────────────
 * 
 * Map<String, Object> filters = new HashMap<>();
 * filters.put("category", "Food");
 * 
 * Map<String, Object> config = new HashMap<>();
 * config.put("class", Transaction.class);
 * config.put("table", "transactions");
 * config.put("filters", filters);
 * config.put("orderBy", "date DESC");
 * config.put("limit", 50);
 * List<Transaction> transactions = GenericSQLiteService.readAll(config);
 * 
 * ───────────────────────────────────────────────────────────────────────────────
 * READ - Get data as Maps (for GUI tables - no class needed)
 * ───────────────────────────────────────────────────────────────────────────────
 * 
 * Map<String, Object> config = new HashMap<>();
 * config.put("table", "transactions");
 * config.put("orderBy", "date DESC");
 * List<Map<String, Object>> rows = GenericSQLiteService.readAllAsMap(config);
 * // rows = [{"id": "txn_123", "amount": 100.50, "date": "2026-01-28"}, ...]
 * 
 * ───────────────────────────────────────────────────────────────────────────────
 * UPDATE - Update specific columns
 * ───────────────────────────────────────────────────────────────────────────────
 * 
 * Map<String, Object> updates = new HashMap<>();
 * updates.put("amount", 150.75);
 * updates.put("category", "Entertainment");
 * 
 * Map<String, Object> config = new HashMap<>();
 * config.put("class", Transaction.class);
 * config.put("table", "transactions");
 * config.put("id", "txn_123");
 * config.put("updates", updates);
 * GenericSQLiteService.update(config);
 * 
 * ───────────────────────────────────────────────────────────────────────────────
 * DELETE - Remove record by ID
 * ───────────────────────────────────────────────────────────────────────────────
 * 
 * Map<String, Object> config = new HashMap<>();
 * config.put("class", Transaction.class);
 * config.put("table", "transactions");
 * config.put("id", "txn_123");
 * GenericSQLiteService.delete(config);
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
public class GenericSQLiteService<T> {

    private final Class<T> entityClass;
    private final String tableName;
    private final String primaryKeyColumnName;

    private static final Map<String, GenericSQLiteService<?>> singletonInstances = new HashMap<>();

    private GenericSQLiteService(Class<T> entityClass, String tableName, String primaryKeyColumnName) {
        this.entityClass = entityClass;
        this.tableName = tableName;
        this.primaryKeyColumnName = primaryKeyColumnName == null || primaryKeyColumnName.isEmpty() ? "id"
                : primaryKeyColumnName;
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T> GenericSQLiteService<T> getInstance(Class<T> entityClass, String tableName,
            String primaryKeyColumnName) {
        String instanceKey = entityClass.getName() + "|" + tableName + "|"
                + (primaryKeyColumnName == null ? "id" : primaryKeyColumnName);

        if (!singletonInstances.containsKey(instanceKey)) {
            singletonInstances.put(instanceKey, new GenericSQLiteService<>(entityClass, tableName, primaryKeyColumnName));
        }

        return (GenericSQLiteService<T>) singletonInstances.get(instanceKey);
    }

    protected Connection getConnection() throws SQLException {
        return Database.getInstance().getConnection();
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // CREATE (INSERT)
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * INSERT a new record into the database
     * 
     * @param config Configuration map with required keys
     */
    @SuppressWarnings("unchecked")
    public static <T> void create(Map<String, Object> config) {
        validateRequired(config, "class", "table", "entity");

        Class<T> entityClass = (Class<T>) config.get("class");
        String tableName = (String) config.get("table");
        String primaryKey = (String) config.getOrDefault("pk", "id");
        T entity = (T) config.get("entity");

        getInstance(entityClass, tableName, primaryKey).create(entity);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // READ (SELECT)
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * SELECT a single record by primary key
     * 
     * @param config Configuration map
     * @return The entity object, or null if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T read(Map<String, Object> config) {
        validateRequired(config, "class", "table", "id");

        Class<T> entityClass = (Class<T>) config.get("class");
        String tableName = (String) config.get("table");
        String primaryKey = (String) config.getOrDefault("pk", "id");
        String idValue = String.valueOf(config.get("id"));

        return getInstance(entityClass, tableName, primaryKey).read(idValue);
    }

    /**
     * SELECT multiple records with optional filters, ordering, and limit
     * 
     * @param config Configuration map
     * @return List of matching entities
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> readAll(Map<String, Object> config) {
        validateRequired(config, "class", "table");

        Class<T> entityClass = (Class<T>) config.get("class");
        String tableName = (String) config.get("table");
        String primaryKey = (String) config.getOrDefault("pk", "id");

        List<String> columns = (List<String>) config.get("columns");
        Map<String, Object> filters = (Map<String, Object>) config.get("filters");
        String orderBy = (String) config.get("orderBy");
        Integer limit = (Integer) config.get("limit");

        return getInstance(entityClass, tableName, primaryKey).readAll(columns, filters, orderBy, limit);
    }

    /**
     * SELECT single record as Map - perfect for GUI display without needing a class
     * 
     * @param config Configuration map
     * @return Map of column->value pairs, or null if not found
     */
    public static Map<String, Object> readAsMap(Map<String, Object> config) {
        validateRequired(config, "table", "id");

        String tableName = (String) config.get("table");
        String primaryKey = (String) config.getOrDefault("pk", "id");
        Object idValue = config.get("id");

        String selectSQL = "SELECT * FROM " + tableName + " WHERE " + primaryKey + " = ?";

        try (Connection databaseConnection = Database.getInstance().getConnection();
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(selectSQL)) {

            preparedStatement.setObject(1, idValue);

            try (ResultSet queryResults = preparedStatement.executeQuery()) {
                if (queryResults.next()) {
                    return resultSetToMap(queryResults);
                }
            }

        } catch (Exception exception) {
            throw new RuntimeException("Failed to read record from table: " + tableName, exception);
        }

        return null;
    }

    /**
     * SELECT multiple records as List of Maps - perfect for GUI tables without needing a class
     * 
     * @param config Configuration map
     * @return List of Maps, each representing a row
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> readAllAsMap(Map<String, Object> config) {
        validateRequired(config, "table");

        String tableName = (String) config.get("table");
        List<String> columnsToSelect = (List<String>) config.get("columns");
        Map<String, Object> filterConditions = (Map<String, Object>) config.get("filters");
        String orderByClause = (String) config.get("orderBy");
        Integer resultLimit = (Integer) config.get("limit");

        String selectedColumns = (columnsToSelect == null || columnsToSelect.isEmpty()) ? "*"
                : String.join(", ", columnsToSelect);
        StringBuilder selectSQLBuilder = new StringBuilder("SELECT " + selectedColumns + " FROM " + tableName);
        List<Object> queryParameters = new ArrayList<>();

        if (filterConditions != null && !filterConditions.isEmpty()) {
            selectSQLBuilder.append(" WHERE ");
            int filterIndex = 0;

            for (Map.Entry<String, Object> filterEntry : filterConditions.entrySet()) {
                if (filterIndex++ > 0) {
                    selectSQLBuilder.append(" AND ");
                }
                selectSQLBuilder.append(filterEntry.getKey()).append(" = ?");
                queryParameters.add(filterEntry.getValue());
            }
        }

        if (orderByClause != null && !orderByClause.isEmpty()) {
            selectSQLBuilder.append(" ORDER BY ").append(orderByClause);
        }

        if (resultLimit != null && resultLimit > 0) {
            selectSQLBuilder.append(" LIMIT ").append(resultLimit);
        }

        try (Connection databaseConnection = Database.getInstance().getConnection();
                PreparedStatement preparedStatement = databaseConnection
                        .prepareStatement(selectSQLBuilder.toString())) {

            for (int parameterIndex = 0; parameterIndex < queryParameters.size(); parameterIndex++) {
                preparedStatement.setObject(parameterIndex + 1, queryParameters.get(parameterIndex));
            }

            try (ResultSet queryResults = preparedStatement.executeQuery()) {
                List<Map<String, Object>> resultRows = new ArrayList<>();

                while (queryResults.next()) {
                    resultRows.add(resultSetToMap(queryResults));
                }

                return resultRows;
            }

        } catch (Exception exception) {
            throw new RuntimeException("Failed to read all records from table: " + tableName, exception);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // UPDATE
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * UPDATE specific columns by primary key
     * 
     * @param config Configuration map
     */
    @SuppressWarnings("unchecked")
    public static <T> void update(Map<String, Object> config) {
        validateRequired(config, "class", "table");

        Class<T> entityClass = (Class<T>) config.get("class");
        String tableName = (String) config.get("table");
        String primaryKey = (String) config.getOrDefault("pk", "id");

        // Two update modes: by entity or by id+updates
        if (config.containsKey("entity")) {
            // Update mode 1: Using entity object
            T entity = (T) config.get("entity");
            getInstance(entityClass, tableName, primaryKey).update(entity);
        } else if (config.containsKey("id") && config.containsKey("updates")) {
            // Update mode 2: Using id + updates map
            Object idValue = config.get("id");
            Map<String, Object> updates = (Map<String, Object>) config.get("updates");
            getInstance(entityClass, tableName, primaryKey).update(idValue, updates);
        } else {
            throw new IllegalArgumentException("Update requires either 'entity' OR ('id' + 'updates')");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // DELETE
    // ═════════════════════════════════════════════════════════════════════════════
    /**
     * DELETE a record by primary key
     * 
     * @param config Configuration map
     */
    @SuppressWarnings("unchecked")
    public static <T> void delete(Map<String, Object> config) {
        validateRequired(config, "class", "table", "id");

        Class<T> entityClass = (Class<T>) config.get("class");
        String tableName = (String) config.get("table");
        String primaryKey = (String) config.getOrDefault("pk", "id");
        String idValue = String.valueOf(config.get("id"));

        getInstance(entityClass, tableName, primaryKey).delete(idValue);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═════════════════════════════════════════════════════════════════════════════
    /**
     * Validate that required keys exist in the config map
     */
    private static void validateRequired(Map<String, Object> config, String... requiredKeys) {
        if (config == null) {
            throw new IllegalArgumentException("Config map cannot be null");
        }

        for (String key : requiredKeys) {
            if (!config.containsKey(key)) {
                throw new IllegalArgumentException("Missing required key: '" + key + "'");
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // INSTANCE METHODS (used internally by static methods)
    // ═════════════════════════════════════════════════════════════════════════════

    public void create(T entity) {
        Field[] entityFields = entityClass.getDeclaredFields();
        StringBuilder columnNamesBuilder = new StringBuilder();
        StringBuilder valuePlaceholdersBuilder = new StringBuilder();
        List<Field> fieldsToInsert = new ArrayList<>();

        for (Field currentField : entityFields) {
            if (Modifier.isStatic(currentField.getModifiers())) continue;
            currentField.setAccessible(true);
            try {
                Object fieldValue = currentField.get(entity);

                if (fieldsToInsert.size() > 0) {
                    columnNamesBuilder.append(", ");
                    valuePlaceholdersBuilder.append(", ");
                }

                columnNamesBuilder.append(currentField.getName());
                valuePlaceholdersBuilder.append("?");
                fieldsToInsert.add(currentField);

            } catch (IllegalAccessException ignoredException) {
            }
        }

        String insertSQL = "INSERT INTO " + tableName + " (" + columnNamesBuilder + ") VALUES ("
                + valuePlaceholdersBuilder + ")";

        try (Connection databaseConnection = getConnection();
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(insertSQL)) {

            for (int parameterIndex = 0; parameterIndex < fieldsToInsert.size(); parameterIndex++) {
                Object fieldValue = fieldsToInsert.get(parameterIndex).get(entity);
                preparedStatement.setObject(parameterIndex + 1, fieldValue);
            }

            preparedStatement.executeUpdate();

        } catch (Exception exception) {
            throw new RuntimeException("Failed to create entity in table: " + tableName, exception);
        }
    }

    public T read(String primaryKeyValue) {
        String selectSQL = "SELECT * FROM " + tableName + " WHERE " + primaryKeyColumnName + " = ?";

        try (Connection databaseConnection = getConnection();
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(selectSQL)) {

            preparedStatement.setObject(1, primaryKeyValue);

            try (ResultSet queryResults = preparedStatement.executeQuery()) {
                if (queryResults.next()) {
                    return mapResultSetRowToEntity(queryResults);
                }
            }

        } catch (Exception exception) {
            throw new RuntimeException("Failed to read entity with " + primaryKeyColumnName + " = " + primaryKeyValue,
                    exception);
        }

        return null;
    }

    public List<T> readAll(List<String> columnsToSelect, Map<String, Object> filterConditions, String orderByClause,
            Integer resultLimit) {
        String selectedColumns = (columnsToSelect == null || columnsToSelect.isEmpty()) ? "*"
                : String.join(", ", columnsToSelect);
        StringBuilder selectSQLBuilder = new StringBuilder("SELECT " + selectedColumns + " FROM " + tableName);
        List<Object> queryParameters = new ArrayList<>();

        if (filterConditions != null && !filterConditions.isEmpty()) {
            selectSQLBuilder.append(" WHERE ");
            int filterIndex = 0;

            for (Map.Entry<String, Object> filterEntry : filterConditions.entrySet()) {
                if (filterIndex++ > 0) {
                    selectSQLBuilder.append(" AND ");
                }
                selectSQLBuilder.append(filterEntry.getKey()).append(" = ?");
                queryParameters.add(filterEntry.getValue());
            }
        }

        if (orderByClause != null && !orderByClause.isEmpty()) {
            selectSQLBuilder.append(" ORDER BY ").append(orderByClause);
        }

        if (resultLimit != null && resultLimit > 0) {
            selectSQLBuilder.append(" LIMIT ").append(resultLimit);
        }

        try (Connection databaseConnection = getConnection();
                PreparedStatement preparedStatement = databaseConnection
                        .prepareStatement(selectSQLBuilder.toString())) {

            for (int parameterIndex = 0; parameterIndex < queryParameters.size(); parameterIndex++) {
                preparedStatement.setObject(parameterIndex + 1, queryParameters.get(parameterIndex));
            }

            try (ResultSet queryResults = preparedStatement.executeQuery()) {
                List<T> resultEntities = new ArrayList<>();

                while (queryResults.next()) {
                    resultEntities.add(mapResultSetRowToEntity(queryResults));
                }

                return resultEntities;
            }

        } catch (Exception exception) {
            throw new RuntimeException("Failed to read all entities from table: " + tableName, exception);
        }
    }

    public void update(T entity) {
        Field[] entityFields = entityClass.getDeclaredFields();
        List<Field> fieldsToUpdate = new ArrayList<>();
        Object primaryKeyValue = null;

        for (Field currentField : entityFields) {
            if (Modifier.isStatic(currentField.getModifiers())) continue;
            currentField.setAccessible(true);

            try {
                if (currentField.getName().equals(primaryKeyColumnName)) {
                    primaryKeyValue = currentField.get(entity);
                    continue;
                }

                Object fieldValue = currentField.get(entity);
                if (fieldValue != null) {
                    fieldsToUpdate.add(currentField);
                }

            } catch (IllegalAccessException ignoredException) {
            }
        }

        if (fieldsToUpdate.isEmpty()) {
            return;
        }

        StringBuilder updateSQLBuilder = new StringBuilder("UPDATE ").append(tableName).append(" SET ");

        for (int fieldIndex = 0; fieldIndex < fieldsToUpdate.size(); fieldIndex++) {
            if (fieldIndex > 0) {
                updateSQLBuilder.append(", ");
            }
            updateSQLBuilder.append(fieldsToUpdate.get(fieldIndex).getName()).append(" = ?");
        }

        updateSQLBuilder.append(" WHERE ").append(primaryKeyColumnName).append(" = ?");

        try (Connection databaseConnection = getConnection();
                PreparedStatement preparedStatement = databaseConnection
                        .prepareStatement(updateSQLBuilder.toString())) {

            int parameterIndex = 1;

            for (Field fieldToUpdate : fieldsToUpdate) {
                Object fieldValue = fieldToUpdate.get(entity);
                preparedStatement.setObject(parameterIndex++, fieldValue);
            }

            preparedStatement.setObject(parameterIndex, primaryKeyValue);

            preparedStatement.executeUpdate();

        } catch (Exception exception) {
            throw new RuntimeException("Failed to update entity in table: " + tableName, exception);
        }
    }

    public void update(Object primaryKeyValue, Map<String, Object> columnUpdates) {
        if (columnUpdates == null || columnUpdates.isEmpty()) {
            return;
        }

        StringBuilder updateSQLBuilder = new StringBuilder("UPDATE ").append(tableName).append(" SET ");

        int columnIndex = 0;
        for (String columnName : columnUpdates.keySet()) {
            if (columnIndex++ > 0) {
                updateSQLBuilder.append(", ");
            }
            updateSQLBuilder.append(columnName).append(" = ?");
        }

        updateSQLBuilder.append(" WHERE ").append(primaryKeyColumnName).append(" = ?");

        try (Connection databaseConnection = getConnection();
                PreparedStatement preparedStatement = databaseConnection
                        .prepareStatement(updateSQLBuilder.toString())) {

            int parameterIndex = 1;

            for (Object newValue : columnUpdates.values()) {
                preparedStatement.setObject(parameterIndex++, newValue);
            }

            preparedStatement.setObject(parameterIndex, primaryKeyValue);

            preparedStatement.executeUpdate();

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Failed to update " + primaryKeyColumnName + " = " + primaryKeyValue + " in table: " + tableName,
                    exception);
        }
    }

    public void delete(String primaryKeyValue) {
        String deleteSQL = "DELETE FROM " + tableName + " WHERE " + primaryKeyColumnName + " = ?";

        try (Connection databaseConnection = getConnection();
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(deleteSQL)) {

            preparedStatement.setObject(1, primaryKeyValue);
            preparedStatement.executeUpdate();

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to delete entity with " + primaryKeyColumnName + " = " + primaryKeyValue,
                    exception);
        }
    }

    private T mapResultSetRowToEntity(ResultSet queryResults) throws Exception {
        T entityInstance = entityClass.getDeclaredConstructor().newInstance();
        ResultSetMetaData resultMetadata = queryResults.getMetaData();
        int columnCount = resultMetadata.getColumnCount();

        Map<String, Object> rowData = new HashMap<>();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            String columnName = resultMetadata.getColumnLabel(columnIndex);
            Object columnValue = queryResults.getObject(columnIndex);
            rowData.put(columnName, columnValue);
        }

        for (Field entityField : entityClass.getDeclaredFields()) {
            entityField.setAccessible(true);

            if (rowData.containsKey(entityField.getName())) {
                Object columnValue = rowData.get(entityField.getName());

                if (columnValue != null) {
                    entityField.set(entityInstance, columnValue);
                }
            }
        }

        return entityInstance;
    }

    /**
     * Convert a ResultSet row to a Map (helper for GUI display)
     */
    private static Map<String, Object> resultSetToMap(ResultSet queryResults) throws SQLException {
        ResultSetMetaData resultMetadata = queryResults.getMetaData();
        int columnCount = resultMetadata.getColumnCount();
        Map<String, Object> rowData = new HashMap<>();

        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            String columnName = resultMetadata.getColumnLabel(columnIndex);
            Object columnValue = queryResults.getObject(columnIndex);
            rowData.put(columnName, columnValue);
        }

        return rowData;
    }
}