package id.example.sehatin.models;

public class Child {
    public String id;
    public String userId;    // parent's uid
    public String name;
    public String birthDate; // ISO string "yyyy-MM-dd"
    public String gender;    // "male"/"female"
    public Double birthWeight;
    public Double birthHeight;

    public Child() {}

    public Child(String id, String userId, String name, String birthDate, String gender,
                 Double birthWeight, Double birthHeight) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.birthWeight = birthWeight;
        this.birthHeight = birthHeight;
    }
}
