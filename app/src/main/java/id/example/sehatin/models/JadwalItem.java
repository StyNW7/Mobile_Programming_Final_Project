package id.example.sehatin.models;

public class JadwalItem {
    private String namaVaksin;
    private String tanggal;
    private int usia;

    public JadwalItem(String namaVaksin, String tanggal, int usia) {
        this.namaVaksin = namaVaksin;
        this.tanggal = tanggal;
        this.usia = usia;
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
}
