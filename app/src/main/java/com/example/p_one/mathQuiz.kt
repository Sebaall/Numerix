package com.example.p_one

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class mathQuiz : AppCompatActivity() {

    private enum class OperationType {
        SUMA, RESTA, MULTIPLICACION, DIVISION
    }

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var tvApodoHeader: TextView
    private lateinit var tvProgreso: TextView
    private lateinit var tvOperacion: TextView
    private lateinit var tvResultadoInstantaneo: TextView
    private lateinit var tvResumenParcial: TextView

    private lateinit var btnOpcion1: Button
    private lateinit var btnOpcion2: Button
    private lateinit var btnOpcion3: Button
    private lateinit var btnOpcion4: Button
    private lateinit var btnSiguiente: Button

    private val random = Random(System.currentTimeMillis())

    private var num1: Int = 0
    private var num2: Int = 0
    private var respuestaCorrecta: Int = 0
    private var operacionActual: OperationType = OperationType.SUMA
    private var simboloOperacion: String = "+"

    private val totalPreguntas = 10
    private var numeroPregunta = 1
    private var correctas = 0
    private var incorrectas = 0

    private var respondido = false

    private var apodoAlumno: String = "Invitado"
    private var uidAuth: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_math_quiz)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        uidAuth = auth.currentUser?.uid

        apodoAlumno = intent.getStringExtra("apodoAlumno") ?: "Invitado"

        tvApodoHeader = findViewById(R.id.tvApodoHeader)
        tvProgreso = findViewById(R.id.tvProgreso)
        tvOperacion = findViewById(R.id.tvOperacion)
        tvResultadoInstantaneo = findViewById(R.id.tvResultadoInstantaneo)
        tvResumenParcial = findViewById(R.id.tvResumenParcial)

        btnOpcion1 = findViewById(R.id.btnOpcion1)
        btnOpcion2 = findViewById(R.id.btnOpcion2)
        btnOpcion3 = findViewById(R.id.btnOpcion3)
        btnOpcion4 = findViewById(R.id.btnOpcion4)
        btnSiguiente = findViewById(R.id.btnSiguiente)

        tvApodoHeader.text = "Alumno: $apodoAlumno"
        actualizarProgreso()
        actualizarResumenParcial()

        btnOpcion1.setOnClickListener { verificarRespuesta(btnOpcion1.text.toString()) }
        btnOpcion2.setOnClickListener { verificarRespuesta(btnOpcion2.text.toString()) }
        btnOpcion3.setOnClickListener { verificarRespuesta(btnOpcion3.text.toString()) }
        btnOpcion4.setOnClickListener { verificarRespuesta(btnOpcion4.text.toString()) }

        btnSiguiente.setOnClickListener {
            if (!respondido) {
                Toast.makeText(this, "Primero responde la pregunta", Toast.LENGTH_SHORT).show()
            } else {
                if (numeroPregunta >= totalPreguntas) {
                    // 👉 Aquí ya es la décima: guardamos y vamos al RESULT
                    guardarResultadoEnFirestore()
                } else {
                    numeroPregunta++
                    generarNuevaPregunta()
                }
            }
        }

        generarNuevaPregunta()
    }

    private fun actualizarProgreso() {
        tvProgreso.text = "Pregunta $numeroPregunta / $totalPreguntas"
    }

    private fun actualizarResumenParcial() {
        tvResumenParcial.text = "Correctas: $correctas | Incorrectas: $incorrectas"
    }

    private fun generarNuevaPregunta() {
        respondido = false
        tvResultadoInstantaneo.text = ""

        operacionActual = OperationType.values()[random.nextInt(OperationType.values().size)]

        when (operacionActual) {
            OperationType.SUMA -> {
                simboloOperacion = "+"
                num1 = random.nextInt(10)
                num2 = random.nextInt(10)
                respuestaCorrecta = num1 + num2
            }
            OperationType.RESTA -> {
                simboloOperacion = "-"
                val a = random.nextInt(10)
                val b = random.nextInt(10)
                num1 = max(a, b)
                num2 = min(a, b)
                respuestaCorrecta = num1 - num2
            }
            OperationType.MULTIPLICACION -> {
                simboloOperacion = "×"
                num1 = random.nextInt(10)
                num2 = random.nextInt(10)
                respuestaCorrecta = num1 * num2
            }
            OperationType.DIVISION -> {
                simboloOperacion = "÷"
                val resultado = random.nextInt(10)
                val divisor = random.nextInt(9) + 1
                num1 = resultado * divisor
                num2 = divisor
                respuestaCorrecta = resultado
            }
        }

        tvOperacion.text = "$num1 $simboloOperacion $num2"
        actualizarProgreso()

        val opcionesSet = mutableSetOf<Int>()
        opcionesSet.add(respuestaCorrecta)

        while (opcionesSet.size < 4) {
            opcionesSet.add(random.nextInt(19))
        }

        val opcionesList = opcionesSet.shuffled()
        btnOpcion1.text = opcionesList[0].toString()
        btnOpcion2.text = opcionesList[1].toString()
        btnOpcion3.text = opcionesList[2].toString()
        btnOpcion4.text = opcionesList[3].toString()
    }

    private fun verificarRespuesta(textoBoton: String) {
        if (respondido) return

        val respuestaElegida = textoBoton.toIntOrNull() ?: return
        respondido = true

        if (respuestaElegida == respuestaCorrecta) {
            correctas++
            tvResultadoInstantaneo.text = "¡Correcto!"
            tvResultadoInstantaneo.setTextColor(Color.GREEN)
        } else {
            incorrectas++
            tvResultadoInstantaneo.text = "Incorrecto"
            tvResultadoInstantaneo.setTextColor(Color.RED)
        }

        actualizarResumenParcial()
    }

    private fun guardarResultadoEnFirestore() {
        val porcentaje = (correctas * 100.0) / totalPreguntas.toDouble()

        val data = hashMapOf(
            "uidAuth" to uidAuth,
            "apodo" to apodoAlumno,
            "correctas" to correctas,
            "incorrectas" to incorrectas,
            "totalPreguntas" to totalPreguntas,
            "porcentaje" to porcentaje,
            "fechaUltimoJuego" to Timestamp.now()
        )

        db.collection("mathQuizResultados")
            .document(apodoAlumno)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                val intent = Intent(this, Results::class.java)
                intent.putExtra("apodoAlumno", apodoAlumno)
                intent.putExtra("correctas", correctas)
                intent.putExtra("incorrectas", incorrectas)
                intent.putExtra("totalPreguntas", totalPreguntas)
                intent.putExtra("porcentaje", porcentaje)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar resultados: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
