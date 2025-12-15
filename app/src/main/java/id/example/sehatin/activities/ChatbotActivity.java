package id.example.sehatin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import id.example.sehatin.BuildConfig;
import id.example.sehatin.R;
import id.example.sehatin.chatbot.ChatAdapter;
import id.example.sehatin.chatbot.ChatMessage;
import id.example.sehatin.chatbot.ChatService;
import id.example.sehatin.chatbot.GroqRequest; // NEW
import id.example.sehatin.chatbot.GroqResponse; // NEW
import id.example.sehatin.utils.SessionManager;
import io.noties.markwon.Markwon;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatbotActivity extends AppCompatActivity {

    // --- CONFIGURATION ---

    private static final String GROQ_API_KEY = "Bearer " + BuildConfig.GROQ_API_KEY;

    private static final String BASE_URL = "https://api.groq.com/";

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    private ChatService chatService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        sessionManager = new SessionManager(this);
        setupViews();
        setupNavigation();
        setupRetrofit();

        addBotMessage("Halo! Saya asisten Sehatin (Llama 3). Ada yang bisa saya bantu?");
    }

    private void setupViews() {
        rvChat = findViewById(R.id.rv_chat);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);

        messageList = new ArrayList<>();
        Markwon markwon = Markwon.create(this);
        adapter = new ChatAdapter(messageList, markwon);

        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());

        TextView tvTitle = findViewById(R.id.tvToolbarTitle);
        if (tvTitle != null) tvTitle.setText("Sehatin AI");

        findViewById(R.id.btnSignOut).setOnClickListener(v -> {
            sessionManager.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        chatService = retrofit.create(ChatService.class);
    }

    private void sendMessage() {
        String query = etMessage.getText().toString().trim();
        if (query.isEmpty()) return;

        // 1. Add User Message
        messageList.add(new ChatMessage(query, true));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvChat.smoothScrollToPosition(messageList.size() - 1);
        etMessage.setText("");

        // 2. Call Groq API
        GroqRequest request = new GroqRequest(query);

        chatService.getChatResponse(GROQ_API_KEY, request).enqueue(new Callback<GroqResponse>() {
            @Override
            public void onResponse(Call<GroqResponse> call, Response<GroqResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().choices.isEmpty()) {
                    try {
                        String botReply = response.body().choices.get(0).message.content;
                        addBotMessage(botReply);
                    } catch (Exception e) {
                        addBotMessage("Format jawaban aneh.");
                    }
                } else {
                    // === CRITICAL FIX: READ THE ERROR MESSAGE ===
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown Error";
                        android.util.Log.e("GROQ_ERROR", "Error: " + errorBody); // Check Logcat for this!

                        // Show a specific hint to the user
                        if (response.code() == 401) {
                            addBotMessage("Error 401: API Key salah. Cek 'Bearer' dan spasi.");
                        } else if (response.code() == 400) {
                            addBotMessage("Error 400: Format Request Salah. Cek Logcat.");
                        } else {
                            addBotMessage("Gagal (" + response.code() + "): " + errorBody);
                        }
                    } catch (Exception e) {
                        addBotMessage("Gagal terhubung: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<GroqResponse> call, Throwable t) {
                addBotMessage("Gagal koneksi internet: " + t.getMessage());
            }
        });
    }

    private void addBotMessage(String message) {
        messageList.add(new ChatMessage(message, false));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvChat.smoothScrollToPosition(messageList.size() - 1);
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        FloatingActionButton fab = findViewById(R.id.fabEmergency);

        bottomNav.setSelectedItemId(R.id.nav_chatbot);
        bottomNav.getMenu().findItem(R.id.nav_placeholder).setEnabled(false);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_chatbot) {
                return true;
            } else if (id == R.id.nav_article) {
                startActivity(new Intent(this, InfoRubrikActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });

        fab.setOnClickListener(v -> startActivity(new Intent(this, EmergencyActivity.class)));
    }
}