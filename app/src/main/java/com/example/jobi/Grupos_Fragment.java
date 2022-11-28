package com.example.jobi;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobi.Adapter.GroupAdapter;
import com.example.jobi.Adapter.UserAdapter;
import com.example.jobi.Model.Group;
import com.example.jobi.Model.User;
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
import java.util.List;


public class Grupos_Fragment extends Fragment {
    FloatingActionButton Btn_CrearGrupo;
    private RecyclerView recyclerView;
    FirebaseUser mAuth;
    private GroupAdapter groupAdapter;
    EditText Buscar;
    ArrayList<Group> mGroup;


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

    //Método para el botón Creat Grupo
    public void CrearGrupo(){
        Intent main = new Intent(getActivity(), CrearGrupo.class);
        startActivity(main);
    }
    private void BuscarGpos(String s) {
        mGroup.clear();

        Query query = FirebaseDatabase.getInstance().getReference("Groups").orderByChild("Nombre")
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mGroup.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.child("Participantes").child(mAuth.getUid()).exists()){
                        String nombre = snapshot.child("Nombre").getValue().toString();
                        String id = snapshot.child("groupId").getValue().toString();
                        String foto = snapshot.child("ImageUrl").getValue().toString();
                        Group group = new Group(id,nombre,foto);
                        mGroup.add(group);
                    }
                }

                groupAdapter = new GroupAdapter(getContext(),mGroup);
                recyclerView.setAdapter(groupAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void MostrarGrupos(){

        mGroup = new ArrayList<>();
        mGroup.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");

        reference.orderByChild("Nombre").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (Buscar.getText().toString().equals("")) {
                    mGroup.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        if (snapshot.child("Participantes").child(mAuth.getUid()).exists()) {
                            String nombre = snapshot.child("Nombre").getValue().toString();
                            String id = snapshot.child("groupId").getValue().toString();
                            String foto = snapshot.child("ImageUrl").getValue().toString();
                            Group group = new Group(id, nombre, foto);
                            mGroup.add(group);
                        }

                    }
                }
                groupAdapter = new GroupAdapter(getContext(),mGroup);
                recyclerView.setAdapter(groupAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

}
