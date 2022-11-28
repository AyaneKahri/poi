package com.example.jobi.Grupos;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobi.Adapter.GroupAdapter;
import com.example.jobi.Adapter.SubGroupAdapter;
import com.example.jobi.CrearGrupo;
import com.example.jobi.MessageActivity;
import com.example.jobi.Model.Group;
import com.example.jobi.Model.SubGroup;
import com.example.jobi.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class SubGrupos_Fragment extends Fragment {
    FloatingActionButton Btn_CrearGrupo;
    private RecyclerView recyclerView;
    FirebaseUser mAuth;
    private com.example.jobi.Adapter.SubGroupAdapter SubGroupAdapter;
    String groupid;
    ArrayList<SubGroup> mGroup;
    EditText Buscar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups,container,false);
        Btn_CrearGrupo = view.findViewById(R.id.btn_creargpo);
        mAuth = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = view.findViewById(R.id.recycler_viewG);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        Btn_CrearGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CrearGrupo();
            }
        });

        MostrarGrupos();
        Buscar = view.findViewById(R.id.txt_search);
        Buscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                BuscarGpos(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return view;

    }

    //Método para el botón Crear Grupo
    public void CrearGrupo(){
        //Agarramos el ID del GRUPO
        SharedPreferences prefs = getActivity().getSharedPreferences("Preferences", 0);
        groupid = prefs.getString("groupid", "");

        Intent main = new Intent(getActivity(), CrearSubGrupo.class);
        startActivity(main);
    }
    private void BuscarGpos(String s) {
        //Agarramos el ID del GRUPO
        SharedPreferences prefs = getActivity().getSharedPreferences("Preferences", 0);
        groupid = prefs.getString("groupid", "");

        Query query = FirebaseDatabase.getInstance().getReference("Groups").child(groupid).child("SubGroups").orderByChild("Nombre")
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mGroup.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.child("Participantes").child(mAuth.getUid()).exists()){
                        String nombre = snapshot.child("Nombre").getValue().toString();
                        String id = snapshot.child("Id").getValue().toString();
                        String groupId = snapshot.child("groupId").getValue().toString();
                        String foto = snapshot.child("ImageUrl").getValue().toString();
                        SubGroup group = new SubGroup(id,groupId,nombre,foto);
                        mGroup.add(group);
                    }
                }

                SubGroupAdapter = new SubGroupAdapter(getContext(),mGroup);
                recyclerView.setAdapter(SubGroupAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void MostrarGrupos(){
        //Agarramos el ID del GRUPO
        SharedPreferences prefs = getActivity().getSharedPreferences("Preferences", 0);
        groupid = prefs.getString("groupid", "");
        mGroup = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups").child(groupid).child("SubGroups");

        reference.orderByChild("Nombre").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (Buscar.getText().toString().equals("")) {
                mGroup.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        if (snapshot.child("Participantes").child(mAuth.getUid()).exists()) {
                            String nombre = snapshot.child("Nombre").getValue().toString();
                            String id = snapshot.child("Id").getValue().toString();
                            String groupId = snapshot.child("groupId").getValue().toString();
                            String foto = snapshot.child("ImageUrl").getValue().toString();
                            SubGroup group = new SubGroup(id, groupId, nombre, foto);
                            mGroup.add(group);
                        }
                    }
                }
                SubGroupAdapter = new SubGroupAdapter(getContext(),mGroup);
                recyclerView.setAdapter(SubGroupAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}
