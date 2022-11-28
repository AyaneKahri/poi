package com.example.jobi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    private EditText correo;
    private EditText contraseña;

    //VARIABLES PARA VALIDAR

    private String mail = "";
    private String password = "";

    Button btnLogin;
    private FirebaseAuth mAuth; //Declaramos la instancia de FirebaseAuth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance(); //Inicializamos FirebaseAuth
        //ID de los textboxs
        correo = findViewById(R.id.txt_mail);
        contraseña = findViewById(R.id.txt_password);
        //ID del botón Login
        btnLogin = findViewById(R.id.btn_login);

        //CUANDO CLICKEO A BTN_LOGIN
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mail = correo.getText().toString();
                password = contraseña.getText().toString();
                //Si los textbox NO estan vacios
                if(!mail.isEmpty() && !password.isEmpty()) {
                    Login();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Favor de llenar los campos",Toast.LENGTH_SHORT).show();
                }


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

            mAuth.getInstance().signOut();
        }
    }
    public void Login() {
        mAuth.signInWithEmailAndPassword(mail.trim(), password.trim())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(getApplicationContext(), "Has Iniciado Sesión",Toast.LENGTH_SHORT).show();

                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent principal = new Intent(getApplicationContext(), Menu.class);
                            startActivity(principal);
                            finish(); //Evita que vaya  la pantalla de Login
                           // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(getApplicationContext(), "Authentication failed",Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }



}

