package id.example.sehatin.models;

public class User {
    public String id;      // use Firebase UID or auto id
    public String name;
    public String email;
    public String password;
    public String phoneNumber;
    public String address;

    public User() {} // required for Firestore

    public User(String id, String name, String email, String password, String phoneNumber, String address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
}
