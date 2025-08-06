import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public class MessageIdUuidMigrator {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/pgb?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "medusa";

    public static void main(String[] args) {
        try (
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement selectStmt = conn.prepareStatement("SELECT id FROM messages");
            PreparedStatement updateStmt = conn.prepareStatement("UPDATE messages SET id = ? WHERE id = ?");
        ) {
            conn.setAutoCommit(false);
            ResultSet rs = selectStmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                String oldId = rs.getString("id");
                String newId = UUID.randomUUID().toString().toUpperCase();

                updateStmt.setString(1, newId);
                updateStmt.setString(2, oldId);
                updateStmt.addBatch();

                count++;
                if (count % 1000 == 0) {
                    updateStmt.executeBatch();
                    conn.commit();
                    System.out.printf("Updated %d records...\n", count);
                }
            }

            updateStmt.executeBatch();
            conn.commit();

            System.out.println("Migration completed. Total updated rows: " + count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
