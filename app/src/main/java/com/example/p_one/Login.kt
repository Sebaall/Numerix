package com.example.p_one

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AlertDialog
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

    private lateinit var firebase: FirebaseFirestore
    private lateinit var txtcorreo: EditText
    private lateinit var txtcontrasena: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebase = FirebaseFirestore.getInstance()
        txtcorreo = findViewById(R.id.txt_correo)
        txtcontrasena = findViewById(R.id.txt_contrasena)
        btnLogin = findViewById(R.id.btn_login)

        btnLogin.setOnClickListener {
            validador()
        }
    }

    private fun validador() {
        val correo = txtcorreo.text.toString().trim()
        val pass = txtcontrasena.text.toString()

        if (correo.isEmpty()) {
            mostrarAlerta("Error", "Ingresa tu correo")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            mostrarAlerta("Error", "Correo no válido")
            return
        }
        if (pass.isEmpty()) {
            mostrarAlerta("Error", "Ingresa tu contraseña")
            return
        }
        if (pass.length < 6) {
            mostrarAlerta("Error", "La contraseña debe tener al menos 6 caracteres")
            return
        }

        firebase.collection("users")
            .whereEqualTo("correo", correo)
            .limit(1)
            .get()
            .addOnSuccessListener { correo ->
                if (correo.isEmpty) {
                    mostrarAlerta("Usuario no encontrado", "El correo ingresado no existe en la base de datos.")
                } else {
                    val doc = correo.documents.first()
                    val passDb = doc.getString("contrasena") ?: ""

                    if (passDb == pass) {
                        android.os.Handler(mainLooper).postDelayed({
                            val cruder = Intent(this, Crud::class.java)
                            startActivity(cruder)
                        }, 1000)
                    } else {
                        mostrarAlerta("Error", "Contraseña incorrecta.")
                    }
                }
            }
            .addOnFailureListener {
                mostrarAlerta("Error de conexión", it.message ?: "No se pudo conectar con el servidor.")
            }
    }

    private fun mostrarAlerta(titulo: String, mensaje: String) {
        val b = AlertDialog.Builder(this)
        b.setTitle(titulo)
        b.setMessage(mensaje)
        b.setPositiveButton("Aceptar", null)
        b.create().show()
    }
}
