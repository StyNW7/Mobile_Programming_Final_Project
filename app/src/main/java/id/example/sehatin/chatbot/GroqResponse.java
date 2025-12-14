package id.example.sehatin.chatbot;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GroqResponse {
    @SerializedName("choices")
    public List<Choice> choices;

    public static class Choice {
        @SerializedName("message")
        public Message message;
    }

    public static class Message {
        @SerializedName("content")
        public String content;
    }
}