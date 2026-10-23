package Main;

public class SubscriptionModel {
    private String memberId;
    private String memberName;
    private String address; // ✅ Added Address variable
    private String planDuration;
    private String startDate;
    private String expiryDate;
    private String status;

    // ✅ Updated Constructor to accept 7 arguments
    public SubscriptionModel(String memberId, String memberName, String address, String planDuration, String startDate, String expiryDate, String status) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.address = address;
        this.planDuration = planDuration;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public String getMemberInfo() { 
        return memberId + " - " + memberName; 
    }

    // Standard Getters
    public String getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public String getAddress() { return address; } // ✅ Added Getter
    public String getPlanDuration() { return planDuration; }
    public String getStartDate() { return startDate; }
    public String getExpiryDate() { return expiryDate; }
    public String getStatus() { return status; }
    
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public void setPlanDuration(String planDuration) { this.planDuration = planDuration; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public void setStatus(String status) { this.status = status; }
}