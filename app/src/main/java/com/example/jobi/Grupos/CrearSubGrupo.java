package com.example.jobi.Grupos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jobi.CrearGrupo;
import com.example.jobi.Menu;
import com.example.jobi.MessageActivity;
import com.example.jobi.Model.User;
import com.example.jobi.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CrearSubGrupo extends AppCompatActivity {
    String group_id;
    //Navegador
    CircleImageView group_image;
    TextView group_name;
    DatabaseReference reference;
    DatabaseReference reference2;

    //SUBIR IMAGEN

    private static final int IMAGE_REQUEST = 1;
    String mUri;
    ImageView ivFoto;
    Button btnSeleccionarImagen;
    Uri imagenUri;
    StorageTask uploadTask;
    private ProgressDialog pd = null;

    //FIREBASE
    private FirebaseAuth mAuth;
    //Donde guardo la foto
   private StorageReference storageReference = FirebaseStorage.getInstance().getReference("SubGroupImages");

    //REGISTRO
    Button btnRegistrar;
    TextView txt_groupname;
    String ID_GRUPO;
    Button btn_return;


    //VARIABLES PARA VALIDAR
    private String nombre = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_sub_grupo);
        //TRAEMOS EL ID DEL GRUPO
        SharedPreferences prefs = getSharedPreferences("Preferences", 0);
        group_id = prefs.getString("groupid", "");

        //NAVEGADOR
        group_image =findViewById(R.id.profile_image);
        group_name = findViewById(R.id.username);
        //MOSTRAMOS LOS DATOS EN EL NAVEGADOR
        reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.orderByChild("groupId").equalTo(group_id).addValueEventListener(new ValueEventListener() {
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
        //ID DEL USUARIO
        mAuth = FirebaseAuth.getInstance(); //Inicializamos FirebaseAuth
        ivFoto = findViewById(R.id.GroupFoto);
        //REGISTRO
        btnSeleccionarImagen = findViewById(R.id.btn_examinarsubgroup);
        btnRegistrar = findViewById(R.id.btnRegistrarsubgroup);
        txt_groupname = findViewById(R.id.txt_subgroupname);
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nombre = txt_groupname.getText().toString();

               // String g_timestamp = ""+ System.currentTimeMillis();

                //Si los textbox NO estan vacios
                if(!nombre.isEmpty()&&imagenUri!=null){
                    uploadToFirebase();
                    //Registrar(""+g_timestamp);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Favor de llenar los campos",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //CUANDO CLICKEO A BTN_EXAMINAR

        btnSeleccionarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seleccionarImagen();
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



    }
    private void Registrar(String g_timestamp){
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("groupId",group_id);
        hashMap.put("Id",g_timestamp);
        hashMap.put("Nombre",nombre);
        hashMap.put("creador",mAuth.getUid());
        if(mUri==null){
            hashMap.put("ImageUrl","default");
        }else{
            hashMap.put("ImageUrl",mUri);
        }


        ID_GRUPO = g_timestamp;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups").child(group_id).child("SubGroups");
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Groups").child(group_id).child("SubGroups").child(g_timestamp).child("Participantes");

        HashMap<String, String> hashMap2 = new HashMap<>();

        hashMap2.put("role","creador");
        hashMap2.put("id",mAuth.getUid());

        reference.child(g_timestamp).setValue(hashMap);
        //Añadimos al creador
        reference2.child(mAuth.getUid()).setValue(hashMap2);

        Toast.makeText(CrearSubGrupo.this, "Se ha agregado el sub-grupo correctamente", Toast.LENGTH_SHORT).show();

       Intent principal = new Intent(getApplicationContext(), MenuGroup.class);
        principal.putExtra("groupId",group_id);
        startActivity(principal);

    }

    public void seleccionarImagen() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);

    }

    private void uploadToFirebase (){
        //PROGRESS DIALOG
        this.pd = ProgressDialog.show(this, "Jobi",
                "Creando sub-grupo...", true, false);
        if (imagenUri != null){

            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imagenUri));

            uploadTask = fileReference.putFile(imagenUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        mUri = downloadUri.toString();
                        String g_timestamp = ""+ System.currentTimeMillis();
                        Registrar(""+g_timestamp);
                        pd.dismiss();
                    }else {
                        pd.dismiss();
                        Toast.makeText(CrearSubGrupo.this,"Failed!",Toast.LENGTH_SHORT).show();

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CrearSubGrupo.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            pd.dismiss();
            Toast.makeText(CrearSubGrupo.this,"No seleccionaste imagen.",Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri mUri){

        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imagenUri = data.getData();
            ivFoto.setImageURI(imagenUri);

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(CrearSubGrupo.this,"Subida en curso.", Toast.LENGTH_SHORT).show();
            }else {
               // uploadToFirebase();
            }
        }

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