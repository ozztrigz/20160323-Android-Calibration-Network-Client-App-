package ca.on.tradeport.calibrationnetwork;


public class User {

    public String userID;
    public String name;
    public String userType;
    public String address1;
    public String address2;
    public String province;
    public String city;
    public String country;
    public String postalCode;
    public String email;
    public String phone;
    public String username;

    public User(String userID, String name, String userType, String address1, String address2,
                String province, String city, String country, String postalCode, String email,
                String phone, String username) {

        this.userID = userID;
        this.name = name;
        this.userType = userType;
        this.address1 = address1;
        this.address2 = address2;
        this.province = province;
        this.city = city;
        this.country = country;
        this.postalCode = postalCode;
        this.email = email;
        this.phone = phone;
        this.username = username;


    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
