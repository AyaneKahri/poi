package com.example.jobi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
//import androidx.core.content.FileProvider;
import com.example.jobi.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;



import java.util.HashMap;



public class Registro extends AppCompatActivity {

    //TEXT BOXS DE LA PANTALLA
    private EditText usuario;
    private EditText correo;
    private EditText contraseña;

    //BOTON_EXAMINAR-----------
    ImageView ivFoto;
    Button btnSeleccionarImagen;
    Button btnRegistrar;

    //VARIABLES PARA VALIDAR
    private String username = "";
    private String mail = "";
    private String password = "";


    //REGISTRO DE USUARIOS CON FIREBASE
    FirebaseAuth mAuth; //Declaramos la instancia de FirebaseAuth
    DatabaseReference mDatabase;
    DatabaseReference imgref;
    String mUri;
    StorageReference storageReference;
    ProgressDialog cargando;


    //VARIABLES PARA LA FOTO

    private StorageReference reference = FirebaseStorage.getInstance().getReference();
    Uri imagenUri;
    StorageTask uploadTask;
    private static final int IMAGE_REQUEST = 1;
    private ProgressDialog pd = null;

//-----------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance(); //Inicializamos FirebaseAuth


        //VARIABLES DE REGISTRO
        //ID de los textboxs
        usuario=findViewById(R.id.txt_username);
        correo = findViewById(R.id.txt_correo);
        contraseña = findViewById(R.id.txt_contraseña);
        //ID de la foto
        ivFoto = findViewById(R.id.PerfilFoto);
        //ID del botón examinar...
        btnSeleccionarImagen = findViewById(R.id.btn_examinar);
        //ID del botón registrar
        btnRegistrar = findViewById(R.id.btn_login);

        imgref = FirebaseDatabase.getInstance().getReference().child("Fotos");
        storageReference = FirebaseStorage.getInstance().getReference();
        cargando = new ProgressDialog(this);



        //CUANDO CLICKEO A BTN_REGISTRAR
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {

        username = usuario.getText().toString();
        mail     = correo.getText().toString();
        password = contraseña.getText().toString();

       //Si los textbox NO estan vacios
        if(!username.isEmpty() && !mail.isEmpty() && !password.isEmpty() &&imagenUri!=null){

           //Checa si el password tiene mínimo 6 caracteres
           if(password.length() >= 6){

               //FUNCION PARA REGISTRAR
               uploadToFirebase();
           }
           else{
               Toast.makeText(getApplicationContext(), "La contraseña debe de tener más de 6 caracteres",Toast.LENGTH_SHORT).show();
           }

        }
        else{
            Toast.makeText(getApplicationContext(), "Favor de llenar los campos",Toast.LENGTH_SHORT).show();
        }


    }
    });

        if(ContextCompat.checkSelfPermission(Registro.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Registro.this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        //CUANDO CLICKEO A BTN_EXAMINAR

        btnSeleccionarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seleccionarImagen();
            }
        });
    }


    //VERIFICAMOS SI EL USUARIO YA INICIO SESIÓN
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Toast.makeText(getApplicationContext(), "Ya has iniciado sesión.", Toast.LENGTH_SHORT).show();
            Intent principal = new Intent(getApplicationContext(), Menu.class);
            startActivity(principal);
            finish(); //Evita que vaya  la pantalla de Registro
        }
    }

    //MÉTODO PARA REGISTRAR USUARIOS

    public void RegistrarUsuario(){


        mAuth.createUserWithEmailAndPassword(mail.trim(), password.trim())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            // If sign in fails, display a message to the user.
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            assert firebaseUser != null;
                            String userid = firebaseUser.getUid();
                            mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                            HashMap<String, String> map = new HashMap<>();
                            map.put("usuario", username);
                            map.put("correo", mail);
                            map.put("contraseña", password);
                            map.put("status", "offline");
                            if(mUri==null){
                                map.put("ImageUrl","default");
                            }else{
                                map.put("ImageUrl",mUri);
                            }

                            map.put("id", userid);

                            uploadToFirebase();

                            mDatabase.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task2) {
                                    if(task2.isSuccessful()){
                                        pd.dismiss();

                                        Toast.makeText(getApplicationContext(), "Te has registrado Correctamente",Toast.LENGTH_SHORT).show();

                                        Intent principal = new Intent(getApplicationContext(), Login.class);
                                        startActivity(principal);
                                        finish(); //Evita que vaya  la pantalla de Registro


                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(), "No c pudo",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            pd.dismiss();
                            Toast.makeText(getApplicationContext(), "Favor de poner los datos correctamente",Toast.LENGTH_SHORT).show();
                            // updateUI(null);     // If sign in fails, display a message to the user.

                        }
                    }
                });
    }

    public void seleccionarImagen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imagenUri = data.getData();
            ivFoto.setImageURI(imagenUri);

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(Registro.this,"Subida en curso.", Toast.LENGTH_SHORT).show();
            }else {
               // uploadToFirebase();
            }
        }

    }



    private void uploadToFirebase (){
        this.pd = ProgressDialog.show(this, "Jobi",
                "Registrando usuario...", true, false);
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
                        RegistrarUsuario();

                    }else {
                        Toast.makeText(Registro.this,"Failed!",Toast.LENGTH_SHORT).show();

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Registro.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            Toast.makeText(Registro.this,"No seleccionaste imagen.",Toast.LENGTH_SHORT).show();
        }
    }
    private String getFileExtension(Uri mUri){

        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));

    }


}

