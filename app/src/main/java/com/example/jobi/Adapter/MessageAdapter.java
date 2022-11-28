package com.example.jobi.Adapter;

import android.content.Context;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobi.MessageActivity;
import com.example.jobi.Model.Chat;
import com.example.jobi.Model.User;
import com.example.jobi.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {


    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;

    private Context mContext;
    private List <Chat> mChat;
    private String imageurl;


    FirebaseUser fuser;

public MessageAdapter(Context mContext, List<Chat> mChat, String imageurl){
        this.mContext = mContext;
        this.mChat = mChat;
        this.imageurl = imageurl;

        }

@NonNull
Context context;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
    }
@Override
public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == MSG_TYPE_RIGHT){

        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right,parent,false);
        return new MessageAdapter.ViewHolder(view);
        }else{

        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left,parent,false);
        return new MessageAdapter.ViewHolder(view);

    }


    }
@Override
public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

    Chat chat= mChat.get(position);
    String type = mChat.get(position).getType();

    if(type.equals("texto")){
        holder.show_message.setVisibility(View.VISIBLE);
        holder.show_messageImage.setVisibility(View.GONE);
        holder.url_pdf.setVisibility(View.GONE);
        holder.Location.setVisibility(View.GONE);
        holder.show_message.setText(chat.getMessage());
        if(position==mChat.size()-1){
            if (chat.getIsseen().equals("true")){
                holder.txt_seen.setText("Visto");
            }else{
                holder.txt_seen.setText("Entregado");
            }
        }else{
            holder.txt_seen.setVisibility(View.GONE);
        }
    }
    if(type.equals("imagen")){
        holder.show_message.setVisibility(View.GONE);
        holder.url_pdf.setVisibility(View.GONE);
        holder.Location.setVisibility(View.GONE);
        holder.show_messageImage.setVisibility(View.VISIBLE);
        Glide.with(context).load(chat.getMessage()).into(holder.show_messageImage);
        if(position==mChat.size()-1){
            if (chat.getIsseen().equals("true")){
                holder.txt_seenI.setText("Visto");
            }else{
                holder.txt_seenI.setText("Entregado");
            }
        }else{
            holder.txt_seenI.setVisibility(View.GONE);
        }
    }
    if(type.equals("archivo")){
        holder.show_message.setVisibility(View.GONE);
        holder.show_messageImage.setVisibility(View.GONE);
        holder.Location.setVisibility(View.GONE);
        holder.url_pdf.setVisibility(View.VISIBLE);
        Glide.with(context).load(chat.getMessage()).into(holder.url_pdf);
        if(position==mChat.size()-1){
            if (chat.getIsseen().equals("true")){
                holder.txt_seenF.setText("Visto");
            }else{
                holder.txt_seenF.setText("Entregado");
            }
        }else{
            holder.txt_seenF.setVisibility(View.GONE);
        }
    }
    if(type.equals("Ubicación")){
        holder.show_message.setVisibility(View.GONE);
        holder.show_messageImage.setVisibility(View.GONE);
        holder.Location.setVisibility(View.VISIBLE);
        holder.url_pdf.setVisibility(View.GONE);
        Glide.with(context).load(chat.getMessage()).into(holder.url_pdf);
        if(position==mChat.size()-1){
            if (chat.getIsseen().equals("true")){
                holder.txt_seenL.setText("Visto");
            }else{
                holder.txt_seenL.setText("Entregado");
            }
        }else{
            holder.txt_seenL.setVisibility(View.GONE);
        }
    }

    holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(chat.getType().equals("archivo") || chat.getType().equals("imagen")){
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setType("application/pdf");
                intent.setData(Uri.parse(chat.getMessage()));
                mContext.startActivity(intent);
            }
            if(chat.getType().equals("Ubicación")){

                // Creamos un uri agarrando nuestra latitud y longitud
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+chat.getLatitud()+","+chat.getLongitud());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Usamos google maps
                mapIntent.setPackage("com.google.android.apps.maps");
                mContext.startActivity(mapIntent);

            }

        }
    });

        }

@Override
public int getItemCount() {
        return mChat.size();
        }

public class ViewHolder extends  RecyclerView.ViewHolder{
    public TextView show_message;
    public ImageView show_messageImage;
    public ImageView profile_image;
    public ImageView url_pdf;
    public ImageView Location;
    public TextView txt_seen;
    public TextView txt_seenI;
    public TextView txt_seenF;
    public TextView txt_seenL;
    public  ViewHolder(View view){
        super(view);

        show_message = view.findViewById(R.id.show_message);
        show_messageImage = view.findViewById(R.id.messageIv);
        profile_image = view.findViewById(R.id.fotouser);
        url_pdf = view.findViewById(R.id.messageFile);
        Location = view.findViewById(R.id.messageLocation);
        txt_seen = view.findViewById(R.id.txt_seen);
        txt_seenI = view.findViewById(R.id.txt_seenImage);
        txt_seenF = view.findViewById(R.id.txt_seenFile);
        txt_seenL = view.findViewById(R.id.txt_seenLoc);
    }

}

    @Override
    public int getItemViewType(int position) {
        fuser= FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;

        }else {
            return MSG_TYPE_LEFT;
        }
    }
}
