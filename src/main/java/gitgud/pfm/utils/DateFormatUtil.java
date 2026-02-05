package gitgud.pfm.utils;

import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for standardizing date formats across the application.
 * 
 * Display Format (UK):
 * - Date only: dd/MM/yyyy (e.g., "05/02/2026")
 * - Date with time: dd/MM/yyyy HH:mm:ss (e.g., "05/02/2026 14:30:45")
 * 
 * Storage Format (ISO - for database and internal use):
 * - Date only: yyyy-MM-dd (e.g., "2026-02-05")
 * - Date with time: yyyy-MM-dd HH:mm:ss (e.g., "2026-02-05 14:30:45")
 */
public class DateFormatUtil {
    
    // UK display format constants
    public static final DateTimeFormatter UK_DATE_FORMAT = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter UK_DATETIME_FORMAT = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    // ISO storage format constants
    public static final DateTimeFormatter ISO_DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter ISO_DATETIME_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // UK date prompt text for DatePicker controls
    public static final String UK_DATE_PROMPT = "dd/MM/yyyy";
    
    /**
     * Convert ISO date string (yyyy-MM-dd) to UK format (dd/MM/yyyy).
     * Returns the original string if parsing fails or input is null/empty.
     * 
     * @param isoDate Date string in ISO format (yyyy-MM-dd)
     * @return Date string in UK format (dd/MM/yyyy)
     */
    public static String isoToUkDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return isoDate;
        }
        try {
            LocalDate date = LocalDate.parse(isoDate, ISO_DATE_FORMAT);
            return date.format(UK_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            // Return original if parsing fails
            return isoDate;
        }
    }
    
    /**
     * Convert UK date string (dd/MM/yyyy) to ISO format (yyyy-MM-dd).
     * Returns the original string if parsing fails or input is null/empty.
     * 
     * @param ukDate Date string in UK format (dd/MM/yyyy)
     * @return Date string in ISO format (yyyy-MM-dd)
     */
    public static String ukToIsoDate(String ukDate) {
        if (ukDate == null || ukDate.isEmpty()) {
            return ukDate;
        }
        try {
            LocalDate date = LocalDate.parse(ukDate, UK_DATE_FORMAT);
            return date.format(ISO_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            // Return original if parsing fails
            return ukDate;
        }
    }
    
    /**
     * Convert ISO datetime string (yyyy-MM-dd HH:mm:ss) to UK format (dd/MM/yyyy HH:mm:ss).
     * Returns the original string if parsing fails or input is null/empty.
     * 
     * @param isoDateTime DateTime string in ISO format (yyyy-MM-dd HH:mm:ss)
     * @return DateTime string in UK format (dd/MM/yyyy HH:mm:ss)
     */
    public static String isoToUkDateTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) {
            return isoDateTime;
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(isoDateTime, ISO_DATETIME_FORMAT);
            return dateTime.format(UK_DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            // Try parsing as date only and return as UK date
            try {
                LocalDate date = LocalDate.parse(isoDateTime, ISO_DATE_FORMAT);
                return date.format(UK_DATE_FORMAT);
            } catch (DateTimeParseException e2) {
                // Return original if all parsing fails
                return isoDateTime;
            }
        }
    }
    
    /**
     * Convert UK datetime string (dd/MM/yyyy HH:mm:ss) to ISO format (yyyy-MM-dd HH:mm:ss).
     * Returns the original string if parsing fails or input is null/empty.
     * 
     * @param ukDateTime DateTime string in UK format (dd/MM/yyyy HH:mm:ss)
     * @return DateTime string in ISO format (yyyy-MM-dd HH:mm:ss)
     */
    public static String ukToIsoDateTime(String ukDateTime) {
        if (ukDateTime == null || ukDateTime.isEmpty()) {
            return ukDateTime;
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(ukDateTime, UK_DATETIME_FORMAT);
            return dateTime.format(ISO_DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            // Try parsing as date only
            try {
                LocalDate date = LocalDate.parse(ukDateTime, UK_DATE_FORMAT);
                return date.format(ISO_DATE_FORMAT);
            } catch (DateTimeParseException e2) {
                // Return original if all parsing fails
                return ukDateTime;
            }
        }
    }
    
    /**
     * Convert ISO datetime/date string to UK date only format (dd/MM/yyyy).
     * Useful when you only want to display the date portion.
     * 
     * @param isoString Date or DateTime string in ISO format
     * @return Date string in UK format (dd/MM/yyyy)
     */
    public static String isoToUkDateOnly(String isoString) {
        if (isoString == null || isoString.isEmpty()) {
            return isoString;
        }
        try {
            // Try parsing as datetime first
            LocalDateTime dateTime = LocalDateTime.parse(isoString, ISO_DATETIME_FORMAT);
            return dateTime.format(UK_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            // Try parsing as date only
            try {
                LocalDate date = LocalDate.parse(isoString, ISO_DATE_FORMAT);
                return date.format(UK_DATE_FORMAT);
            } catch (DateTimeParseException e2) {
                return isoString;
            }
        }
    }
    
    /**
     * Get a StringConverter for DatePicker controls that displays dates in UK format.
     * The converter handles both display (LocalDate -> String) and parsing (String -> LocalDate).
     * 
     * @return StringConverter configured for UK date format (dd/MM/yyyy)
     */
    public static StringConverter<LocalDate> getUkDatePickerConverter() {
        return new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date == null) {
                    return "";
                }
                return date.format(UK_DATE_FORMAT);
            }
            
            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                try {
                    return LocalDate.parse(string.trim(), UK_DATE_FORMAT);
                } catch (DateTimeParseException e) {
                    // Try ISO format as fallback (for backwards compatibility)
                    try {
                        return LocalDate.parse(string.trim(), ISO_DATE_FORMAT);
                    } catch (DateTimeParseException e2) {
                        return null;
                    }
                }
            }
        };
    }
    
    /**
     * Configure a DatePicker to use UK date format.
     * Sets the converter and prompt text.
     * 
     * @param datePicker The DatePicker to configure
     */
    public static void configureDatePickerUkFormat(javafx.scene.control.DatePicker datePicker) {
        if (datePicker != null) {
            datePicker.setConverter(getUkDatePickerConverter());
            datePicker.setPromptText(UK_DATE_PROMPT);
        }
    }
    
    /**
     * Format a LocalDate to ISO format string for storage.
     * 
     * @param date LocalDate to format
     * @return Date string in ISO format (yyyy-MM-dd)
     */
    public static String formatToIso(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(ISO_DATE_FORMAT);
    }
    
    /**
     * Format a LocalDateTime to ISO format string for storage.
     * 
     * @param dateTime LocalDateTime to format
     * @return DateTime string in ISO format (yyyy-MM-dd HH:mm:ss)
     */
    public static String formatToIso(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(ISO_DATETIME_FORMAT);
    }
    
    /**
     * Format a LocalDate to UK format string for display.
     * 
     * @param date LocalDate to format
     * @return Date string in UK format (dd/MM/yyyy)
     */
    public static String formatToUk(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(UK_DATE_FORMAT);
    }
    
    /**
     * Format a LocalDateTime to UK format string for display.
     * 
     * @param dateTime LocalDateTime to format
     * @return DateTime string in UK format (dd/MM/yyyy HH:mm:ss)
     */
    public static String formatToUk(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(UK_DATETIME_FORMAT);
    }
    
    /**
     * Parse an ISO date string to LocalDate.
     * 
     * @param isoDate Date string in ISO format (yyyy-MM-dd)
     * @return LocalDate or null if parsing fails
     */
    public static LocalDate parseIsoDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(isoDate, ISO_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Parse an ISO datetime string to LocalDateTime.
     * Also handles date-only strings by returning start of day.
     * 
     * @param isoDateTime DateTime string in ISO format (yyyy-MM-dd HH:mm:ss)
     * @return LocalDateTime or null if parsing fails
     */
    public static LocalDateTime parseIsoDateTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(isoDateTime, ISO_DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            // Try parsing as date only
            try {
                LocalDate date = LocalDate.parse(isoDateTime, ISO_DATE_FORMAT);
                return date.atStartOfDay();
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }
}
