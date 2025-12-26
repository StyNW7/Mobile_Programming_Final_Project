package id.example.sehatin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.example.sehatin.R;
import id.example.sehatin.models.JadwalItem;

public class JadwalImunisasiAdapter extends RecyclerView.Adapter<JadwalImunisasiAdapter.JadwalViewHolder> {
    private final List<JadwalItem> jadwalList;
    private final OnItemCheckListener listener;

    public interface OnItemCheckListener {
        void onItemCheck(JadwalItem item);
    }

    public JadwalImunisasiAdapter(List<JadwalItem> jadwalList, OnItemCheckListener listener) {
        this.jadwalList = jadwalList;
        this.listener = listener;
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
        
        holder.cbIsCompleted.setOnCheckedChangeListener(null);
        holder.cbIsCompleted.setChecked(item.isCompleted());

        holder.cbIsCompleted.setOnClickListener(v -> {
            boolean isChecked = holder.cbIsCompleted.isChecked();
            item.setCompleted(isChecked);
            if (listener != null) {
                listener.onItemCheck(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return jadwalList.size();
    }

    public static class JadwalViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNamaVaksin;
        final TextView tvTanggalVaksin;
        final CheckBox cbIsCompleted;

        public JadwalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaVaksin = itemView.findViewById(R.id.tv_nama_vaksin);
            tvTanggalVaksin = itemView.findViewById(R.id.tv_tanggal_vaksin);
            cbIsCompleted = itemView.findViewById(R.id.cb_is_completed);
        }
    }
}
