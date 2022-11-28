package com.example.jobi;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.jobi.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.UnsupportedEncodingException;
import java.lang.ref.Reference;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile_Fragment extends Fragment {
    CircleImageView profile_image;
    TextView username;
    TextView mail;
    Button Btn_CerrarSesion;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    DatabaseReference mDatabase;
    StorageReference storageReference;
    FirebaseAuth mAuth; //Declaramos la instancia de FirebaseAuth
    private Context mContext;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile,container,false);
        profile_image  = view.findViewById(R.id.PerfilFoto);
        username = view.findViewById(R.id.txt_perfilusuario);
        mail = view.findViewById(R.id.txt_perfilmail);
        Btn_CerrarSesion = view.findViewById(R.id.btn_perfilcerrar);
        storageReference = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance(); //Inicializamos FirebaseAuth
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                String usuario = dataSnapshot.child("usuario").getValue().toString();
                String contraseña = dataSnapshot.child("contraseña").getValue().toString();
                String correo = dataSnapshot.child("correo").getValue().toString();
                String foto = dataSnapshot.child("ImageUrl").getValue().toString();
                writeNewUser(firebaseUser.getUid(),usuario,contraseña,correo,foto);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Btn_CerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CerrarSesion();
            }
        });
        return view;


    }

    //Método para el botón Iniciar Sesión
    public void CerrarSesion(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.getInstance().signOut();
        Intent main = new Intent(getActivity(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(main);


    }
    public void writeNewUser(String userId, String name, String password,String email,String imagen) {
        User user = new User(userId,name,password, email,imagen);
        if (getActivity() == null) {
            return;
        }else{
            username.setText(user.getUsername());
            mail.setText(user.getEmail());
            Glide.with(getContext()).load(user.getImagenURL()).into(profile_image);
        }






    }




}
