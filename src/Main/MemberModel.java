package Main;

/**
 * Data Model representing a Gym Member profile.
 */
public class MemberModel {
    
    private String memberId;
    private String fullName;
    private String phone;
    private String email;
    private String gender;
    private int age;
    private String height;
    private String weight;
    private String status;
    private String address;

    public MemberModel() {}

    public MemberModel(String memberId, String fullName, String phone, String email, String gender,
                       int age, String height, String weight, String status) {
        this(memberId, fullName, phone, email, gender, age, height, weight, status, "N/A");
    }

    public MemberModel(String memberId, String fullName, String phone, String email, String gender,
                       int age, String height, String weight, String status, String address) {
        this.memberId = memberId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.status = status;
        this.address = address;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddress() {
        return address != null ? address : "N/A";
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
