package id.example.sehatin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.example.sehatin.R;
import id.example.sehatin.models.JadwalItem;

public class JadwalImunisasiAdapter extends RecyclerView.Adapter<JadwalImunisasiAdapter.JadwalViewHolder> {

    private List<JadwalItem> jadwalList;

    public JadwalImunisasiAdapter(List<JadwalItem> jadwalList) {
        this.jadwalList = jadwalList;
    }

    @NonNull
    @Override
    public JadwalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_jadwal_imunisasi, parent, false);
        return new JadwalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JadwalViewHolder holder, int position) {
        JadwalItem item = jadwalList.get(position);
        holder.tvNamaVaksin.setText(item.getNamaVaksin() + " (Usia " + item.getUsia() + " bulan)");
        holder.tvTanggalVaksin.setText("📅 " + item.getTanggal());
    }

    @Override
    public int getItemCount() {
        return jadwalList.size();
    }

    static class JadwalViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaVaksin;
        TextView tvTanggalVaksin;

        public JadwalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaVaksin = itemView.findViewById(R.id.tv_nama_vaksin);
            tvTanggalVaksin = itemView.findViewById(R.id.tv_tanggal_vaksin);
        }
    }
}
