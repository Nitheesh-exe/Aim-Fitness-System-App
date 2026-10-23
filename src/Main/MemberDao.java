package Main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Data Access Object managing gym member records, profile modifications,
 * cascading deletions, and bulk CSV ingestion.
 */
public class MemberDao {

    public Connection dbconnect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DatabaseSetup.getConnection();
    }

    /**
     * Inserts a brand new member into the database.
     */
    public void Insert(MemberModel m) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO members (member_id, full_name, phone, email, gender, age, height, weight, status, address) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = dbconnect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getMemberId());
            ps.setString(2, m.getFullName());
            ps.setString(3, m.getPhone());
            ps.setString(4, m.getEmail());
            ps.setString(5, m.getGender());
            ps.setInt(6, m.getAge());
            ps.setString(7, m.getHeight());
            ps.setString(8, m.getWeight());
            ps.setString(9, m.getStatus() != null ? m.getStatus() : "Active");
            ps.setString(10, m.getAddress() != null ? m.getAddress() : "N/A");
            ps.executeUpdate();
        }
    }

    /**
     * Retrieves all members ordered alphabetically by full name.
     */
    public ArrayList<MemberModel> view() throws ClassNotFoundException, SQLException {
        ArrayList<MemberModel> list = new ArrayList<>();
        String sql = "SELECT member_id, full_name, phone, email, gender, age, height, weight, status, address "
                + "FROM members ORDER BY full_name ASC";
        try (Connection c = dbconnect();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                MemberModel m = new MemberModel();
                m.setMemberId(rs.getString("member_id"));
                m.setFullName(rs.getString("full_name"));
                m.setPhone(rs.getString("phone"));
                m.setEmail(rs.getString("email"));
                m.setGender(rs.getString("gender"));
                m.setAge(rs.getInt("age"));
                m.setHeight(rs.getString("height"));
                m.setWeight(rs.getString("weight"));
                m.setStatus(rs.getString("status"));
                m.setAddress(rs.getString("address"));
                list.add(m);
            }
        }
        return list;
    }

    /**
     * Updates an existing member's personal information.
     */
    public void update(MemberModel m) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE members SET full_name=?, phone=?, email=?, gender=?, age=?, height=?, weight=? "
                + "WHERE member_id=?";
        try (Connection c = dbconnect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getFullName());
            ps.setString(2, m.getPhone());
            ps.setString(3, m.getEmail());
            ps.setString(4, m.getGender());
            ps.setInt(5, m.getAge());
            ps.setString(6, m.getHeight());
            ps.setString(7, m.getWeight());
            ps.setString(8, m.getMemberId());
            ps.executeUpdate();
        }
    }

    /**
     * Cascades deletion of a member and all related attendance, payments, and subscription records.
     */
    public void delete(String id) throws SQLException, ClassNotFoundException {
        try (Connection c = dbconnect()) {
            c.setAutoCommit(false);
            try (PreparedStatement attendance = c.prepareStatement("DELETE FROM attendance WHERE member_id = ?");
                 PreparedStatement payments = c.prepareStatement("DELETE FROM payments WHERE member_id = ?");
                 PreparedStatement subscriptions = c.prepareStatement("DELETE FROM subscriptions WHERE member_id = ?");
                 PreparedStatement member = c.prepareStatement("DELETE FROM members WHERE member_id = ?")) {

                attendance.setString(1, id);
                attendance.executeUpdate();

                payments.setString(1, id);
                payments.executeUpdate();

                subscriptions.setString(1, id);
                subscriptions.executeUpdate();

                member.setString(1, id);
                member.executeUpdate();

                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /**
     * Ingests a bulk CSV export in an atomic transaction with automatic date normalization.
     */
    public void importFromCSV(String filePath) throws Exception {
        try (Connection c = dbconnect()) {
            c.setAutoCommit(false);

            String memberSql = "INSERT OR REPLACE INTO members (member_id, full_name, phone, status, address) VALUES (?, ?, ?, 'Active', ?)";
            String subSql = "INSERT OR REPLACE INTO subscriptions (member_id, duration_plan, start_date, expiry_date, status) VALUES (?, ?, ?, ?, 'Active')";
            String paySql = "INSERT OR IGNORE INTO payments (member_id, amount, payment_mode) VALUES (?, ?, 'Offline')";

            try (BufferedReader br = new BufferedReader(new FileReader(filePath));
                 PreparedStatement psMem = c.prepareStatement(memberSql);
                 PreparedStatement psSub = c.prepareStatement(subSql);
                 PreparedStatement psPay = c.prepareStatement(paySql)) {

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    String[] data = line.split(",", -1);
                    if (data.length < 8) {
                        continue;
                    }

                    // 1. Sanitize Data
                    String memberId = data[0].trim();
                    if (!memberId.startsWith("AIM-")) {
                        memberId = "AIM-" + memberId;
                    }
                    String name = data[1].trim();
                    String phone = data[2].trim();
                    String plan = data[3].trim();
                    String start = normalizeDate(data[4].trim());
                    String expiry = normalizeDate(data[6].trim());

                    double amount = 0.0;
                    try {
                        amount = Double.parseDouble(data[7].trim());
                    } catch (Exception ignored) {}

                    String address = "N/A";
                    if (data.length > 9 && data[9] != null && !data[9].trim().isEmpty()) {
                        address = data[9].trim();
                    }

                    // 2. Queue Batches
                    psMem.setString(1, memberId);
                    psMem.setString(2, name);
                    psMem.setString(3, phone);
                    psMem.setString(4, address);
                    psMem.addBatch();

                    psSub.setString(1, memberId);
                    psSub.setString(2, plan);
                    psSub.setString(3, start);
                    psSub.setString(4, expiry);
                    psSub.addBatch();

                    psPay.setString(1, memberId);
                    psPay.setDouble(2, amount);
                    psPay.addBatch();
                }

                // 3. Execute and Commit
                psMem.executeBatch();
                psSub.executeBatch();
                psPay.executeBatch();
                c.commit();

            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /**
     * Converts varied legacy CSV date formats into ISO standard YYYY-MM-DD.
     */
    private String normalizeDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return LocalDate.now().toString();
        }

        String[] formats = {"dd.MM.yyyy", "M/d/yyyy", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd"};
        for (String pattern : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDate.parse(rawDate.trim(), formatter).toString();
            } catch (Exception ignored) {}
        }

        return LocalDate.now().toString();
    }

    /**
     * Exports all members and their current active subscription into a clean RFC-4180 CSV file with UTF-8 BOM.
     *
     * @param file Target destination file chosen by user
     * @return Number of member rows exported
     */
    public int exportToCSV(java.io.File file) throws Exception {
        String sql = "SELECT m.member_id, m.full_name, m.phone, m.email, m.gender, m.age, m.height, m.weight, m.status, m.address, "
                + "s.duration_plan, s.start_date, s.expiry_date "
                + "FROM members m "
                + "LEFT JOIN (SELECT member_id, duration_plan, start_date, expiry_date, MAX(id) FROM subscriptions GROUP BY member_id) s "
                + "  ON m.member_id = s.member_id "
                + "ORDER BY m.member_id ASC";

        int count = 0;
        try (Connection c = dbconnect();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {

            // UTF-8 Byte Order Mark for universal Excel/Numbers compatibility
            bw.write('\ufeff');

            // CSV Header
            bw.write("Member ID,Full Name,Phone,Email,Gender,Age,Height,Weight,Status,Address,Plan,Start Date,Expiry Date\n");

            while (rs.next()) {
                count++;
                bw.write(escapeCsv(rs.getString("member_id")) + ",");
                bw.write(escapeCsv(rs.getString("full_name")) + ",");
                bw.write(escapeCsv(rs.getString("phone")) + ",");
                bw.write(escapeCsv(rs.getString("email")) + ",");
                bw.write(escapeCsv(rs.getString("gender")) + ",");
                bw.write(rs.getInt("age") + ",");
                bw.write(escapeCsv(rs.getString("height")) + ",");
                bw.write(escapeCsv(rs.getString("weight")) + ",");
                bw.write(escapeCsv(rs.getString("status")) + ",");
                bw.write(escapeCsv(rs.getString("address")) + ",");
                bw.write(escapeCsv(rs.getString("duration_plan")) + ",");
                bw.write(escapeCsv(rs.getString("start_date")) + ",");
                bw.write(escapeCsv(rs.getString("expiry_date")));
                bw.write("\n");
            }
        }
        return count;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }
        String v = value.trim();
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return "\"" + v + "\"";
    }
}
