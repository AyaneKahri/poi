package com.example.jobi.SubGrupos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.jobi.Chat_Fragment;
import com.example.jobi.Contactos_Fragment;
import com.example.jobi.Grupos.GroupAddParticipant;
import com.example.jobi.Grupos.MenuGroup;
import com.example.jobi.Grupos_Fragment;
import com.example.jobi.Profile_Fragment;
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

public class MenuSubGroup extends AppCompatActivity {
    CircleImageView subgroup_image;
    TextView subgroup_name;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    DatabaseReference mDatabase;
    StorageReference storageReference;

    String group_id;
    String subgroup_id;


    Button btn_return;
    private FirebaseAuth mAuth; //Declaramos la instancia de FirebaseAuth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_sub_group);

        //Agarra el ID del grupo y subgrupo
        Intent intent = getIntent();
        group_id = intent.getStringExtra("groupId");
        subgroup_id = intent.getStringExtra("SubgroupId");

        //NAVEGADOR
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


        subgroup_image =findViewById(R.id.profile_image);
        subgroup_name = findViewById(R.id.username);
        storageReference = FirebaseStorage.getInstance().getReference();

        reference = FirebaseDatabase.getInstance().getReference("Groups").child(group_id).child("SubGroups");

        reference.orderByChild("Id").equalTo(subgroup_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String nombre = dataSnapshot.child("Nombre").getValue().toString();
                    String foto = dataSnapshot.child("ImageUrl").getValue().toString();

                    subgroup_name.setText(nombre);
                    Glide.with(getApplicationContext()).load(foto).into(subgroup_image);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        //BOTÓN DE REGRESAR
        btn_return = findViewById(R.id.btn_return);
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MenuGroup.class);
                intent.putExtra("groupId",group_id);

                startActivity(intent);
            }
        });



        //Manda el ID del grupo y sub grupo
        SharedPreferences prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("groupid", group_id);
        editor.putString("SubgroupId", subgroup_id);
        editor.commit();











        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ChatSubGroup_Fragment()).commit();
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
                            selectedFragment = new ChatSubGroup_Fragment();
                            break;
                        case R.id.nav_info:
                            selectedFragment = new InfoSubGrupos_Fragment();
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