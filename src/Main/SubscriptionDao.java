package Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Data Access Object for Subscription lifecycles, renewal transactions,
 * and querying members pending automated 7-day expiry WhatsApp notifications.
 */
public class SubscriptionDao {

    public Connection dbconnect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DatabaseSetup.getConnection();
    }

    /**
     * Atomically records a plan renewal and logs payment transaction.
     */
    public void processRenewal(String memberId, String duration, String startDate, String expiryDate,
                               double amount, String mode) throws Exception {
        String subSql = "INSERT INTO subscriptions (member_id, duration_plan, start_date, expiry_date, status) "
                + "VALUES (?, ?, ?, ?, 'Active')";
        String paySql = "INSERT INTO payments (member_id, amount, payment_mode, payment_date) VALUES (?, ?, ?, ?)";

        try (Connection c = dbconnect()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps1 = c.prepareStatement(subSql);
                 PreparedStatement ps2 = c.prepareStatement(paySql)) {

                ps1.setString(1, memberId);
                ps1.setString(2, duration);
                ps1.setString(3, startDate);
                ps1.setString(4, expiryDate);
                ps1.executeUpdate();

                ps2.setString(1, memberId);
                ps2.setDouble(2, amount);
                ps2.setString(3, mode != null ? mode : "Cash");
                ps2.setString(4, startDate != null ? startDate : java.time.LocalDate.now().toString());
                ps2.executeUpdate();

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
     * Fetches members whose latest subscription expires within the next 7 days
     * and who have not yet received an automated WhatsApp reminder (reminder_sent = 0).
     *
     * @return List of String arrays: [sub_id, full_name, phone, expiry_date]
     */
    public ArrayList<String[]> getPendingExpiriesForReminder() throws Exception {
        ArrayList<String[]> recipients = new ArrayList<>();
        String query = "SELECT s.id, s.member_id, m.full_name, m.phone, s.expiry_date "
                + "FROM subscriptions s "
                + "JOIN members m ON s.member_id = m.member_id "
                + "WHERE s.expiry_date BETWEEN date('now', 'localtime') AND date('now', 'localtime', '+7 days') "
                + "AND (s.reminder_sent IS NULL OR s.reminder_sent = 0) "
                + "AND s.rowid = (SELECT MAX(s2.rowid) FROM subscriptions s2 WHERE s2.member_id = s.member_id)";

        try (Connection c = dbconnect();
             PreparedStatement ps = c.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                recipients.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("expiry_date")
                });
            }
        }
        return recipients;
    }

    /**
     * Marks a subscription reminder as dispatched to guarantee idempotency.
     */
    public void markReminderSent(int subscriptionId) throws Exception {
        String sql = "UPDATE subscriptions SET reminder_sent = 1 WHERE id = ?";
        try (Connection c = dbconnect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, subscriptionId);
            ps.executeUpdate();
        }
    }

    /**
     * Returns the latest subscription status for every member.
     */
    public ArrayList<SubscriptionModel> viewAll() throws Exception {
        ArrayList<SubscriptionModel> list = new ArrayList<>();
        String query = "SELECT s.member_id, m.full_name, m.address, s.duration_plan, s.start_date, s.expiry_date, s.status "
                + "FROM subscriptions s "
                + "JOIN members m ON s.member_id = m.member_id "
                + "WHERE s.rowid = (SELECT MAX(rowid) FROM subscriptions s2 WHERE s2.member_id = s.member_id) "
                + "ORDER BY s.expiry_date DESC";

        try (Connection c = dbconnect();
             PreparedStatement ps = c.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new SubscriptionModel(
                        rs.getString("member_id"),
                        rs.getString("full_name"),
                        rs.getString("address"),
                        rs.getString("duration_plan"),
                        rs.getString("start_date"),
                        rs.getString("expiry_date"),
                        rs.getString("status")
                ));
            }
        }
        return list;
    }

    /**
     * Retrieves all members expiring within 14 days for the WhatsApp Alerts Hub.
     */
    public ArrayList<WhatsAppReminderModel> getAllUpcomingExpiries() throws Exception {
        ArrayList<WhatsAppReminderModel> list = new ArrayList<>();
        String query = "SELECT s.id, s.member_id, coalesce(m.full_name, 'Unknown') as full_name, "
                + "coalesce(m.phone, 'N/A') as phone, s.expiry_date, coalesce(s.reminder_sent, 0) as reminder_sent "
                + "FROM subscriptions s "
                + "JOIN members m ON s.member_id = m.member_id "
                + "WHERE s.expiry_date BETWEEN date('now', 'localtime', '-1 day') AND date('now', 'localtime', '+14 days') "
                + "AND s.rowid = (SELECT MAX(s2.rowid) FROM subscriptions s2 WHERE s2.member_id = s.member_id) "
                + "ORDER BY s.expiry_date ASC";

        try (Connection c = dbconnect();
             PreparedStatement ps = c.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            java.time.LocalDate today = java.time.LocalDate.now();
            while (rs.next()) {
                int subId = rs.getInt("id");
                String memberId = rs.getString("member_id");
                String name = rs.getString("full_name");
                String phone = rs.getString("phone");
                String expiry = rs.getString("expiry_date");
                int reminderSent = rs.getInt("reminder_sent");

                String daysRem = "0 days";
                try {
                    java.time.LocalDate expDate = java.time.LocalDate.parse(expiry);
                    long days = java.time.temporal.ChronoUnit.DAYS.between(today, expDate);
                    if (days < 0) {
                        daysRem = "Expired (" + Math.abs(days) + "d ago)";
                    } else if (days == 0) {
                        daysRem = "Expires Today";
                    } else {
                        daysRem = days + " days left";
                    }
                } catch (Exception ignored) {}

                String status = reminderSent == 1 ? "Sent" : "Pending";
                list.add(new WhatsAppReminderModel(subId, memberId, name, phone, expiry, daysRem, status));
            }
        }
        return list;
    }
}
