import java.util.regex.Pattern;

// Business rules for employee data. Each check returns null when the value
// is valid, otherwise a friendly message explaining what is wrong.
public final class Validator {

    public static final double MAX_SALARY = 100_000_000;

    public static final int NAME_MIN_LENGTH = 2;
    public static final int NAME_MAX_LENGTH = 50;

    public static final String[] DEPARTMENTS = {
            "IT", "Finance", "HR", "Marketing", "Sales", "Operations"
    };

    private static final Pattern ID_PATTERN =
            Pattern.compile("E\\d{3}");

    // Words of letters joined by single spaces, hyphens or apostrophes
    private static final Pattern NAME_PATTERN =
            Pattern.compile("\\p{L}+([ '-]\\p{L}+)*");

    // 500000, 500000.5, 500,000.50
    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("(\\d+|\\d{1,3}(,\\d{3})+)(\\.\\d+)?");

    private Validator() {
    }

    // --------------------------------------------------
    // Employee ID
    // --------------------------------------------------

    public static String normalizeId(String id) {
        return id == null ? "" : id.trim().toUpperCase();
    }

    public static String checkId(String id) {

        id = normalizeId(id);

        if (id.isEmpty()) {
            return "Employee ID is required.";
        }

        if (!ID_PATTERN.matcher(id).matches()) {
            return "Employee ID must be the letter E followed by 3 digits, e.g. E001.";
        }

        if (id.equals("E000")) {
            return "E000 is not a valid ID. Employee IDs start from E001.";
        }

        return null;
    }

    // --------------------------------------------------
    // Name
    // --------------------------------------------------

    // Trims the name, collapses repeated spaces and capitalises each word
    // ("  john   o'neil-smith " becomes "John O'Neil-Smith")
    public static String normalizeName(String name) {

        if (name == null) {
            return "";
        }

        char[] chars = name.trim().replaceAll("\\s+", " ").toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (i == 0 || chars[i - 1] == ' ' || chars[i - 1] == '-' || chars[i - 1] == '\'') {
                chars[i] = Character.toUpperCase(chars[i]);
            }
        }

        return new String(chars);
    }

    public static String checkName(String name) {

        name = normalizeName(name);

        if (name.isEmpty()) {
            return "Name is required.";
        }

        if (name.length() < NAME_MIN_LENGTH) {
            return "Name is too short. It must have at least "
                    + NAME_MIN_LENGTH + " characters.";
        }

        if (name.length() > NAME_MAX_LENGTH) {
            return "Name is too long. It can have at most "
                    + NAME_MAX_LENGTH + " characters.";
        }

        if (name.matches(".*\\d.*")) {
            return "Name cannot contain numbers.";
        }

        if (!NAME_PATTERN.matcher(name).matches()) {
            return "Name can only contain letters, spaces, hyphens (-) and apostrophes ('),"
                    + " and must start and end with a letter.";
        }

        return null;
    }

    // --------------------------------------------------
    // Department
    // --------------------------------------------------

    // Returns the official department name, or null if it is not recognised
    public static String toDepartment(String department) {

        if (department == null) {
            return null;
        }

        for (String known : DEPARTMENTS) {
            if (known.equalsIgnoreCase(department.trim())) {
                return known;
            }
        }

        return null;
    }

    public static String checkDepartment(String department) {

        if (department == null || department.trim().isEmpty()) {
            return "Department is required.";
        }

        if (toDepartment(department) == null) {
            return "'" + department.trim() + "' is not a valid department. Valid departments are: "
                    + String.join(", ", DEPARTMENTS) + ".";
        }

        return null;
    }

    // --------------------------------------------------
    // Amounts typed by the user
    // --------------------------------------------------

    public static String checkAmountFormat(String input, String label) {

        input = input == null ? "" : input.trim();

        if (input.isEmpty()) {
            return label + " is required. Please enter an amount.";
        }

        if (input.startsWith("-")) {
            return label + " cannot be negative.";
        }

        if (!AMOUNT_PATTERN.matcher(input).matches()) {
            return "'" + input + "' is not a valid amount. Use digits only, e.g. 500000 or 500000.50.";
        }

        int dot = input.indexOf('.');

        if (dot >= 0 && input.length() - dot - 1 > 2) {
            return label + " can have at most 2 decimal places.";
        }

        return null;
    }

    // Only call after checkAmountFormat returned null
    public static double parseAmount(String input) {
        return Double.parseDouble(input.trim().replace(",", ""));
    }

    // --------------------------------------------------
    // Salary and tax
    // --------------------------------------------------

    public static String checkSalary(double salary) {

        if (Double.isNaN(salary) || Double.isInfinite(salary)) {
            return "Salary must be a valid number.";
        }

        if (salary <= 0) {
            return "Salary must be greater than zero.";
        }

        if (salary > MAX_SALARY) {
            return "Salary cannot be more than " + formatMoney(MAX_SALARY)
                    + ". Please check the amount.";
        }

        return null;
    }

    public static String checkTax(double tax, double grossSalary) {

        if (Double.isNaN(tax) || Double.isInfinite(tax)) {
            return "Tax must be a valid number.";
        }

        if (tax < 0) {
            return "Tax cannot be negative.";
        }

        if (tax > grossSalary) {
            return "Tax cannot be more than the gross salary ("
                    + formatMoney(grossSalary) + ").";
        }

        return null;
    }

    public static String checkTaxPercentage(double percentage) {

        if (Double.isNaN(percentage) || percentage < 0 || percentage > 100) {
            return "Tax percentage must be between 0 and 100.";
        }

        return null;
    }

    public static String formatMoney(double amount) {
        return String.format("%,.2f", amount);
    }
}
