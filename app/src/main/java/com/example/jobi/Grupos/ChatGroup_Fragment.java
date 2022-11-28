package com.example.jobi.Grupos;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobi.Adapter.GroupMessageAdapter;
import com.example.jobi.Adapter.MessageAdapter;
import com.example.jobi.Adapter.UserAdapter;
import com.example.jobi.Menu;
import com.example.jobi.MessageActivity;
import com.example.jobi.Model.Chat;
import com.example.jobi.Model.Chatlist;
import com.example.jobi.Model.User;
import com.example.jobi.R;
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


public class ChatGroup_Fragment extends Fragment {
    //BOTONES DE LA BARRA DE MENSAJES
    ImageButton attachBtn;
    ImageButton btn_send;
    EditText text_send;

    //ID DEL USUARIO LOGEADO Y DEL GRUPO
    FirebaseUser fuser;
    String groupid;

    //VARIABLES PARA LA FOTO
    String mUri;
    private StorageReference Sreference = FirebaseStorage.getInstance().getReference("GroupChatImages");
    private StorageReference Freference = FirebaseStorage.getInstance().getReference("GroupChatFiles");
    Uri imagenUri;
    StorageTask uploadTask;
    private static final int IMAGE_REQUEST = 1;
    private ProgressDialog pd = null;

    //Listas
    DatabaseReference reference;
    GroupMessageAdapter messageAdapter;
    List<Chat> mchat;
    RecyclerView recyclerView;

    //UBICACION
    private LocationRequest locationRequest;
    String latitud = null;
    String longitud = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatsgroup, container, false);

        text_send = view.findViewById(R.id.text_send);
        btn_send = view.findViewById(R.id.btn_send);
        attachBtn = view.findViewById(R.id.attachBtn);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //Agarramos el ID del GRUPO
        SharedPreferences prefs = getActivity().getSharedPreferences("Preferences", 0);
        groupid = prefs.getString("groupid", "");
        //Agarramos el ID del usuario logeado
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        //BOTÓN DE ENVIAR ARCHIVOS
        attachBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public  void onClick(View view){
                SeleccionarTipo();
            }
        });


        //Cuando presione el botón de enviar
        btn_send.setOnClickListener(new View.OnClickListener(){

            @Override
            public  void onClick(View view){

                String msg = text_send.getText().toString();
                if(!msg.equals("")){

                 sendMesage(fuser.getUid(), groupid,msg);

                }else{
                    Toast.makeText(getContext(), "No puedes enviar mensajes vacios ", Toast.LENGTH_SHORT).show();
                }

                text_send.setText("");
            }
        });

        //OBTENER UBICACION

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);



        readMessages(fuser.getUid(),groupid);


        return  view;

    }

