package com.example.p_one.crudAdmin

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.p_one.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class crudEditRol : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var spinnerUsuario: Spinner
    private lateinit var spinnerNuevoRol: Spinner

    private lateinit var tvNombreUsuario: TextView
    private lateinit var tvCorreoUsuario: TextView
    private lateinit var tvRolActualUsuario: TextView
    private lateinit var progressEditRol: ProgressBar

    private val listaUserIds = mutableListOf<String>()
    private val listaUserLabels = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crud_edit_rol)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        spinnerUsuario = findViewById(R.id.spinnerUsuario)
        spinnerNuevoRol = findViewById(R.id.spinnerNuevoRol)

        tvNombreUsuario = findViewById(R.id.tvNombreUsuario)
        tvCorreoUsuario = findViewById(R.id.tvCorreoUsuario)
        tvRolActualUsuario = findViewById(R.id.tvRolActualUsuario)
        progressEditRol = findViewById(R.id.progressEditRol)

        cargarUsuariosEnSpinner()
        cargarRolesEnSpinner()

        spinnerUsuario.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position < listaUserIds.size && listaUserIds[position].isNotEmpty()) {
                    cargarDatosUsuario(listaUserIds[position])
                } else {
                    limpiarInfoUsuario()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
    }

    private fun cargarUsuariosEnSpinner() {
        progressEditRol.visibility = View.VISIBLE

        db.collection("users")
            .get()
            .addOnSuccessListener { snap ->
                listaUserIds.clear()
                listaUserLabels.clear()

                for (doc in snap.documents) {
                    try {
                        val uid = doc.id
                        val nombre = (doc.getString("nombre") ?: "").trim()
                        val apellido = (doc.getString("apellido") ?: "").trim()
                        val correo = (doc.getString("correo") ?: "").trim()

                        val label = when {
                            nombre.isNotEmpty() || apellido.isNotEmpty() ->
                                listOf(nombre, apellido)
                                    .filter { it.isNotEmpty() }
                                    .joinToString(" ")
                            correo.isNotEmpty() -> correo
                            else -> uid
                        }

                        listaUserIds.add(uid)
                        listaUserLabels.add(label)
                    } catch (e: Exception) {
                        // Si algún documento viene raro, lo ignoramos para no crashear
                    }
                }

                if (listaUserLabels.isEmpty()) {
                    listaUserLabels.add("No hay usuarios")
                    listaUserIds.add("")
                }

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    listaUserLabels
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerUsuario.adapter = adapter

                progressEditRol.visibility = View.GONE
            }
            .addOnFailureListener {
                progressEditRol.visibility = View.GONE
                mostrarAlerta("Error", "No se pudo cargar usuarios.")
            }
    }

    private fun cargarDatosUsuario(uid: String) {
        progressEditRol.visibility = View.VISIBLE

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    try {
                        val nombre = listOfNotNull(
                            doc.getString("nombre"),
                            doc.getString("apellido")
                        )
                            .filter { !it.isNullOrBlank() }
                            .joinToString(" ")
                            .ifBlank { "-" }

                        val correo = doc.getString("correo") ?: "-"
                        val rol = doc.getString("rol") ?: "-"

                        tvNombreUsuario.text = "Nombre: $nombre"
                        tvCorreoUsuario.text = "Correo: $correo"
                        tvRolActualUsuario.text = "Rol actual: $rol"
                    } catch (e: Exception) {
                        limpiarInfoUsuario()
                    }
                } else {
                    limpiarInfoUsuario()
                }

                progressEditRol.visibility = View.GONE
            }
            .addOnFailureListener {
                progressEditRol.visibility = View.GONE
                limpiarInfoUsuario()
                mostrarAlerta("Error", "No se pudo cargar los datos.")
            }
    }

    private fun cargarRolesEnSpinner() {
        val opciones = listOf("Alumno", "Profesor", "Administrador")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opciones
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerNuevoRol.adapter = adapter
    }

    private fun limpiarInfoUsuario() {
        tvNombreUsuario.text = "Nombre: -"
        tvCorreoUsuario.text = "Correo: -"
        tvRolActualUsuario.text = "Rol actual: -"
    }

    fun aplicarRolUsuarioOnClick(view: View) {
        val idxUser = spinnerUsuario.selectedItemPosition

        if (idxUser < 0 || idxUser >= listaUserIds.size || listaUserIds[idxUser].isEmpty()) {
            mostrarAlerta("Error", "Selecciona un usuario válido.")
            return
        }

        val uidUser = listaUserIds[idxUser]
        val idxRol = spinnerNuevoRol.selectedItemPosition

        val (menuClave, rolTexto, nivelAcceso) = when (idxRol) {
            0 -> Triple("MENU_ALUMNOS", "Alumno", 1L)
            1 -> Triple("MENU_PROFESOR", "Profesor", 2L)
            2 -> Triple("MENU_ADMIN", "Administrador", 3L)
            else -> Triple("MENU_ALUMNOS", "Alumno", 1L)
        }

        progressEditRol.visibility = View.VISIBLE

        val updates = hashMapOf<String, Any>(
            "rol" to rolTexto,
            "roles" to listOf(menuClave),
            "nivelAcceso" to nivelAcceso,
            "updatedAt" to System.currentTimeMillis()
        )

        when (menuClave) {
            "MENU_ADMIN" -> {
                updates["idAdmin"] = uidUser
                updates["idAlumno"] = FieldValue.delete()
                updates["idProfesor"] = FieldValue.delete()
            }
            "MENU_PROFESOR" -> {
                updates["idProfesor"] = uidUser
                updates["idAlumno"] = FieldValue.delete()
                updates["idAdmin"] = FieldValue.delete()
            }
            "MENU_ALUMNOS" -> {
                updates["idAlumno"] = uidUser
                updates["idProfesor"] = FieldValue.delete()
                updates["idAdmin"] = FieldValue.delete()
            }
        }

        db.collection("users")
            .document(uidUser)
            .update(updates)
            .addOnSuccessListener {
                progressEditRol.visibility = View.GONE
                tvRolActualUsuario.text = "Rol actual: $rolTexto"
                mostrarAlerta("Listo", "Rol actualizado correctamente.")
            }
            .addOnFailureListener {
                progressEditRol.visibility = View.GONE
                mostrarAlerta("Error", "No se pudo actualizar el rol.")
            }
    }

    private fun mostrarAlerta(titulo: String, mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Aceptar", null)
            .create()
            .show()
    }
}
