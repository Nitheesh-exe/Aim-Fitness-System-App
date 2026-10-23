package Main;

public class AttendanceModel {
    private String memberId;
    private String memberName;
    private String checkIn;
    private String checkOut;
    private String duration;

    public AttendanceModel(String memberId, String memberName, String checkIn, String checkOut, String duration) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.duration = duration;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
    

}