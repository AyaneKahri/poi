package com.example.jobi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth; //Declaramos la instancia de FirebaseAuth
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance(); //Inicializamos FirebaseAuth
    }

    //Método para el botón Registro
    public void PantallaRegistro(View view){
        Intent registro = new Intent(this, Registro.class);
        startActivity(registro);
    }

    //Método para el botón Iniciar Sesión
    public void PantallaLogin(View view){
        Intent login = new Intent(this, Login.class);
        startActivity(login);
    }

    //VERIFICAMOS SI EL USUARIO YA INICIO SESIÓN
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){


       Intent principal = new Intent(getApplicationContext(), Menu.class);
        startActivity(principal);
            finish(); //Evita que vaya  la pantalla de Registro*/
        }
    }
}