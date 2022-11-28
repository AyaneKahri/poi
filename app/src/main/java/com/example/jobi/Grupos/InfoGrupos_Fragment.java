package com.example.jobi.Grupos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.jobi.Adapter.UserAdapter;
import com.example.jobi.MainActivity;
import com.example.jobi.Model.User;
import com.example.jobi.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InfoGrupos_Fragment extends Fragment {

    private UserAdapter participantsAdapter;
    private List<User> mUsers;
    RecyclerView recyclerView;
    FirebaseAuth mAuth;
    String group_id;
    String sub_group_Id;
    FloatingActionButton btn_add;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_infogroup,container,false);

        //Agarramos el ID del GRUPO
        SharedPreferences prefs = getActivity().getSharedPreferences("Preferences", 0);
        group_id = prefs.getString("groupid", "");
        //Agarramos el ID del subGrupo
        sub_group_Id = prefs.getString("SubgroupId", "");



        mAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        MostrarParticipantesGpo();


        //BOTÓN DE AÑADIR PARTICIPANTE
        btn_add = view.findViewById(R.id.btn_add);

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AñadirParticipantes();
            }
        });

        return view;

    }
    //Método para el botón Añadir participantes
    public void AñadirParticipantes(){
        Intent intent = new Intent(getContext(), GroupAddParticipant.class);
        intent.putExtra("groupId",group_id);
        startActivity(intent);


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
                                String online = dataSnapshot.child("status").getValue().toString();
                                User user = new User(id,usuario,contraseña, correo,foto,"");
                                if(!mAuth.getUid().equals(user.getId())){
                                    mUsers.add(user);
                                }
                            }
                            participantsAdapter= new UserAdapter(getContext(),mUsers,false);
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
}
