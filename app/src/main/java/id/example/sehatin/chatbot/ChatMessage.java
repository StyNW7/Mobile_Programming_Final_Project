package id.example.sehatin.chatbot;

public class ChatMessage {
    public String message;
    public boolean isUser; // true = User, false = AI

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }
}