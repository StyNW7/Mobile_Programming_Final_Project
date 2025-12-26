package id.example.sehatin.models;

public class JadwalItem {
    private final String namaVaksin;
    private final String tanggal;
    private final int usia;

    private boolean isCompleted;
    
    public JadwalItem(String namaVaksin, String tanggal, int usia) {
        this.namaVaksin = namaVaksin;
        this.tanggal = tanggal;
        this.usia = usia;
        this.isCompleted = false;
    }

    public String getNamaVaksin() {
        return namaVaksin;
    }

    public String getTanggal() {
        return tanggal;
    }

    public int getUsia() {
        return usia;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
