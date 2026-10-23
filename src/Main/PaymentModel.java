package Main;

/**
 * Model representing a recorded membership payment transaction.
 */
public class PaymentModel {
    private String receiptId;
    private String memberId;
    private String memberName;
    private String durationPlan;
    private double amount;
    private String paymentMode;
    private String paymentDate;
    private String status;

    public PaymentModel(String receiptId, String memberId, String memberName, String durationPlan,
                        double amount, String paymentMode, String paymentDate, String status) {
        this.receiptId = receiptId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.durationPlan = durationPlan;
        this.amount = amount;
        this.paymentMode = paymentMode;
        this.paymentDate = paymentDate;
        this.status = status;
    }

    public String getReceiptId() { return receiptId; }
    public String getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public String getDurationPlan() { return durationPlan; }
    public double getAmount() { return amount; }
    public String getFormattedAmount() { return String.format("₹ %.2f", amount); }
    public String getPaymentMode() { return paymentMode; }
    public String getPaymentDate() { return paymentDate; }
    public String getStatus() { return status; }
}
