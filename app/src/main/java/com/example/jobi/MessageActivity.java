package com.example.jobi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.jobi.Adapter.MessageAdapter;
import com.example.jobi.Model.Chat;
import com.example.jobi.Model.User;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {
    //NAV BAR
    CircleImageView profile_image;
    TextView username;
    ImageView onlineT;
    //ID DE LOS USUARIOS
    FirebaseUser fuser;
    DatabaseReference reference;
    String userid;

    //BOTONES DE LA BARRA DE MENSAJES
    ImageButton attachBtn;
    ImageButton btn_send;
    EditText text_send;

    //VARIABLES PARA LA FOTO
    String mUri;
    private StorageReference Sreference = FirebaseStorage.getInstance().getReference("ChatImages");
    private StorageReference Freference = FirebaseStorage.getInstance().getReference("ChatFiles");
    Uri imagenUri;
    StorageTask uploadTask;
    private static final int IMAGE_REQUEST = 1;
    private ProgressDialog pd = null;

    //LISTAS
    MessageAdapter messageAdapter;
    List<Chat> mchat;
    RecyclerView recyclerView;
    //UBICACION
    private LocationRequest locationRequest;
    String latitud = null;
    String longitud = null;

    Intent intent;
//Listener para el "visto"
    ValueEventListener seenListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        //BARRA DE NAVEGACIÓN
        Toolbar toolbar = findViewById(R.id.tool_chat);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //LISTA DE LOS MENSAJES
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //DATOS DEL USUARIO
        profile_image = findViewById(R.id.Profile_chat);
        username = findViewById(R.id.username);
        onlineT = findViewById(R.id.txtlinea);
        //BOTONES
        attachBtn = findViewById(R.id.attachBtn);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);

        intent = getIntent();
        userid = intent.getStringExtra("id");
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        //BOTÓN DE ENVIAR ARCHIVOS
        attachBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public  void onClick(View view){
                SeleccionarTipo();
            }
        });


        //BOTÓN DE ENVIAR MENSAJE
        btn_send.setOnClickListener(new View.OnClickListener(){

            @Override
            public  void onClick(View view){

                String msg = text_send.getText().toString();
                if(!msg.equals("")){

                    sendMesage(fuser.getUid(), userid,msg);

                }else{
                    Toast.makeText(MessageActivity.this, "No puedes enviar mensajes vacios ", Toast.LENGTH_SHORT).show();
                }

                text_send.setText("");
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String usuario = snapshot.child("usuario").getValue().toString();
                String id = snapshot.child("id").getValue().toString();
                String contraseña = snapshot.child("contraseña").getValue().toString();
                String correo = snapshot.child("correo").getValue().toString();
                String foto = snapshot.child("ImageUrl").getValue().toString();
                String online = snapshot.child("status").getValue().toString();
                User user = new User(id,usuario,contraseña, correo,foto,online);
                username.setText(user.getUsername());
                if ( MessageActivity.this == null) {
                    return;
                }else {
                    Glide.with(getApplicationContext()).load(user.getImagenURL()).into(profile_image);
                }
                if(user.getStatus().equals("online")){
                    onlineT.setVisibility(View.VISIBLE);
                }else{
                    onlineT.setVisibility(View.GONE);
                }



                readMessages(fuser.getUid(),userid, user.getImagenURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        seenMessage(userid);

        //OBTENER UBICACION

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

    }
    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                    String recividor = snapshot.child("reciver").getValue().toString();
                    String enviador = snapshot.child("sender").getValue().toString();
                    if (recividor.equals(fuser.getUid()) && enviador.equals(userid)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
//Mensajes de texto----------------
    private void sendMesage(String sender, String reciver,  String message){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new  HashMap<>();
        hashMap.put("sender" , sender);
        hashMap.put("reciver" , reciver);
        hashMap.put("message" , message);
        hashMap.put("type" , "texto");
        hashMap.put("isseen", false);
        reference.child("Chats").push().setValue(hashMap);


// aniadir usuario a chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);


        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userid)
                .child(fuser.getUid());
        chatRefReceiver.child("id").setValue(fuser.getUid());




    }

    private void readMessages (String myid, String userid, String imageurl){
        mchat = new ArrayList<>();



        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mchat.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){

                    String men = snapshot1.child("message").getValue().toString();
                    String recividor = snapshot1.child("reciver").getValue().toString();
                    String enviador = snapshot1.child("sender").getValue().toString();
                    String type = snapshot1.child("type").getValue().toString();
                    String visto = snapshot1.child("isseen").getValue().toString();
                    if(snapshot1.child("latitud").getValue()!=null){
                       latitud = snapshot1.child("latitud").getValue().toString();
                        longitud = snapshot1.child("longitud").getValue().toString();
                    }
                    Chat chat = new Chat(enviador, recividor,men,type, latitud, longitud,visto);

                    if (chat.getReciver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReciver().equals(userid) && chat.getSender().equals(myid)){
                        mchat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
//Mensaje imagenes----------------

    private void sendImageMessage(String sender, String reciver,Uri imagenUri){
        this.pd = ProgressDialog.show(this, "Jobi",
                "Subiendo imagen...", true, false);
        if (imagenUri != null){
            final StorageReference fileReference = Sreference.child(System.currentTimeMillis()
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
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                        HashMap<String, Object> hashMap = new  HashMap<>();
                        hashMap.put("sender" , sender);
                        hashMap.put("reciver" , reciver);
                        hashMap.put("message" , mUri);
                        hashMap.put("type" , "imagen");
                        hashMap.put("isseen", false);
                        reference.child("Chats").push().setValue(hashMap);

                        // Añadir usuario a chat fragment
                        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                                .child(fuser.getUid())
                                .child(userid);


                        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(!snapshot.exists()){
                                    chatRef.child("id").setValue(userid);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                                .child(userid)
                                .child(fuser.getUid());
                        chatRefReceiver.child("id").setValue(fuser.getUid());
                        pd.dismiss();


                    }else {
                        Toast.makeText(MessageActivity.this,"Failed!",Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MessageActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            Toast.makeText(MessageActivity.this,"No seleccionaste imagen.",Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }
    }
    private String getFileExtension(Uri mUri){

        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));

    }

    public void seleccionarImagen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }




    private void SeleccionarTipo(){
        String[] options = {"Imágenes", "Documentos o Audio","Ubicación"};
        //Dialogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tipo de archivo");
        //Opciones para el dialogo
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Opción Imágenes
                if(which==0){
                    seleccionarImagen();
                }
                //Opción Documentos o Audio
                if(which==1){
                    seleccionarArchivo();
                }

                //Opción Ubicación
                if(which==2){
                    CompartirUbicaion(fuser.getUid(), userid);
                }
            }
        });
        builder.create().show();
    }




    //MENSAJE ARCHIVOS


    private  void uploadFiletoFirebase(Uri data, String sender, String reciver){
        this.pd = ProgressDialog.show(this, "Jobi",
                "Subiendo archivo...", true, false);

        StorageReference reference = Freference.child(System.currentTimeMillis() + "pdf");
        reference.putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isComplete());
                Uri uri = uriTask.getResult();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                HashMap<String, Object> hashMap = new  HashMap<>();
                hashMap.put("sender" , sender);
                hashMap.put("reciver" , reciver);
                hashMap.put("message" , uri.toString());
                hashMap.put("type" , "archivo");
                hashMap.put("isseen", false);
                reference.child("Chats").push().setValue(hashMap);

                // Añadir usuario a chat fragment
                final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                        .child(fuser.getUid())
                        .child(userid);


                chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.exists()){
                            chatRef.child("id").setValue(userid);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                        .child(userid)
                        .child(fuser.getUid());
                chatRefReceiver.child("id").setValue(fuser.getUid());
                pd.dismiss();



            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

            }
        });




    }
    public void seleccionarArchivo() {
        Intent intent = new Intent();
       intent.setType("*/*");

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "FILE SELECT"),12);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imagenUri = data.getData();
           sendImageMessage(fuser.getUid(), userid,imagenUri);

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(MessageActivity.this,"Subida en curso.", Toast.LENGTH_SHORT).show();
            }else {
                // uploadToFirebase();
            }
        }

        if(requestCode == 12 && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            uploadFiletoFirebase(data.getData(),fuser.getUid(),userid);
        }

    }

    private void CompartirUbicaion(String sender, String reciver ){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if (ActivityCompat.checkSelfPermission(MessageActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled()) { //Si tenemos el GPS habilitado


                   LocationServices.getFusedLocationProviderClient(MessageActivity.this).requestLocationUpdates(locationRequest, new LocationCallback() {
                       @Override
                       public void onLocationResult(@NonNull LocationResult locationResult) {
                           super.onLocationResult(locationResult);
                           LocationServices.getFusedLocationProviderClient(MessageActivity.this)
                                   .removeLocationUpdates(this);

                           if (locationResult != null && locationResult.getLocations().size() >0){

                               int index = locationResult.getLocations().size() - 1;
                               double latitude = locationResult.getLocations().get(index).getLatitude();
                               double longitude = locationResult.getLocations().get(index).getLongitude();
                               DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                               HashMap<String, Object> hashMap = new  HashMap<>();
                               hashMap.put("sender" , sender);
                               hashMap.put("reciver" , reciver);
                               hashMap.put("message" , "mensaje");
                               hashMap.put("type" , "Ubicación");
                               hashMap.put("latitud" , latitude);
                               hashMap.put("longitud" , longitude);
                               hashMap.put("isseen", false);
                               reference.child("Chats").push().setValue(hashMap);


                                // Añadir usuario a chat fragment
                               final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                                       .child(fuser.getUid())
                                       .child(userid);


                               chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                                       if(!snapshot.exists()){
                                           chatRef.child("id").setValue(userid);
                                       }
                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError error) {

                                   }
                               });

                               final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                                       .child(userid)
                                       .child(fuser.getUid());
                               chatRefReceiver.child("id").setValue(fuser.getUid());


                           }
                       }
                   }, Looper.getMainLooper());



                } else { //Si no esta habilitado lo prendemos
                    turnOnGPS();
                }

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }



    }

    private void turnOnGPS() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MessageActivity.this, "El GPS está prendido", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MessageActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }
    private boolean isGPSEnabled() { //Si el GPS está habilitado
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }
//ONLINE O OFFLINE
private void status(String status){
    reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

    HashMap<String, Object> hashMap = new HashMap<>();
    hashMap.put("status", status);

    reference.updateChildren(hashMap);
}

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
    }
}