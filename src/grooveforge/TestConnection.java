package grooveforge;

import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Console JDBC checkpoint for the required 14 project tables. */
public final class TestConnection {
    private TestConnection() { }

    public static List<String> missingTables(Connection connection) throws SQLException {
        List<String> missing = new ArrayList<String>();
        for (String table : Model.tables().keySet()) {
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
                result.next();
            } catch (SQLException exception) {
                missing.add(table);
            }
        }
        return missing;
    }

    public static void main(String[] args) {
        Console console = System.console();
        Scanner scanner = console == null ? new Scanner(System.in) : null;
        String url = args.length > 0 ? args[0] : read(console, scanner, "JDBC URL: ");
        String username = args.length > 1 ? args[1] : read(console, scanner, "Username: ");
        String password = args.length > 2 ? args[2] : readPassword(console, scanner);

        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException exception) {
            System.err.println("Driver missing: add ojdbc11.jar to lib/.");
            System.exit(2);
            return;
        }

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            List<String> missing = missingTables(connection);
            if (!missing.isEmpty()) {
                System.err.println("Project tables missing or inaccessible: " + missing);
                System.exit(4);
                return;
            }
            System.out.println("Connection successful. Accessible project tables: " + Model.tables().size());
        } catch (SQLException exception) {
            if (exception.getErrorCode() == 1017) {
                System.err.println("Bad credentials: Oracle rejected the username or password.");
            } else {
                System.err.println("Connection failed: " + exception.getMessage());
            }
            System.exit(3);
        }
    }

    private static String read(Console console, Scanner scanner, String prompt) {
        return console != null ? console.readLine(prompt) : promptAndRead(scanner, prompt);
    }

    private static String readPassword(Console console, Scanner scanner) {
        if (console != null) {
            char[] password = console.readPassword("Password: ");
            return password == null ? "" : new String(password);
        }
        return promptAndRead(scanner, "Password: ");
    }

    private static String promptAndRead(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}
