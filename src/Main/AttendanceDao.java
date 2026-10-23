package Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Data Access Object for Member Attendance check-in, check-out, and
 * dynamic elapsed workout duration calculation via SQLite Julian day math.
 */
public class AttendanceDao {

    public Connection dbconnect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DatabaseSetup.getConnection();
    }

    /**
     * Records a check-in for a verified member at the current local timestamp.
     *
     * @param memberId Unique member identifier (e.g. "AIM-12345")
     * @throws Exception if member does not exist in database
     */
    public void checkIn(String memberId) throws Exception {
        try (Connection c = dbconnect();
             PreparedStatement check = c.prepareStatement("SELECT member_id FROM members WHERE member_id = ?");
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO attendance (member_id, check_in_time) VALUES (?, datetime('now', 'localtime'))")) {

            check.setString(1, memberId);
            try (ResultSet rs = check.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("Member ID [" + memberId + "] does not exist in the database!");
                }
            }

            ps.setString(1, memberId);
            ps.executeUpdate();
        }
    }

    /**
     * Checks out an active member and computes workout duration dynamically.
     * Duration formula: (days * 24) hours + ((days * 24 * 60) % 60) minutes.
     *
     * @param memberId Unique member identifier
     * @throws Exception if no active (unclosed) check-in exists for this member
     */
    public void checkOut(String memberId) throws Exception {
        String updateSql = "UPDATE attendance SET "
                + "check_out_time = datetime('now', 'localtime'), "
                + "duration = CAST(((julianday(datetime('now', 'localtime')) - julianday(check_in_time)) * 24) AS INT) || 'h ' || "
                + "CAST((((julianday(datetime('now', 'localtime')) - julianday(check_in_time)) * 24 * 60) % 60) AS INT) || 'm' "
                + "WHERE member_id = ? AND check_out_time IS NULL";

        try (Connection c = dbconnect();
             PreparedStatement ps = c.prepareStatement(updateSql)) {
            ps.setString(1, memberId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new Exception("No active check-in found for member [" + memberId + "]!");
            }
        }
    }

    /**
     * Retrieves attendance history joined with member full names, ordered latest first.
     */
    public ArrayList<AttendanceModel> viewAll() throws Exception {
        ArrayList<AttendanceModel> list = new ArrayList<>();
        String query = "SELECT a.member_id, m.full_name, "
                + "strftime('%d-%m-%Y %H:%M', a.check_in_time) AS in_time, "
                + "strftime('%d-%m-%Y %H:%M', a.check_out_time) AS out_time, "
                + "a.duration "
                + "FROM attendance a "
                + "LEFT JOIN members m ON a.member_id = m.member_id "
                + "ORDER BY a.check_in_time DESC";

        try (Connection c = dbconnect();
             PreparedStatement ps = c.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new AttendanceModel(
                        rs.getString("member_id"),
                        rs.getString("full_name") != null ? rs.getString("full_name") : "Unknown",
                        rs.getString("in_time"),
                        rs.getString("out_time") != null ? rs.getString("out_time") : "Active Now",
                        rs.getString("duration") != null ? rs.getString("duration") : "--"
                ));
            }
        }
        return list;
    }
}
