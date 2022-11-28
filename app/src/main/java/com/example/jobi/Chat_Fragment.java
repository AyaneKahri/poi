package com.example.jobi;


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

import com.example.jobi.Adapter.UserAdapter;
import com.example.jobi.Model.Chatlist;
import com.example.jobi.Model.User;
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


public class Chat_Fragment extends Fragment {


    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;
    EditText Buscar;
    FirebaseUser fuser;
    DatabaseReference reference;


    private List<Chatlist> usersList;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();



        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    Chatlist chatlist = snapshot1.getValue(Chatlist.class);
                    usersList.add(chatlist);

                }



                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Buscar = view.findViewById(R.id.txt_search);
        Buscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                BuscarUsuarios(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return  view;

    }

    private void BuscarUsuarios(String s) {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("usuario")
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String usuario = snapshot.child("usuario").getValue().toString();
                    String contraseña = snapshot.child("contraseña").getValue().toString();
                    String correo = snapshot.child("correo").getValue().toString();
                    String foto = snapshot.child("ImageUrl").getValue().toString();
                    String di = snapshot.child("id").getValue().toString();
                    String online = snapshot.child("status").getValue().toString();
                    User user = new User(di, usuario, contraseña, correo, foto,online);

                    assert user != null;
                    for( Chatlist chatlist : usersList){
                        if (user.getId().equals(chatlist.getId())){
                            mUsers.add(user);


                        }
                    }
                }

                userAdapter = new UserAdapter(getContext(),mUsers,false);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void chatList(){
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("usuario").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (Buscar.getText().toString().equals("")) {
                    mUsers.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                        String usuario = snapshot1.child("usuario").getValue().toString();
                        String contraseña = snapshot1.child("contraseña").getValue().toString();
                        String correo = snapshot1.child("correo").getValue().toString();
                        String foto = snapshot1.child("ImageUrl").getValue().toString();
                        String di = snapshot1.child("id").getValue().toString();
                        String online = snapshot1.child("status").getValue().toString();
                        User user = new User(di, usuario, contraseña, correo, foto,online);


                        for (Chatlist chatlist : usersList) {
                            if (user.getId().equals(chatlist.getId())) {
                                mUsers.add(user);


                            }
                        }
                    }
                }
                userAdapter = new UserAdapter(getContext(), mUsers,false);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}