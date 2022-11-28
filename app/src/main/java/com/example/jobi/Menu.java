package com.example.jobi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.jobi.Model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Menu extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    DatabaseReference mDatabase;
    StorageReference storageReference;

    private FirebaseAuth mAuth; //Declaramos la instancia de FirebaseAuth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profile_image =findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        storageReference = FirebaseStorage.getInstance().getReference();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
        status("online");
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new Chat_Fragment()).commit();
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.nav_logout:
                mAuth.getInstance().signOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener(){
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.nav_chat:
                            selectedFragment = new Chat_Fragment();
                            break;
                        case R.id.nav_contact:
                            selectedFragment = new Contactos_Fragment();
                            break;
                        case R.id.nav_group:
                            selectedFragment = new Grupos_Fragment();
                            break;
                        case R.id.nav_profile:
                            selectedFragment = new Profile_Fragment();
                            break;

                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();

                    return true;
                }
            };

    public void writeNewUser(String userId, String name, String password,String email,String imagen) {
        User user = new User(userId,name,password, email,imagen);

        username.setText(user.getUsername());
        if (user.getImagenURL().equals("default")){
            profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {

            //change this
            Glide.with(getApplicationContext()).load(user.getImagenURL()).into(profile_image);
        }

    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }


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