package id.example.sehatin.models;

public class EmergencyContact {
    public String id;
    public String userId; // owner of this contact (parent)
    public String contactName;
    public String relationship; // midwife, doctor, clinic, husband, etc.
    public String phoneNumber;
    public String additionalInfo; // optional: work hours, clinic address

    public EmergencyContact() {}

    public EmergencyContact(String id, String userId,
                            String contactName, String relationship,
                            String phoneNumber, String additionalInfo) {
        this.id = id;
        this.userId = userId;
        this.contactName = contactName;
        this.relationship = relationship;
        this.phoneNumber = phoneNumber;
        this.additionalInfo = additionalInfo;
    }
}
