package id.example.sehatin.chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import id.example.sehatin.R;
import io.noties.markwon.Markwon; // For Markdown Support

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messages;
    private Markwon markwon; // Helper to render Bold/Italic text from AI

    public ChatAdapter(List<ChatMessage> messages, Markwon markwon) {
        this.messages = messages;
        this.markwon = markwon;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        if (msg.isUser) {
            holder.layoutBot.setVisibility(View.GONE);
            holder.layoutUser.setVisibility(View.VISIBLE);
            holder.tvUser.setText(msg.message);
        } else {
            holder.layoutUser.setVisibility(View.GONE);
            holder.layoutBot.setVisibility(View.VISIBLE);
            // Render Markdown for Bot
            markwon.setMarkdown(holder.tvBot, msg.message);
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutBot, layoutUser;
        TextView tvBot, tvUser;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutBot = itemView.findViewById(R.id.layout_bot);
            layoutUser = itemView.findViewById(R.id.layout_user);
            tvBot = itemView.findViewById(R.id.tv_msg_bot);
            tvUser = itemView.findViewById(R.id.tv_msg_user);
        }
    }
}