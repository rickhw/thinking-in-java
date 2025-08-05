
import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Scanner;

public class MessageIdMigrator {

    private static final String DB_HOST = System.getenv().getOrDefault("DB_HOST", "localhost");
    private static final String DB_PORT = System.getenv().getOrDefault("DB_PORT", "3306");
    private static final String DB_NAME = System.getenv().getOrDefault("DB_NAME", "pgb");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "medusa");

    private static final String JDBC_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?useSSL=false&serverTimezone=UTC";

    private static void log(String message) {
        System.out.printf("[INFO ] %s %s%n", now(), message);
    }

    private static void warn(String message) {
        System.out.printf("[WARN ] %s %s%n", now(), message);
    }

    private static void error(String message) {
        System.err.printf("[ERROR] %s %s%n", now(), message);
        System.exit(1);
    }

    private static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static Connection connect() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", DB_USER);
        props.setProperty("password", DB_PASSWORD);
        return DriverManager.getConnection(JDBC_URL, props);
    }

    private static boolean isMigrationNeeded(Connection conn) throws SQLException {
        String query = """
                SELECT DATA_TYPE 
                FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'messages' AND COLUMN_NAME = 'id'
                """;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, DB_NAME);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String dataType = rs.getString("DATA_TYPE");
                log("Current ID column type: " + dataType);
                return "bigint".equalsIgnoreCase(dataType);
            }
        }
        error("messages.id column not found.");
        return false;
    }

    private static void backupMessagesTable() throws IOException, InterruptedException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "backup_messages_" + timestamp + ".sql";
        log("Creating backup file: " + filename);

        ProcessBuilder pb = new ProcessBuilder(
                "mysqldump",
                "-h" + DB_HOST,
                "-P" + DB_PORT,
                "-u" + DB_USER,
                "-p" + DB_PASSWORD,
                DB_NAME,
                "messages"
        );

        pb.redirectOutput(new File(filename));
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            log("Backup created: " + filename);
        } else {
            error("Failed to create backup.");
        }
    }

    private static void runMigrationScript(Connection conn) throws Exception {
        String scriptPath = "src/main/resources/db/migration/V2__migrate_message_id_to_varchar.sql";
        log("Running migration script: " + scriptPath);

        StringBuilder sql = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(scriptPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql.toString());
            log("Migration script executed successfully.");
        }
    }

    private static void verifyMigration(Connection conn) throws SQLException {
        log("Verifying migration...");

        String typeQuery = """
            SELECT DATA_TYPE, CHARACTER_MAXIMUM_LENGTH 
            FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'messages' AND COLUMN_NAME = 'id'
        """;

        try (PreparedStatement stmt = conn.prepareStatement(typeQuery)) {
            stmt.setString(1, DB_NAME);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if (!"varchar".equalsIgnoreCase(rs.getString("DATA_TYPE")) || rs.getInt("CHARACTER_MAXIMUM_LENGTH") != 36) {
                    error("Column 'id' is not VARCHAR(36).");
                }
            }
        }

        long total = getLong(conn, "SELECT COUNT(*) FROM messages");
        long valid = getLong(conn, "SELECT COUNT(*) FROM messages WHERE id REGEXP '^[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}$'");
        long unique = getLong(conn, "SELECT COUNT(DISTINCT id) FROM messages");

        if (total != valid || total != unique) {
            error("Validation failed: total=" + total + ", valid=" + valid + ", unique=" + unique);
        }

        log("✓ Verification passed: total=" + total + ", valid=" + valid + ", unique=" + unique);
    }

    private static long getLong(Connection conn, String query) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    private static boolean isConfirmed(String[] args) {
        for (String arg : args) {
            if ("--confirm".equalsIgnoreCase(arg) || "--force".equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }
    

    public static void main(String[] args) {
        log("=== Message ID Migration Tool ===");
        log("Database: " + DB_HOST + ":" + DB_PORT + "/" + DB_NAME);
    
        try (Connection conn = connect()) {
            if (!isMigrationNeeded(conn)) {
                warn("Migration not needed.");
                return;
            }
    
            warn("This migration will convert messages.id from BIGINT to VARCHAR(36).");
            warn("This is a destructive change. Please ensure you have backups.");
    
            if (!isConfirmed(args)) {
                warn("Confirmation flag not found. Use --confirm or --force to run this migration.");
                log("Migration cancelled.");
                return;
            }
    
            backupMessagesTable();
            runMigrationScript(conn);
            verifyMigration(conn);
            log("=== Migration completed successfully ===");
    
        } catch (Exception e) {
            error("Migration failed: " + e.getMessage());
        }
    }
    
}
