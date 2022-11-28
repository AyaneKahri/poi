package com.example.jobi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jobi.Grupos.MenuGroup;
import com.example.jobi.Model.User;
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

public class CrearGrupo extends AppCompatActivity {

    //Navegador
    CircleImageView profile_image;
    TextView username;
    StorageReference storageReference;
    FirebaseUser firebaseUser;
    DatabaseReference ref;
    DatabaseReference mDatabase;
    Button btn_return;
    //SUBIR IMAGEN

    private static final int IMAGE_REQUEST = 1;
    String mUri;
    ImageView ivFoto;
    Button btnSeleccionarImagen;
    Uri imagenUri;
    StorageTask uploadTask;
    DatabaseReference reference2;
    //FIREBASE
    private FirebaseAuth mAuth;
    //Donde guardo la foto
    private StorageReference reference = FirebaseStorage.getInstance().getReference("GroupImages");

    //REGISTRO
    Button btnRegistrar;
    TextView txt_groupname;
    String ID_GRUPO;

    //VARIABLES PARA VALIDAR
    private String nombre = "";
    private ProgressDialog pd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_grupo);

        //NAVEGADOR---------------------------------
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profile_image =findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        storageReference = FirebaseStorage.getInstance().getReference();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        //MOSTRAMOS LOS DATOS EN EL NAVEGADOR
        ref.addValueEventListener(new ValueEventListener() {
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

        //FIREBASE--------------------

        mAuth = FirebaseAuth.getInstance(); //Inicializamos FirebaseAuth
        ivFoto = findViewById(R.id.GroupFoto);
        btnSeleccionarImagen = findViewById(R.id.btn_examinargroup);
        btnRegistrar = findViewById(R.id.btnRegistrargroup);
        txt_groupname = findViewById(R.id.txt_groupname);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nombre = txt_groupname.getText().toString();



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
                Intent intent = new Intent(getApplicationContext(), Menu.class);

                startActivity(intent);
            }
        });

    }


    private void Registrar(String g_timestamp){

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("groupId",g_timestamp);
        hashMap.put("Nombre",nombre);
        hashMap.put("creador",mAuth.getUid());
        if(mUri==null){
            hashMap.put("ImageUrl","default");
        }else{
            hashMap.put("ImageUrl",mUri);
        }



        ID_GRUPO = g_timestamp;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Groups").child(g_timestamp).child("Participantes");

        HashMap<String, String> hashMap2 = new HashMap<>();

        hashMap2.put("role","creador");
        hashMap2.put("id",mAuth.getUid());

        reference.child(g_timestamp).setValue(hashMap);
        //Añadimos al creador
        reference2.child(mAuth.getUid()).setValue(hashMap2);
        Toast.makeText(CrearGrupo.this, "Se ha agregado el grupo correctamente", Toast.LENGTH_SHORT).show();

        Intent principal = new Intent(getApplicationContext(), Menu.class);
        startActivity(principal);

    }

    public void seleccionarImagen() {

     Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);

    }

    private void uploadToFirebase (){
        this.pd = ProgressDialog.show(this, "Jobi",
                "Creando grupo...", true, false);
        if (imagenUri != null){
            final StorageReference fileReference = reference.child(System.currentTimeMillis()
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
                        Toast.makeText(CrearGrupo.this,"Failed!",Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CrearGrupo.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            Toast.makeText(CrearGrupo.this,"No seleccionaste imagen.",Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }
    }

    private String getFileExtension(Uri mUri){

        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imagenUri = data.getData();
            ivFoto.setImageURI(imagenUri);

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(CrearGrupo.this,"Subida en curso.", Toast.LENGTH_SHORT).show();
            }else {
                //uploadToFirebase();
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