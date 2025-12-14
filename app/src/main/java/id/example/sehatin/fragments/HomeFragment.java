package id.example.sehatin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import id.example.sehatin.R;
import id.example.sehatin.activities.*;
import id.example.sehatin.models.User;
import id.example.sehatin.utils.SessionManager;

public class HomeFragment extends Fragment {

    private SessionManager sessionManager;
    private User currentUser;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        currentUser = sessionManager.getUserDetail();

        // 1. Set Welcome Text
        TextView tvWelcome = view.findViewById(R.id.tvWelcomeName);
        if (currentUser != null && currentUser.name != null) {
            tvWelcome.setText(currentUser.name);
        }

        // 2. Setup Feature Buttons
        LinearLayout btnJadwal = view.findViewById(R.id.btnJadwal);
        LinearLayout btnInfo = view.findViewById(R.id.btnInfo);
        LinearLayout btnRiwayat = view.findViewById(R.id.btnRiwayat);
        LinearLayout btnChat = view.findViewById(R.id.btnChat);

        btnJadwal.setOnClickListener(v -> startActivity(new Intent(getActivity(), ImunisasiActivity.class)));
        btnInfo.setOnClickListener(v -> startActivity(new Intent(getActivity(), InfoRubrikActivity.class)));
        btnRiwayat.setOnClickListener(v -> startActivity(new Intent(getActivity(), RiwayatActivity.class)));
        btnChat.setOnClickListener(v -> startActivity(new Intent(getActivity(), EmergencyActivity.class))); // Or ChatActivity

        // 3. Setup Test Button (Logic moved from Activity)
        Button btnTest = view.findViewById(R.id.testButton);
        btnTest.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Testing Worker...", Toast.LENGTH_SHORT).show();
            // Paste your seeding/testing logic here if needed
        });
    }
}