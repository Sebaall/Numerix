package com.example.p_one.Models

data class Usuario(
    var idUsuario: String? = null,
    var correo: String? = null,
    var nombre: String? = null,
    var rol: String? = null,          // "alumno" | "profesor" | "admin"
    var idPerfil: String? = null,     // docId en Alumnos/Profesores/Admins
    var activo: Boolean = true,
    var createdAt: Long? = System.currentTimeMillis()
)
