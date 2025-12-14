package id.example.sehatin.chatbot;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class GroqRequest {

    // FIX: Use the 8b model (Standard & Most Stable)
    @SerializedName("model")
    public String model = "llama-3.1-8b-instant";

    @SerializedName("messages")
    public List<Message> messages;

    public GroqRequest(String userPrompt) {
        this.messages = new ArrayList<>();
        // System Prompt
        this.messages.add(new Message("system", "Anda adalah asisten kesehatan Sehatin. Jawablah singkat, ramah, dan dalam Bahasa Indonesia."));
        // User Prompt
        this.messages.add(new Message("user", userPrompt));
    }

    public static class Message {
        @SerializedName("role")
        public String role;

        @SerializedName("content")
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}