//Mensajes de texto
    private void sendMesage(String sender, String reciver,  String message){

        //ID del mensaje
        String id= ""+System.currentTimeMillis();

        HashMap<String, String> hashMap = new  HashMap<>();
        hashMap.put("sender" , sender);
        hashMap.put("groupid" , reciver);
        hashMap.put("message" , message);
        hashMap.put("type" , "texto");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");


        ref.child(reciver).child("Chats").child(id).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "No se pudo enviar el mensaje ", Toast.LENGTH_SHORT).show();
            }
        });


    }
    private void readMessages (String myid, String userid){
        mchat = new ArrayList<>();


        reference = FirebaseDatabase.getInstance().getReference("Groups").child(groupid).child("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mchat.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    // Chat chat = snapshot1.getValue(Chat.class);
                    String men = snapshot1.child("message").getValue().toString();
                    String recividor = snapshot1.child("groupid").getValue().toString();
                    String enviador = snapshot1.child("sender").getValue().toString();
                    String type = snapshot1.child("type").getValue().toString();
                    if(snapshot1.child("latitud").getValue()!=null){
                        latitud = snapshot1.child("latitud").getValue().toString();
                        longitud = snapshot1.child("longitud").getValue().toString();
                    }
                    Chat chat = new Chat(enviador, recividor,men,type, latitud, longitud,"");


                    mchat.add(chat);


                    messageAdapter = new GroupMessageAdapter(getContext(), mchat);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //Mensajes de imagenes
    private void sendImageMessage(String sender, String reciver,Uri imagenUri){
        //ID del mensaje
        String id= ""+System.currentTimeMillis();
        this.pd = ProgressDialog.show(getContext(), "Jobi",
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
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");

                        HashMap<String, Object> hashMap = new  HashMap<>();
                        hashMap.put("sender" , sender);
                        hashMap.put("groupid" , reciver);
                        hashMap.put("message" , mUri);
                        hashMap.put("type" , "imagen");

                        reference.child(reciver).child("Chats").child(id).setValue(hashMap);

                        pd.dismiss();


                    }else {
                        Toast.makeText(getContext(),"Failed!",Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            Toast.makeText(getContext(),"No seleccionaste imagen.",Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }
    }
    private String getFileExtension(Uri mUri){

        ContentResolver cr = getActivity().getApplicationContext().getContentResolver();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Tipo de archivo");
        //Opciones para el dialogo
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Opción Imágenes
                if(which==0){
                    seleccionarImagen();
                }
                //Opción Documentos
                if(which==1){
                    seleccionarArchivo();
                }
                //Opción Ubicación
                if(which==2){
                    CompartirUbicaion(fuser.getUid(), groupid);
                }
            }
        });
        builder.create().show();
    }


    private  void uploadPDFtoFirebase(Uri data, String sender, String reciver){
        //ID del mensaje
        String id= ""+System.currentTimeMillis();
        this.pd = ProgressDialog.show(getContext(), "Jobi",
                "Subiendo archivo...", true, false);

        StorageReference reference = Freference.child(System.currentTimeMillis() + "pdf");
        reference.putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isComplete());
                Uri uri = uriTask.getResult();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");

                HashMap<String, Object> hashMap = new  HashMap<>();
                hashMap.put("sender" , sender);
                hashMap.put("groupid" , reciver);
                hashMap.put("message" , uri.toString());
                hashMap.put("type" , "archivo");

                reference.child(reciver).child("Chats").child(id).setValue(hashMap);


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
        startActivityForResult(Intent.createChooser(intent, "PDF FILE SELECT"),12);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null){
            imagenUri = data.getData();
            sendImageMessage(fuser.getUid(), groupid,imagenUri);

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(getContext(),"Subida en curso.", Toast.LENGTH_SHORT).show();
            }else {
                // uploadToFirebase();
            }
        }

        if(requestCode == 12 && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null){
            uploadPDFtoFirebase(data.getData(),fuser.getUid(),groupid);
        }

    }


    //UBICACIÓN

    private void CompartirUbicaion(String sender, String reciver ){
        //ID del mensaje
        String id= ""+System.currentTimeMillis();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled()) { //Si tenemos el GPS habilitado


                    LocationServices.getFusedLocationProviderClient(getContext()).requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            LocationServices.getFusedLocationProviderClient(getContext())
                                    .removeLocationUpdates(this);

                            if (locationResult != null && locationResult.getLocations().size() >0){

                                int index = locationResult.getLocations().size() - 1;
                                double latitude = locationResult.getLocations().get(index).getLatitude();
                                double longitude = locationResult.getLocations().get(index).getLongitude();
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");

                                HashMap<String, Object> hashMap = new  HashMap<>();
                                hashMap.put("sender" , sender);
                                hashMap.put("groupid" , reciver);
                                hashMap.put("message" , "mensaje");
                                hashMap.put("type" , "Ubicación");
                                hashMap.put("latitud" , latitude);
                                hashMap.put("longitud" , longitude);

                                reference.child(reciver).child("Chats").child(id).setValue(hashMap);

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

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(getContext(), "El GPS está prendido", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(getActivity(), 2);
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
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }

}