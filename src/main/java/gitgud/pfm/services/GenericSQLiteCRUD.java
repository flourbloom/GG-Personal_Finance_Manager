package gitgud.pfm.services;

import gitgud.pfm.interfaces.CRUD;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/**
 * Generic CRUD implementation using JDBC and reflection.
 * - Works with any table name (including schema-qualified names)
 * - Supports create, read (by pk), readAll with optional columns/filters,
 * update by object or by Map, and delete by pk.
 * - Assumes a no-arg constructor for entity type and fields matching column
 * names.
 */
public class GenericSQLiteCRUD<T> implements CRUD<T> {

    private final Class<T> entityClass;
    private final String tableName;
    private final String primaryKeyColumnName;

    // Singleton instances keyed by entityClassName|tableName|primaryKeyColumnName
    private static final Map<String, GenericSQLiteCRUD<?>> singletonInstances = new HashMap<>();

    private GenericSQLiteCRUD(Class<T> entityClass, String tableName, String primaryKeyColumnName) {
        this.entityClass = entityClass;
        this.tableName = tableName;
        this.primaryKeyColumnName = primaryKeyColumnName == null || primaryKeyColumnName.isEmpty() ? "id"
                : primaryKeyColumnName;
    }

    /**
     * Get or create a singleton instance with default "id" as primary key
     */
    @SuppressWarnings("unchecked")
    public static synchronized <T> GenericSQLiteCRUD<T> getInstance(Class<T> entityClass, String tableName) {
        return getInstance(entityClass, tableName, "id");
    }

    /**
     * Get or create a singleton instance with custom primary key column
     */
    @SuppressWarnings("unchecked")
    public static synchronized <T> GenericSQLiteCRUD<T> getInstance(Class<T> entityClass, String tableName,
            String primaryKeyColumnName) {
        String instanceKey = entityClass.getName() + "|" + tableName + "|"
                + (primaryKeyColumnName == null ? "id" : primaryKeyColumnName);

        if (!singletonInstances.containsKey(instanceKey)) {
            singletonInstances.put(instanceKey, new GenericSQLiteCRUD<>(entityClass, tableName, primaryKeyColumnName));
        }

        return (GenericSQLiteCRUD<T>) singletonInstances.get(instanceKey);
    }

    protected Connection getConnection() throws SQLException {
        return Database.getInstance().getConnection();
    }

