package com.quanlydatvemaybay.entity;

public class User {
    private Long id;
    private String userName;
    private String password;
    private String fullName;
    private String email;
    private String sdt;
    private Boolean status;
    private String role;

    public User() {}

    public User(Long id, String userName, String password, String fullName, String email, String sdt, Boolean status) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.sdt = sdt;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isAdmin() { return "ADMIN".equals(role); }

    @Override
    public String toString() {
        return fullName + " (" + userName + ")";
    }
}
