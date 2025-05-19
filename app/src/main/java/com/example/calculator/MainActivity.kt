package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    // Activitatea principală Android: pornește UI-ul Compose
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorScreen()
        }
    }
}

@Composable
fun CalculatorScreen() {
    // Variabile pentru starea UI (valoare afișată, expresia de sus, operatori etc.)
    var display by remember { mutableStateOf("0") } // Ce se afișează jos (rezultatul)
    var expression by remember { mutableStateOf("") } // Expresia completă (ex: 1+2+3=)
    var operand1 by remember { mutableStateOf<Double?>(null) } // Primul operand (număr)
    var pendingOperator by remember { mutableStateOf<String?>(null) } // Operatorul curent
    var shouldReset by remember { mutableStateOf(false) } // Dacă trebuie resetat display-ul la următorul input

    // Istoric calcule
    var showHistory by remember { mutableStateOf(false) } // Flag pentru afișare istoric
    val history = remember { mutableStateListOf<String>() } // Listă cu toate calculele efectuate

    // Funcție pentru a procesa orice buton apăsat (logica calculatorului)
    fun handleInput(input: String) {
        when (input) {
            "C" -> { // Clear: resetează tot
                display = "0"
                operand1 = null
                pendingOperator = null
                shouldReset = false
                expression = ""
            }
            "+", "-", "×", "÷" -> { // Operator nou apăsat
                if (operand1 == null) {
                    operand1 = display.replace(",", ".").toDoubleOrNull()
                    expression = display + input
                } else if (pendingOperator != null) {
                    val result = calculate(
                        operand1,
                        display.replace(",", ".").toDoubleOrNull(),
                        pendingOperator
                    )
                    display = result.toString().removeSuffix(".0")
                    operand1 = result
                    expression += display + input
                }
                pendingOperator = input
                shouldReset = true
            }
            "=" -> { // Egal: efectuează calculul
                if (pendingOperator != null && operand1 != null) {
                    val result = calculate(
                        operand1,
                        display.replace(",", ".").toDoubleOrNull(),
                        pendingOperator
                    )
                    expression += display + "="
                    // Adaugă la istoric expresia și rezultatul
                    history.add(expression + result.toString().removeSuffix(".0"))
                    display = result.toString().removeSuffix(".0")
                    operand1 = null
                    pendingOperator = null
                    shouldReset = true
                }
            }
            "+/-" -> { // Schimbă semnul numărului
                if (display != "0") {
                    if (display.startsWith("-")) display = display.substring(1)
                    else display = "-$display"
                }
            }
            "%" -> { // Procent
                val value = display.replace(",", ".").toDoubleOrNull()
                if (value != null) {
                    display = (value / 100).toString()
                }
            }
            "." -> { // Punct zecimal
                if (!display.contains(".")) {
                    display += "."
                }
            }
            else -> { // Orice cifră apăsată
                if (display == "0" || shouldReset) {
                    display = input
                    shouldReset = false
                } else {
                    display += input
                }
                if (pendingOperator == null) {
                    expression = display
                }
            }
        }
    }

    // Structura principală a UI-ului (toată aplicația)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF333333))
            .padding(8.dp)
    ) {
        // Buton pentru a afișa/ascunde istoricul calculelor (colț stânga sus)
        IconButton(
            onClick = { showHistory = !showHistory },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Text("🕑", color = Color.White, fontSize = 26.sp)
        }

        // Dacă istoricul este deschis, afișăm lista peste calculator
        if (showHistory) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC222222))
                    .padding(top = 56.dp)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(24.dp)
                        .background(Color(0xFF444444), shape = RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        "Istoric calcule:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (history.isEmpty()) {
                        Text(
                            "Niciun calcul efectuat.",
                            color = Color.LightGray,
                            fontSize = 16.sp
                        )
                    } else {
                        // Afișăm fiecare calcul, de la ultimul la primul
                        history.asReversed().forEach {
                            Text(
                                text = it,
                                color = Color.White,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Calculatorul propriu-zis
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // Expresia completă (ex: 1+2+3=)
                Text(
                    text = expression,
                    color = Color.Gray,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                // Valoarea afișată jos (rezultatul sau numărul curent)
                Text(
                    text = display,
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Definim fiecare rând de butoane ca o listă
                val buttonRows = listOf(
                    listOf("C", "+/-", "%", "÷"),
                    listOf("7", "8", "9", "×"),
                    listOf("4", "5", "6", "-"),
                    listOf("1", "2", "3", "+"),
                    listOf("0", ".", "=")
                )

                // Desenăm fiecare rând de butoane
                buttonRows.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { label ->
                            CalculatorButton(
                                label = label,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                onClick = { handleInput(label) },
                                isOperator = label in listOf("÷", "×", "-", "+", "=")
                            )
                        }
                        // Dacă nu sunt 4 butoane pe rând, completează cu spațiu gol
                        if (row.size < 4) {
                            Spacer(
                                modifier = Modifier.weight((4 - row.size).toFloat())
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Composable pentru un buton de calculator (folosit la fiecare buton numeric/operator)
@Composable
fun CalculatorButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isOperator: Boolean = false
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isOperator) Color(0xFFF4A940) else Color(0xFF555555),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(50),
        modifier = modifier.padding(2.dp)
    ) {
        Text(
            text = label,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Funcția matematică ce realizează efectiv calculele (plus, minus, înmulțire, împărțire)
fun calculate(
    op1: Double?,
    op2: Double?,
    operator: String?
): Double {
    if (op1 == null || op2 == null || operator == null) return 0.0
    return when (operator) {
        "+" -> op1 + op2
        "-" -> op1 - op2
        "×" -> op1 * op2
        "÷" -> if (op2 == 0.0) 0.0 else op1 / op2
        else -> op2 ?: 0.0
    }
}