    /**
     * Create (INSERT) a new entity record
     * 
     * @param entity The entity object to insert
     */
    @Override
    public void create(T entity) {
        Field[] entityFields = entityClass.getDeclaredFields();
        StringBuilder columnNamesBuilder = new StringBuilder();
        StringBuilder valuePlaceholdersBuilder = new StringBuilder();
        List<Field> fieldsToInsert = new ArrayList<>();

        for (Field currentField : entityFields) {
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
                // Skip inaccessible fields
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

    /**
     * Read (SELECT) a single entity by primary key
     * 
     * @param primaryKeyValue The primary key value to search for
     * @return The entity object, or null if not found
     */
    @Override
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

    /**
     * Read all entities with optional filters, column selection, ordering, and
     * limit
     * 
     * @param columnsToSelect  List of column names to retrieve (null or empty for
     *                         all columns "*")
     * @param filterConditions Map of column->value pairs for WHERE clause (AND
     *                         logic)
     * @param orderByClause    ORDER BY clause (e.g., "name ASC, date DESC")
     * @param resultLimit      Maximum number of rows to return (null for no limit)
     * @return List of matching entities
     */
    public List<T> readAll(List<String> columnsToSelect, Map<String, Object> filterConditions, String orderByClause,
            Integer resultLimit) {
        String selectedColumns = (columnsToSelect == null || columnsToSelect.isEmpty()) ? "*"
                : String.join(", ", columnsToSelect);
        StringBuilder selectSQLBuilder = new StringBuilder("SELECT " + selectedColumns + " FROM " + tableName);
        List<Object> queryParameters = new ArrayList<>();

        // Build WHERE clause with filters
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

        // Add ORDER BY clause
        if (orderByClause != null && !orderByClause.isEmpty()) {
            selectSQLBuilder.append(" ORDER BY ").append(orderByClause);
        }

        // Add LIMIT clause
        if (resultLimit != null && resultLimit > 0) {
            selectSQLBuilder.append(" LIMIT ").append(resultLimit);
        }

        try (Connection databaseConnection = getConnection();
                PreparedStatement preparedStatement = databaseConnection
                        .prepareStatement(selectSQLBuilder.toString())) {

            // Set WHERE clause parameters
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

    /**
     * Update entity using the entity object
     * Only non-null fields (excluding primary key) are updated
     * 
     * @param entity The entity with updated values (must have primary key set)
     */
    @Override
    public void update(T entity) {
        Field[] entityFields = entityClass.getDeclaredFields();
        List<Field> fieldsToUpdate = new ArrayList<>();
        Object primaryKeyValue = null;

        for (Field currentField : entityFields) {
            currentField.setAccessible(true);

            try {
                // Extract primary key value but don't update it
                if (currentField.getName().equals(primaryKeyColumnName)) {
                    primaryKeyValue = currentField.get(entity);
                    continue;
                }

                Object fieldValue = currentField.get(entity);
                if (fieldValue != null) {
                    fieldsToUpdate.add(currentField);
                }

            } catch (IllegalAccessException ignoredException) {
                // Skip inaccessible fields
            }
        }

        if (fieldsToUpdate.isEmpty()) {
            return; // Nothing to update
        }

        // Build UPDATE SQL
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

            // Set field values to update
            for (Field fieldToUpdate : fieldsToUpdate) {
                Object fieldValue = fieldToUpdate.get(entity);
                preparedStatement.setObject(parameterIndex++, fieldValue);
            }

            // Set primary key value for WHERE clause
            preparedStatement.setObject(parameterIndex, primaryKeyValue);

            preparedStatement.executeUpdate();

        } catch (Exception exception) {
            throw new RuntimeException("Failed to update entity in table: " + tableName, exception);
        }
    }

    /**
     * Update specific columns by primary key using a Map
     * 
     * @param primaryKeyValue The primary key value of the record to update
     * @param columnUpdates   Map of column names to their new values
     * 
     *                        Example usage:
     *                        Map<String, Object> updates = new HashMap<>();
     *                        updates.put("name", "John Doe");
     *                        updates.put("age", 30);
     *                        updates.put("email", "john@example.com");
     *                        crud.update("user_123", updates);
     */
    public void update(Object primaryKeyValue, Map<String, Object> columnUpdates) {
        if (columnUpdates == null || columnUpdates.isEmpty()) {
            return; // Nothing to update
        }

        // Build UPDATE SQL
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

            // Set column values to update
            for (Object newValue : columnUpdates.values()) {
                preparedStatement.setObject(parameterIndex++, newValue);
            }

            // Set primary key value for WHERE clause
            preparedStatement.setObject(parameterIndex, primaryKeyValue);

            preparedStatement.executeUpdate();

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Failed to update " + primaryKeyColumnName + " = " + primaryKeyValue + " in table: " + tableName,
                    exception);
        }
    }

    /**
     * Delete (DELETE) an entity by primary key
     * 
     * @param primaryKeyValue The primary key value of the record to delete
     */
    @Override
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

    /**
     * Map a ResultSet row to an entity object using reflection
     * 
     * @param queryResults The ResultSet positioned at a specific row
     * @return The mapped entity object
     */
    private T mapResultSetRowToEntity(ResultSet queryResults) throws Exception {
        T entityInstance = entityClass.getDeclaredConstructor().newInstance();
        ResultSetMetaData resultMetadata = queryResults.getMetaData();
        int columnCount = resultMetadata.getColumnCount();

        // Extract all column values from ResultSet
        Map<String, Object> rowData = new HashMap<>();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            String columnName = resultMetadata.getColumnLabel(columnIndex);
            Object columnValue = queryResults.getObject(columnIndex);
            rowData.put(columnName, columnValue);
        }

        // Map column values to entity fields
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
}