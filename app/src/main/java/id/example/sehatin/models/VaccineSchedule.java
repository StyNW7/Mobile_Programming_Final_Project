package id.example.sehatin.models;

public class VaccineSchedule {
    public String id;
    public String userId;
    public String childId;
    public String vaccineName;
    public String scheduledDate; // "yyyy-MM-dd"
    public String reminderDate;  // optional
    public boolean isCompleted;
    public String completedDate; // optional
    public String notes;

    public VaccineSchedule() {}

    public VaccineSchedule(String id, String userId, String childId, String vaccineName,
                           String scheduledDate, String reminderDate, boolean isCompleted,
                           String completedDate, String notes) {
        this.id = id;
        this.userId = userId;
        this.childId = childId;
        this.vaccineName = vaccineName;
        this.scheduledDate = scheduledDate;
        this.reminderDate = reminderDate;
        this.isCompleted = isCompleted;
        this.completedDate = completedDate;
        this.notes = notes;
    }
}
