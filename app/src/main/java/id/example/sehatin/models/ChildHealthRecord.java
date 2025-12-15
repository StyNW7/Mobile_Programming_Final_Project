package id.example.sehatin.models;

public class ChildHealthRecord {
    public String id;
    public String childId;
    public String date; // "yyyy-MM-dd"
    public Double weight;
    public Double height;
    public Double headCircumference;
    public String notes;
    public String diagnosis;

    public ChildHealthRecord() {}

    public ChildHealthRecord(String id, String childId, String date,
                             Double weight, Double height, Double headCircumference,
                             String notes, String diagnosis) {
        this.id = id;
        this.childId = childId;
        this.date = date;
        this.weight = weight;
        this.height = height;
        this.headCircumference = headCircumference;
        this.notes = notes;
        this.diagnosis = diagnosis;
    }
}
