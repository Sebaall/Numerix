package com.example.p_one.crudAdmin

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.p_one.Models.Curso
import com.example.p_one.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class crudCursos : AppCompatActivity() {

    private lateinit var firebase: FirebaseFirestore

    private lateinit var txtNombreCurso: TextInputEditText
    private lateinit var txtNivelCurso: TextInputEditText

    private var documentoId: String? = null   // por si luego quieres modo edición

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crud_cursos)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        firebase = FirebaseFirestore.getInstance()

        txtNombreCurso = findViewById(R.id.txt_nombre_curso)
        txtNivelCurso = findViewById(R.id.txt_nivel_curso)
    }

    fun crearCurso(view: View) {
        val nombre = txtNombreCurso.text?.toString()?.trim().orEmpty()
        val nivel = txtNivelCurso.text?.toString()?.trim().orEmpty()

        if (nombre.isEmpty() || nivel.isEmpty()) {
            mostrarAlerta("Error", "Completa nombre y nivel del curso.")
            return
        }

        if (documentoId != null) {
            mostrarAlerta("Aviso", "Estás en modo edición. Usa Editar curso.")
            return
        }

        val cursosRef = firebase.collection("Cursos")
        val nuevoDoc = cursosRef.document()
        val idGenerado = nuevoDoc.id

        val curso = Curso(
            idCurso = idGenerado,
            nombreCurso = nombre,
            nivel = nivel,
            profesorId = null        // se llenará después según lo que elija el profe
        )

        nuevoDoc.set(curso)
            .addOnSuccessListener {
                mostrarAlerta("Éxito", "Curso '$nombre' creado correctamente.")
                limpiarFormCurso()
            }
            .addOnFailureListener { e ->
                mostrarAlerta("Error", e.message ?: "No se pudo guardar el curso.")
            }
    }

    private fun limpiarFormCurso() {
        txtNombreCurso.setText("")
        txtNivelCurso.setText("")
        txtNombreCurso.requestFocus()
    }

    private fun mostrarAlerta(titulo: String, mensaje: String) {
        val b = AlertDialog.Builder(this)
        b.setTitle(titulo)
        b.setMessage(mensaje)
        b.setPositiveButton("Aceptar", null)
        b.create().show()
    }
}
