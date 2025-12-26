package id.example.sehatin.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

@SuppressWarnings("unused")
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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Double getBirthWeight() { return birthWeight; }
    public void setBirthWeight(Double birthWeight) { this.birthWeight = birthWeight; }

    public Double getBirthHeight() { return birthHeight; }
    public void setBirthHeight(Double birthHeight) { this.birthHeight = birthHeight; }
}
