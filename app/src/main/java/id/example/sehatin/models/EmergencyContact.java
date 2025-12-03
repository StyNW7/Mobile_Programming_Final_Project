package id.example.sehatin.models;

public class EmergencyContact {
    public String id;
    public String contactName;
    public String relationship; // midwife, doctor, clinic, husband, etc.
    public String phoneNumber;
    public String additionalInfo; // optional: work hours, clinic address

    public EmergencyContact() {}

    public EmergencyContact(String id, String contactName, String relationship,
                            String phoneNumber, String additionalInfo) {
        this.id = id;
        this.contactName = contactName;
        this.relationship = relationship;
        this.phoneNumber = phoneNumber;
        this.additionalInfo = additionalInfo;
    }
}
