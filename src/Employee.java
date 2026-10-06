public class Employee {

    private String id;
    private String name;
    private String department;
    private double salary;

    public Employee(String id, String name, String department, double salary) {

        requireValid(Validator.checkId(id));

        this.id = Validator.normalizeId(id);

        updateDetails(name, department, salary);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public double getSalary() {
        return salary;
    }

    public void updateDetails(
            String name,
            String department,
            double salary) {

        requireValid(Validator.checkName(name));
        requireValid(Validator.checkDepartment(department));
        requireValid(Validator.checkSalary(salary));

        this.name = Validator.normalizeName(name);
        this.department = Validator.toDepartment(department);
        this.salary = salary;
    }

    private static void requireValid(String error) {
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
    }

    @Override
    public String toString() {
        return "ID: " + id
                + ", Name: " + name
                + ", Department: " + department
                + ", Salary: " + Validator.formatMoney(salary);
    }
}
