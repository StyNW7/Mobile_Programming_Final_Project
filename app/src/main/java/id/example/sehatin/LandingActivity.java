package id.example.sehatin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;

import id.example.sehatin.activities.MainActivity;
import id.example.sehatin.adapters.OnboardingPagerAdapter;
import id.example.sehatin.databinding.ActivityLandingBinding;
import id.example.sehatin.fragments.OnboardingFragment1;
import id.example.sehatin.fragments.OnboardingFragment2;
import id.example.sehatin.fragments.OnboardingFragment3;

public class LandingActivity extends AppCompatActivity {

    private ActivityLandingBinding binding;
    private final int TOTAL_PAGES = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLandingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewPager();
        setupActions();
    }

    private void setupViewPager() {
        OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(this);
        adapter.addFragment(new OnboardingFragment1());
        adapter.addFragment(new OnboardingFragment2());
        adapter.addFragment(new OnboardingFragment3());

        binding.viewPagerOnboarding.setAdapter(adapter);
        binding.viewPagerOnboarding.setOffscreenPageLimit(TOTAL_PAGES);

        new TabLayoutMediator(binding.tabLayoutIndicator, binding.viewPagerOnboarding,
                (tab, position) -> { }).attach();

        binding.viewPagerOnboarding.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (position == TOTAL_PAGES - 1) {
                    binding.btnNext.setImageResource(R.drawable.ic_check);
                } else {
                    binding.btnNext.setImageResource(R.drawable.ic_arrow_right);
                }
            }
        });
    }

    private void setupActions() {
        binding.btnNext.setOnClickListener(v -> {
            int current = binding.viewPagerOnboarding.getCurrentItem();

            if (current < TOTAL_PAGES - 1) {
                binding.viewPagerOnboarding.setCurrentItem(current + 1);
            } else {
                startActivity(new Intent(LandingActivity.this, MainActivity.class));
                finish();
            }
        });

        binding.tvSkip.setOnClickListener(v -> {
            startActivity(new Intent(LandingActivity.this, MainActivity.class));
            finish();
        });
    }
}
