package com.example.p_one

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Ranking : AppCompatActivity() {

    private lateinit var tvTituloRanking: TextView
    private lateinit var lvRanking: ListView
    private lateinit var btnVolverJugar: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var db: FirebaseFirestore

    private var apodoAlumno: String = "Invitado"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ranking)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        tvTituloRanking = findViewById(R.id.tvTituloRanking)
        lvRanking = findViewById(R.id.lvRanking)
        btnVolverJugar = findViewById(R.id.btnVolverJugar)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        // 👇 Recuperamos el apodo que envió Results
        apodoAlumno = intent.getStringExtra("apodoAlumno") ?: "Invitado"

        btnVolverJugar.setOnClickListener {
            // Volver al quiz usando el MISMO apodo
            val intentQuiz = Intent(this, mathQuiz::class.java)
            intentQuiz.putExtra("apodoAlumno", apodoAlumno)
            intentQuiz.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intentQuiz)
            finish()
        }

        btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intentLogin = Intent(this, Login::class.java) // pon el nombre real de tu Login
            intentLogin.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intentLogin)
        }

        cargarRanking()
    }

    private fun cargarRanking() {
        db.collection("mathQuizResultados")
            .orderBy("porcentaje", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->
                if (snap.isEmpty) {
                    Toast.makeText(this, "Sin resultados aún", Toast.LENGTH_SHORT).show()
                } else {
                    val items = mutableListOf<String>()
                    var posicion = 1

                    for (doc in snap.documents) {
                        val apodo = doc.getString("apodo") ?: "Sin apodo"
                        val porcentaje = doc.getDouble("porcentaje") ?: 0.0
                        val correctas = doc.getLong("correctas") ?: 0
                        val totalPreguntas = doc.getLong("totalPreguntas") ?: 0

                        val linea = "$posicion. $apodo  -  ${porcentaje.toInt()}%  ($correctas/$totalPreguntas)"
                        items.add(linea)
                        posicion++
                    }

                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        items
                    )
                    lvRanking.adapter = adapter
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al cargar ranking: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
