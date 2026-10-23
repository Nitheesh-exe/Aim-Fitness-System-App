package Main;

/**
 * Model representing an upcoming membership expiry reminder for WhatsApp messaging.
 */
public class WhatsAppReminderModel {
    private int subscriptionId;
    private String memberId;
    private String fullName;
    private String phone;
    private String expiryDate;
    private String daysRemaining;
    private String status;

    public WhatsAppReminderModel(int subscriptionId, String memberId, String fullName, String phone,
                                 String expiryDate, String daysRemaining, String status) {
        this.subscriptionId = subscriptionId;
        this.memberId = memberId;
        this.fullName = fullName;
        this.phone = phone;
        this.expiryDate = expiryDate;
        this.daysRemaining = daysRemaining;
        this.status = status;
    }

    public int getSubscriptionId() { return subscriptionId; }
    public String getMemberId() { return memberId; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getExpiryDate() { return expiryDate; }
    public String getDaysRemaining() { return daysRemaining; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
