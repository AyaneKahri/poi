package com.example.jobi.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobi.Model.Chat;
import com.example.jobi.Model.User;
import com.example.jobi.R;
import com.google.android.material.transition.Hold;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.ViewHolder> {


    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;
private Context mContext;
private List <Chat> mChat;

FirebaseUser fuser;

public GroupMessageAdapter(Context mContext, List<Chat> mChat ){
        this.mContext = mContext;
        this.mChat = mChat;

        }

    @NonNull
    Context context;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
    }
    @Override
    public GroupMessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT){


            View view = LayoutInflater.from(mContext).inflate(R.layout.chatgroup_item_right,parent,false);
            return new GroupMessageAdapter.ViewHolder(view);
        }else{

            View view = LayoutInflater.from(mContext).inflate(R.layout.chatgroup_item_left,parent,false);
            return new GroupMessageAdapter.ViewHolder(view);

        }


    }
@Override
public void onBindViewHolder(@NonNull GroupMessageAdapter.ViewHolder holder, int position) {



    Chat chat= mChat.get(position);
    String type = mChat.get(position).getType();
    if(type.equals("texto")){
        holder.show_message.setVisibility(View.VISIBLE);
        holder.show_messageImage.setVisibility(View.GONE);
        holder.url_pdf.setVisibility(View.GONE);
        holder.Location.setVisibility(View.GONE);
        holder.show_message.setText(chat.getMessage());
    }
    if(type.equals("imagen")){
        holder.show_message.setVisibility(View.GONE);
        holder.url_pdf.setVisibility(View.GONE);
        holder.Location.setVisibility(View.GONE);
        holder.show_messageImage.setVisibility(View.VISIBLE);
        Glide.with(context).load(chat.getMessage()).into(holder.show_messageImage);
    }
    if(type.equals("archivo")){
        holder.show_message.setVisibility(View.GONE);
        holder.show_messageImage.setVisibility(View.GONE);
        holder.Location.setVisibility(View.GONE);
        holder.url_pdf.setVisibility(View.VISIBLE);
        Glide.with(context).load(chat.getMessage()).into(holder.url_pdf);

    }
    if(type.equals("Ubicación")){
        holder.show_message.setVisibility(View.GONE);
        holder.show_messageImage.setVisibility(View.GONE);
        holder.Location.setVisibility(View.VISIBLE);
        holder.url_pdf.setVisibility(View.GONE);
        Glide.with(context).load(chat.getMessage()).into(holder.url_pdf);

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

    SetUsername(chat, holder);


        }

@Override
public int getItemCount() {
        return mChat.size();
        }

public class ViewHolder extends  RecyclerView.ViewHolder{
    public TextView show_message;
    public TextView username;
    public ImageView show_messageImage;
    public ImageView url_pdf;
    public ImageView Location;
    public  ViewHolder(View view){
        super(view);

        show_message = view.findViewById(R.id.show_message);
        show_messageImage = view.findViewById(R.id.messageIv);
        url_pdf = view.findViewById(R.id.messageFile);
        Location = view.findViewById(R.id.messageLocation);
        username = view.findViewById(R.id.user_show_message);

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


    private void SetUsername(Chat model, GroupMessageAdapter.ViewHolder holder){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String usuario;
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String id = dataSnapshot.child("id").getValue().toString();
                     usuario = dataSnapshot.child("usuario").getValue().toString();
                        if(model.getSender().equals(id)){
                             holder.username.setText(usuario);

                            }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
