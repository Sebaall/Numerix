package com.example.p_one.Models

data class Users(

    // 🔐 Identificación
    var uidAuth: String? = null,       // UID en Firebase Auth
    var rol: String? = null,           // Alumno / Profesor / Administrador
    var activo: Boolean = true,        // Estado del usuario

    // 👤 Datos comunes
    var nombre: String? = null,
    var apellido: String? = null,
    var correo: String? = null,

    // 👶 Datos exclusivos de Alumno
    var idAlumno: String? = null,      // Igual al UID
    var apodoAlumno: String? = null,
    var edadAlumno: Int? = null,
    var idCurso: String? = null,       // ID del curso asignado
    var numAlumno: Long? = null,       // Número correlativo del alumno

    // 👨‍🏫 Datos exclusivos de Profesor
    var idProfesor: String? = null,
    var cursosAsignados: List<String>? = null,

    // 🛠 Datos exclusivos de Administrador
    var idAdmin: String? = null,

    // 🧩 Roles y permisos
    var roles: List<String>? = null,   // Ej: ["MENU_ALUMNOS"]
    var nivelAcceso: Int? = 1,         // Jerarquía de acceso

    // 📅 Auditoría
    var emailVerificado: Boolean = false,
    var createdAt: Long? = System.currentTimeMillis(),
    var updatedAt: Long? = null
)
