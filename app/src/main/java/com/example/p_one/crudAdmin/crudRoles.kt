package com.example.p_one.crudAdmin

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.p_one.Models.Rol
import com.example.p_one.R
import com.google.firebase.firestore.FirebaseFirestore

class crudRoles : AppCompatActivity() {

    private lateinit var firebase: FirebaseFirestore
    private lateinit var txtNombreRol: EditText
    private lateinit var txtDescripcionRol: EditText
    private lateinit var spinnerCreadoPorAdmin: Spinner
    private lateinit var spinnerPermisoMenu: Spinner

    // listas para admins (solo admins en el spinner)
    private val listaAdminsNombres = mutableListOf<String>()
    private val listaAdminsIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crud_roles)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        firebase = FirebaseFirestore.getInstance()

        txtNombreRol = findViewById(R.id.txtNombreRol)
        txtDescripcionRol = findViewById(R.id.txtDescripcionRol)
        spinnerCreadoPorAdmin = findViewById(R.id.spinnerCreadoPorAdmin)
        spinnerPermisoMenu = findViewById(R.id.spinnerPermisoMenu)

        cargarAdminsEnSpinner()
        cargarMenusEnSpinner()
    }

    private fun cargarAdminsEnSpinner() {
        // Solo admins: users con roles que contengan "MENU_ADMIN"
        firebase.collection("users")
            .whereArrayContains("roles", "MENU_ADMIN")
            .get()
            .addOnSuccessListener { snap ->
                listaAdminsNombres.clear()
                listaAdminsIds.clear()

                for (doc in snap.documents) {
                    val uid = doc.id
                    val nombre = doc.getString("nombre")?.trim().orEmpty()
                    val apellido = doc.getString("apellido")?.trim().orEmpty()
                    val correo = doc.getString("correo")?.trim().orEmpty()

                    val display = when {
                        nombre.isNotEmpty() || apellido.isNotEmpty() ->
                            listOf(nombre, apellido).filter { it.isNotEmpty() }.joinToString(" ")
                        correo.isNotEmpty() -> correo
                        else -> uid
                    }

                    listaAdminsNombres.add(display)
                    listaAdminsIds.add(uid)
                }

                if (listaAdminsNombres.isEmpty()) {
                    listaAdminsNombres.add("Sin administradores")
                    listaAdminsIds.add("")
                }

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    listaAdminsNombres
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCreadoPorAdmin.adapter = adapter
            }
            .addOnFailureListener {
                listaAdminsNombres.clear()
                listaAdminsIds.clear()
                listaAdminsNombres.add("Error cargando admins")
                listaAdminsIds.add("")
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    listaAdminsNombres
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCreadoPorAdmin.adapter = adapter
            }
    }

    private fun cargarMenusEnSpinner() {
        // Texto visible en el spinner
        val opcionesVisibles = listOf(
            "Menú alumnos",
            "Menú profesor",
            "Menú admin"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opcionesVisibles
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPermisoMenu.adapter = adapter
    }

    // Enlázala desde el XML con android:onClick="crearRol"
    fun crearRol(view: View) {
        val nombre = txtNombreRol.text.toString().trim()
        val descripcion = txtDescripcionRol.text.toString().trim()

        if (nombre.isEmpty() || descripcion.isEmpty()) {
            mostrarAlerta("Error", "Ingresa nombre y descripción.")
            return
        }

        // Validar admin creador
        val idxAdmin = spinnerCreadoPorAdmin.selectedItemPosition
        if (idxAdmin !in listaAdminsIds.indices || listaAdminsIds[idxAdmin].isEmpty()) {
            mostrarAlerta("Error", "No hay un administrador válido seleccionado como creador.")
            return
        }
        val uidCreador = listaAdminsIds[idxAdmin]

        // Permiso único desde el spinner
        val idxMenu = spinnerPermisoMenu.selectedItemPosition
        if (idxMenu == Spinner.INVALID_POSITION) {
            mostrarAlerta("Permiso", "Selecciona un menú.")
            return
        }

        val permisoClave = when (idxMenu) {
            0 -> "MENU_ALUMNOS"
            1 -> "MENU_PROFESOR"
            2 -> "MENU_ADMIN"
            else -> "MENU_ALUMNOS"
        }

        val permisos = listOf(permisoClave)

        // nivelAcceso derivado según el menú
        val nivelAcceso = when (permisoClave) {
            "MENU_ADMIN" -> 3
            "MENU_PROFESOR" -> 2
            "MENU_ALUMNOS" -> 1
            else -> 1
        }

        val rolesRef = firebase.collection("Roles")
        val docRef = rolesRef.document()
        val idAuto = docRef.id
        val fecha = com.google.firebase.Timestamp.now()

        val rol = Rol(
            idRol = idAuto,
            nombreRol = nombre,
            descripcionRol = descripcion,
            nivelAcceso = nivelAcceso,
            permisos = permisos,
            fechaCreacion = fecha,
            creadoPor = uidCreador
        )

        // Validar duplicado de nombreRol
        firebase.collection("Roles")
            .whereEqualTo("nombreRol", nombre)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                if (!snap.isEmpty) {
                    mostrarAlerta("Error", "Ya existe un rol con ese nombre.")
                    txtNombreRol.text.clear()
                } else {
                    docRef.set(rol)
                        .addOnSuccessListener {
                            mostrarAlerta("Éxito", "Rol '$nombre' creado correctamente.")
                            limpiarForm()
                        }
                        .addOnFailureListener { e ->
                            mostrarAlerta("Error", e.message ?: "No se pudo guardar el rol.")
                        }
                }
            }
            .addOnFailureListener { e ->
                mostrarAlerta("Error", e.message ?: "Error al verificar duplicados.")
            }
    }

    private fun limpiarForm() {
        txtNombreRol.text.clear()
        txtDescripcionRol.text.clear()
        if (spinnerCreadoPorAdmin.adapter != null && spinnerCreadoPorAdmin.adapter.count > 0) {
            spinnerCreadoPorAdmin.setSelection(0)
        }
        if (spinnerPermisoMenu.adapter != null && spinnerPermisoMenu.adapter.count > 0) {
            spinnerPermisoMenu.setSelection(0)
        }
        txtNombreRol.requestFocus()
    }

    private fun mostrarAlerta(titulo: String, mensaje: String) {
        val b = AlertDialog.Builder(this)
        b.setTitle(titulo)
        b.setMessage(mensaje)
        b.setPositiveButton("Aceptar", null)
        b.create().show()
    }
}
