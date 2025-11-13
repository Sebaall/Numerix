package com.example.p_one.Models

data class Rol(
    var idRol: String? = null,                     // ID del rol, ej: "rol_admin"
    var nombreRol: String? = null,                 // Nombre legible, ej: "Administrador"
    var descripcionRol: String? = null,            // Descripción del rol

    var nivelAcceso: Int? = null,                  // Jerarquía, ej: 10 admin, 5 profe, 1 alumno

    // Aquí guardarás SOLO UN permiso: el menú que seleccionas en el spinner
    // Ej: ["MENU_ADMIN"], ["MENU_PROFESOR"], ["MENU_ALUMNOS"]
    var permisos: List<String>? = null,

    var fechaCreacion: com.google.firebase.Timestamp? = null,

    // UID del admin que creó este rol (lo eliges desde un spinner que solo muestra admins)
    var creadoPor: String? = null
)
