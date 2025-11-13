package com.example.p_one.juego

import com.google.firebase.Timestamp

data class MathQuizResultado(
    var id: String? = null,              // id del documento en Firestore (opcional)
    var apodo: String? = null,
    var correctas: Int? = null,
    var incorrectas: Int? = null,
    var totalPreguntas: Int? = null,
    var porcentaje: Double? = null,
    var fechaUltimoJuego: Timestamp? = null
)
