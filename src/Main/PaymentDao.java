package Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Data Access Object for financial transaction ledgers, payment modes, and revenue auditing.
 */
public class PaymentDao {

    public Connection dbconnect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DatabaseSetup.getConnection();
    }

    /**
     * Retrieves all payment transactions ordered by latest receipt ID.
     */
    public ArrayList<PaymentModel> getAllPayments() throws Exception {
        ArrayList<PaymentModel> list = new ArrayList<>();
        String sql = "SELECT p.id, p.member_id, coalesce(m.full_name, 'Unknown Member') as full_name, "
                + "coalesce(s.duration_plan, 'Standard Membership') as duration_plan, "
                + "p.amount, coalesce(p.payment_mode, 'Cash') as payment_mode, "
                + "coalesce(p.payment_date, coalesce(s.start_date, date('now', 'localtime'))) as payment_date "
                + "FROM payments p "
                + "LEFT JOIN members m ON p.member_id = m.member_id "
                + "LEFT JOIN (SELECT member_id, duration_plan, start_date, MAX(id) FROM subscriptions GROUP BY member_id) s "
                + "  ON p.member_id = s.member_id "
                + "ORDER BY p.id DESC";

        try (Connection c = dbconnect();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String receiptId = String.format("TXN-%04d", id);
                String memberId = rs.getString("member_id");
                String name = rs.getString("full_name");
                String plan = rs.getString("duration_plan");
                double amount = rs.getDouble("amount");
                String mode = rs.getString("payment_mode");
                String date = rs.getString("payment_date");
                list.add(new PaymentModel(receiptId, memberId, name, plan, amount, mode, date, "Paid"));
            }
        }
        return list;
    }

    /**
     * Calculates summary metrics: [Total Revenue, Cash Collections, Digital (UPI/Card), Total Count].
     */
    public double[] getPaymentStats() throws Exception {
        double totalRevenue = 0.0;
        double cashTotal = 0.0;
        double digitalTotal = 0.0;
        int count = 0;

        String sql = "SELECT amount, payment_mode FROM payments";
        try (Connection c = dbconnect();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                count++;
                double amt = rs.getDouble("amount");
                totalRevenue += amt;
                String mode = rs.getString("payment_mode");
                if (mode != null && (mode.equalsIgnoreCase("Cash") || mode.equalsIgnoreCase("Offline"))) {
                    cashTotal += amt;
                } else {
                    digitalTotal += amt;
                }
            }
        }
        return new double[]{totalRevenue, cashTotal, digitalTotal, count};
    }

    /**
     * Exports transactions ledger to a clean CSV file.
     */
    public int exportPaymentsToCSV(File file) throws Exception {
        ArrayList<PaymentModel> payments = getAllPayments();
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            bw.write('\ufeff');
            bw.write("Receipt ID,Member ID,Member Name,Plan Duration,Amount (INR),Payment Mode,Date,Status\n");
            for (PaymentModel p : payments) {
                bw.write(escapeCsv(p.getReceiptId()) + ",");
                bw.write(escapeCsv(p.getMemberId()) + ",");
                bw.write(escapeCsv(p.getMemberName()) + ",");
                bw.write(escapeCsv(p.getDurationPlan()) + ",");
                bw.write(String.format("%.2f", p.getAmount()) + ",");
                bw.write(escapeCsv(p.getPaymentMode()) + ",");
                bw.write(escapeCsv(p.getPaymentDate()) + ",");
                bw.write(escapeCsv(p.getStatus()) + "\n");
            }
        }
        return payments.size();
    }

    private String escapeCsv(String val) {
        if (val == null) return "\"\"";
        String v = val.trim();
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return "\"" + v + "\"";
    }
}
