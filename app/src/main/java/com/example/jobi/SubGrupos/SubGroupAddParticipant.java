package com.example.jobi.SubGrupos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.jobi.Adapter.AddParticipantsAdapter;
import com.example.jobi.Adapter.AddParticipantsSubGroupsAdapter;
import com.example.jobi.Grupos.GroupAddParticipant;
import com.example.jobi.Grupos.MenuGroup;
import com.example.jobi.Model.User;
import com.example.jobi.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SubGroupAddParticipant extends AppCompatActivity {

    private AddParticipantsSubGroupsAdapter participantsAdapter;
    private List<User> mUsers;
    RecyclerView recyclerView;
    FirebaseAuth mAuth;
    String group_id;
    String sub_group_Id;
    String role = "creador";
    //Navegador
    CircleImageView group_image;
    TextView group_name;
    DatabaseReference reference;
    Button btn_return;
    DatabaseReference reference2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_add_participant);
        //TRAEMOS EL ID DEL GRUPO
        SharedPreferences prefs = getSharedPreferences("Preferences", 0);
        group_id = prefs.getString("groupid", "");
        //Agarramos el ID del subGrupo
        sub_group_Id = prefs.getString("SubgroupId", "");

        //NAVEGADOR
        group_image =findViewById(R.id.profile_image);
        group_name = findViewById(R.id.username);

        //MOSTRAMOS LOS DATOS EN EL NAVEGADOR
        reference = FirebaseDatabase.getInstance().getReference("Groups").child(group_id).child("SubGroups");;
        reference.orderByChild("Id").equalTo(sub_group_Id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    String nombre = dataSnapshot.child("Nombre").getValue().toString();
                    String foto = dataSnapshot.child("ImageUrl").getValue().toString();

                    group_name.setText(nombre);
                    Glide.with(getApplicationContext()).load(foto).into(group_image);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.recycler_viewadd);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(SubGroupAddParticipant.this));
        group_id = getIntent().getStringExtra("groupId");
        MostrarParticipantesGpo();

        //BOTÓN DE REGRESAR
        btn_return = findViewById(R.id.btn_return);
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MenuSubGroup.class);
                intent.putExtra("groupId",group_id);
                 intent.putExtra("SubgroupId",sub_group_Id);
                startActivity(intent);
            }
        });
    }


    private void MostrarParticipantesGpo(){
        mUsers = new ArrayList<>();
        mUsers.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(group_id).child("Participantes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();

                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    //Agarramos el ID de los participantes
                    String idP = dataSnapshot.child("id").getValue().toString();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                    reference.orderByChild("id").equalTo(idP).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot dataSnapshot: snapshot.getChildren()) {
                                String usuario = dataSnapshot.child("usuario").getValue().toString();
                                String id = dataSnapshot.child("id").getValue().toString();
                                String contraseña = dataSnapshot.child("contraseña").getValue().toString();
                                String correo = dataSnapshot.child("correo").getValue().toString();
                                String foto = dataSnapshot.child("ImageUrl").getValue().toString();
                                User user = new User(id,usuario,contraseña, correo,foto);
                                if(!mAuth.getUid().equals(user.getId())){
                                    mUsers.add(user);
                                }
                            }
                            participantsAdapter= new AddParticipantsSubGroupsAdapter(SubGroupAddParticipant.this,mUsers,group_id,sub_group_Id,role);
                            recyclerView.setAdapter(participantsAdapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });





                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void status(String status){
        reference2 = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference2.updateChildren(hashMap);
    }
    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
    @Override
    protected void onResume() {

        super.onResume();

        status("online");


    }
}