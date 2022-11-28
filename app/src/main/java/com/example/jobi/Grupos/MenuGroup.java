package com.example.jobi.Grupos;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.jobi.Adapter.AddParticipantsAdapter;
import com.example.jobi.Chat_Fragment;
import com.example.jobi.Menu;
import com.example.jobi.Model.User;
import com.example.jobi.R;
import com.example.jobi.Tarea_Fragment;
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

public class MenuGroup extends AppCompatActivity {

     String group_id;
    FirebaseUser firebaseUser;
    Button btn_return;
    CircleImageView group_image;
    TextView group_name;
    StorageReference storageReference;

    DatabaseReference reference;

    private FirebaseAuth mAuth; //Declaramos la instancia de FirebaseAuth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menugroup);
        //Agarra el ID del grupo
        Intent intent = getIntent();
        group_id = intent.getStringExtra("groupId");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //ToolBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        //BOTÓN DE REGRESAR
        btn_return = findViewById(R.id.btn_return);
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Menu.class);
                startActivity(intent);
            }
        });

        group_image =findViewById(R.id.profile_image);
        group_name = findViewById(R.id.username);
        storageReference = FirebaseStorage.getInstance().getReference();

        reference = FirebaseDatabase.getInstance().getReference("Groups");

        reference.orderByChild("groupId").equalTo(group_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
             for(DataSnapshot dataSnapshot: snapshot.getChildren()){
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

        SharedPreferences prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("groupid", group_id);
        editor.commit();


        //BottomNavigation Menu
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ChatGroup_Fragment()).commit();
        }

    }

    public boolean OnCreateOptionsMenu(MenuGroup menu){
        getMenuInflater().inflate(R.menu.navegador, (android.view.Menu) menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.nav_addparticipant:
              Intent intent = new Intent(this, GroupAddParticipant.class);
              intent.putExtra("groupId",group_id);
              startActivity(intent);
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
                            selectedFragment = new ChatGroup_Fragment();
                            break;

                        case R.id.nav_group:
                            selectedFragment = new SubGrupos_Fragment();
                            break;


                        case R.id.nav_info:
                            selectedFragment = new InfoGrupos_Fragment();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();

                    return true;
                }
            };

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
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
