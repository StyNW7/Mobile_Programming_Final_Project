package id.example.sehatin.models;

public class User {
    public String id;      // use Firebase UID or auto id
    public String fcmToken;
    public String name;
    public String email;
    public String phoneNumber;

    public User() {} // required for Firestore

    public User(String id, String fcmToken, String name, String email, String phoneNumber) {
        this.id = id;
        this.fcmToken = fcmToken;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }
}
