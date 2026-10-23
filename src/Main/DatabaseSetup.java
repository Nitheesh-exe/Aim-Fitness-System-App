package Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the SQLite database connection, schema bootstrapping, foreign keys,
 * performance indexes, and dynamic schema migrations for AIM Fitness Park.
 */
public class DatabaseSetup {

    private static Path activeDatabasePath = null;

    /**
     * Resolves the active database file path and opens a new SQLite JDBC connection.
     * <p>
     * Resolution hierarchy:
     * 1. System property {@code -Daim.db.path=/path/to/db}
     * 2. Environment variable {@code AIM_DB_PATH}
     * 3. Persistent user directory: {@code ~/AimFitnessData/aimfitness.db}
     *    (Automatically seeds from local {@code aimfitness.db} on first launch).
     *
     * @return an open {@link Connection} with SQLite Foreign Keys enabled.
     * @throws SQLException if connection fails or directory cannot be created.
     */
    public static Connection getConnection() throws SQLException {
        if (activeDatabasePath == null) {
            String customPath = System.getProperty("aim.db.path");
            if (customPath == null || customPath.isBlank()) {
                customPath = System.getenv("AIM_DB_PATH");
            }

            if (customPath != null && !customPath.isBlank()) {
                activeDatabasePath = Path.of(customPath).toAbsolutePath().normalize();
            } else {
                String userHome = System.getProperty("user.home");
                if (userHome == null || userHome.isBlank()) {
                    throw new SQLException("The user home directory is not available.");
                }

                Path appDirectory = Path.of(userHome, "AimFitnessData");
                activeDatabasePath = appDirectory.resolve("aimfitness.db");
                Path legacyDatabasePath = Path.of("aimfitness.db").toAbsolutePath().normalize();

                try {
                    Files.createDirectories(appDirectory);
                    if (!Files.exists(activeDatabasePath)
                            && Files.exists(legacyDatabasePath)
                            && !legacyDatabasePath.equals(activeDatabasePath.toAbsolutePath().normalize())) {
                        Files.copy(legacyDatabasePath, activeDatabasePath);
                        System.out.println("📦 Copied seed database from " + legacyDatabasePath + " to " + activeDatabasePath);
                    }
                } catch (IOException e) {
                    throw new SQLException("Could not prepare the AimFitness database directory.", e);
                }
            }
        }

        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + activeDatabasePath.toAbsolutePath());
        // Enforce SQLite Foreign Key constraints
        try (Statement s = conn.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Bootstraps the application schema, indexes, migrations, and default admin account.
     * Executed safely on application startup.
     */
    public static void initializeDatabase() {
        String createMembers = "CREATE TABLE IF NOT EXISTS members ("
                + "member_id TEXT PRIMARY KEY, "
                + "full_name TEXT NOT NULL, "
                + "phone TEXT, "
                + "email TEXT, "
                + "gender TEXT, "
                + "age INTEGER, "
                + "height TEXT, "
                + "weight TEXT, "
                + "status TEXT DEFAULT 'Active', "
                + "address TEXT);";

        String createSubscriptions = "CREATE TABLE IF NOT EXISTS subscriptions ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "member_id TEXT NOT NULL, "
                + "duration_plan TEXT NOT NULL, "
                + "start_date TEXT NOT NULL, "
                + "expiry_date TEXT NOT NULL, "
                + "status TEXT DEFAULT 'Active', "
                + "reminder_sent INTEGER DEFAULT 0, "
                + "FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE);";

        String createAttendance = "CREATE TABLE IF NOT EXISTS attendance ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "member_id TEXT NOT NULL, "
                + "check_in_time TEXT NOT NULL, "
                + "check_out_time TEXT, "
                + "duration TEXT, "
                + "FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE);";

        String createPayments = "CREATE TABLE IF NOT EXISTS payments ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "member_id TEXT NOT NULL, "
                + "amount REAL NOT NULL, "
                + "payment_mode TEXT DEFAULT 'Offline', "
                + "FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE);";

        String createAdminUsers = "CREATE TABLE IF NOT EXISTS admin_users ("
                + "username TEXT PRIMARY KEY, "
                + "password_hash TEXT NOT NULL, "
                + "password_salt TEXT NOT NULL, "
                + "password_iterations INTEGER NOT NULL);";

        try (Connection c = getConnection(); Statement stmt = c.createStatement()) {

            // 1. Core tables
            stmt.execute(createMembers);
            stmt.execute(createSubscriptions);
            stmt.execute(createAttendance);
            stmt.execute(createPayments);
            stmt.execute(createAdminUsers);

            // 2. Performance indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_subscriptions_member_id ON subscriptions(member_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_subscriptions_expiry_date ON subscriptions(expiry_date);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_attendance_member_id ON attendance(member_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_attendance_check_in_time ON attendance(check_in_time);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_payments_member_id ON payments(member_id);");

            // 3. Dynamic schema migrations for existing databases
            try {
                stmt.execute("ALTER TABLE subscriptions ADD COLUMN reminder_sent INTEGER DEFAULT 0;");
            } catch (Exception ignored) {
                // Column already exists
            }
            try {
                stmt.execute("ALTER TABLE members ADD COLUMN address TEXT;");
            } catch (Exception ignored) {
                // Column already exists
            }
            try {
                stmt.execute("ALTER TABLE payments ADD COLUMN payment_date TEXT;");
            } catch (Exception ignored) {
                // Column already exists
            }
            try {
                stmt.execute("CREATE TABLE IF NOT EXISTS gym_profile ("
                        + "id INTEGER PRIMARY KEY, "
                        + "gym_name TEXT, "
                        + "contact_phone TEXT, "
                        + "address TEXT, "
                        + "operating_hours TEXT, "
                        + "currency TEXT);");
                stmt.execute("INSERT OR IGNORE INTO gym_profile (id, gym_name, contact_phone, address, operating_hours, currency) "
                        + "VALUES (1, 'AIM Fitness Park', '+91 98765 12345', 'Main Fitness Boulevard, City Center', '05:00 AM - 10:00 PM (Mon-Sat)', '₹ INR');");
            } catch (Exception ignored) {
            }

            // 4. Initial admin setup
            AdminDao.bootstrapFirstAdmin(c);

            System.out.println("✅ Offline Database initialized successfully at: " + activeDatabasePath.toAbsolutePath());

        } catch (Exception e) {
            System.err.println("❌ Database Setup Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Path getActiveDatabasePath() {
        return activeDatabasePath;
    }
}
