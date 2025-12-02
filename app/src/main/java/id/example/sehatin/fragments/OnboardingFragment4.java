package id.example.sehatin.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import id.example.sehatin.R;
import id.example.sehatin.activities.LoginActivity;
import id.example.sehatin.activities.MainActivity;
import id.example.sehatin.activities.RegisterActivity;

public class OnboardingFragment4 extends Fragment {

    public OnboardingFragment4() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_4, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvSkip = view.findViewById(R.id.tvSkip);
        Button btnLogin = view.findViewById(R.id.btn_login);
        Button btnRegister = view.findViewById(R.id.btn_register);

        // ==== SKIP ====
        tvSkip.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), MainActivity.class));
            requireActivity().finish();
        });

        // ==== LOGIN ====
        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), LoginActivity.class)));

        // ==== REGISTER ====
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), RegisterActivity.class)));
    }
}
