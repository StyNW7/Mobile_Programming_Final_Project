package id.example.sehatin.chatbot;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ChatService {

    // Using Groq API (Llama 3)
    @POST("openai/v1/chat/completions")
    Call<GroqResponse> getChatResponse(
            @Header("Authorization") String token,
            @Body GroqRequest request
    );
}