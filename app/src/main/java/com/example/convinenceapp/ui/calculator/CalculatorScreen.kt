package com.example.convinenceapp.ui.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.convinenceapp.data.Board
import com.example.convinenceapp.data.BoardType
import com.example.convinenceapp.ui.boards.BoardsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(viewModel: BoardsViewModel) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("0") }
    var history by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var showSaveDialog by remember { mutableStateOf(false) }
    val boards by viewModel.boards.collectAsState()
    val calcBoards = remember(boards) { boards.filter { it.type == BoardType.CALCULATION } }

    fun evaluate(expr: String): String {
        return try {
            // Simple expression evaluator using Kotlin's built-in
            val cleaned = expr
                .replace("×", "*")
                .replace("÷", "/")
                .replace("%", "/100.0*")
            val result = evalExpr(cleaned)
            if (result == result.toLong().toDouble()) result.toLong().toString()
            else "%.6f".format(result).trimEnd('0').trimEnd('.')
        } catch (e: Exception) {
            "Error"
        }
    }

    fun onButton(label: String) {
        when (label) {
            "=" -> {
                val res = evaluate(expression)
                if (res != "Error") {
                    history = listOf(expression to res) + history.take(19)
                }
                result = res
            }
            "C" -> { expression = ""; result = "0" }
            "⌫" -> { if (expression.isNotEmpty()) expression = expression.dropLast(1) }
            "+/-" -> {
                expression = if (expression.startsWith("-")) expression.drop(1) else "-$expression"
            }
            else -> { expression += label }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculator") },
                actions = {
                    IconButton(
                        onClick = { showSaveDialog = true },
                        enabled = result != "0" && result != "Error" && expression.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save to Board")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = expression.ifEmpty { "0" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 2
                    )
                    Text(
                        text = result,
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        maxLines = 1
                    )
                }
            }

            // Keypad
            val buttons = listOf(
                listOf("C", "+/-", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("⌫", "0", ".", "="),
            )
            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { label ->
                        val isOperator = label in listOf("÷", "×", "-", "+", "=")
                        val isSpecial  = label in listOf("C", "+/-", "%", "⌫")
                        CalcButton(
                            label = label,
                            isOperator = isOperator,
                            isSpecial = isSpecial,
                            modifier = Modifier.weight(1f),
                            onClick = { onButton(label) }
                        )
                    }
                }
            }

            // History
            if (history.isNotEmpty()) {
                Text("History", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(history) { (expr, res) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(expr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("= $res", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveToBoardDialog(
            expression = expression,
            result = result,
            calcBoards = calcBoards,
            onDismiss = { showSaveDialog = false },
            onSave = { boardId ->
                viewModel.saveCalculationToBoard(expression, result, boardId)
                showSaveDialog = false
            }
        )
    }
}

@Composable
private fun CalcButton(
    label: String,
    isOperator: Boolean,
    isSpecial: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = when {
        isOperator -> MaterialTheme.colorScheme.primary
        isSpecial  -> MaterialTheme.colorScheme.secondaryContainer
        else       -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isOperator -> MaterialTheme.colorScheme.onPrimary
        isSpecial  -> MaterialTheme.colorScheme.onSecondaryContainer
        else       -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        if (label == "⌫") {
            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace", modifier = Modifier.size(20.dp))
        } else {
            Text(
                text = label,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SaveToBoardDialog(
    expression: String,
    result: String,
    calcBoards: List<Board>,
    onDismiss: () -> Unit,
    onSave: (boardId: String?) -> Unit
) {
    var selectedBoardId by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save to Board") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$expression = $result", style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
                HorizontalDivider()
                if (calcBoards.isEmpty()) {
                    Text("A new Calculation board will be created.", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("Save to existing board (or leave unselected to create new):", style = MaterialTheme.typography.bodySmall)
                    calcBoards.forEach { board ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedBoardId == board.id,
                                onClick = { selectedBoardId = if (selectedBoardId == board.id) null else board.id }
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(board.title, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedBoardId) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ─── Simple expression evaluator ─────────────────────────────────────────────

private fun evalExpr(expr: String): Double {
    val tokens = tokenize(expr)
    return parseExpr(tokens, 0).first
}

private fun tokenize(expr: String): List<String> {
    val tokens = mutableListOf<String>()
    var i = 0
    val s = expr.replace(" ", "")
    while (i < s.length) {
        when {
            s[i].isDigit() || s[i] == '.' -> {
                val start = i
                while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
                tokens += s.substring(start, i)
            }
            s[i] in "+-*/()" -> { tokens += s[i].toString(); i++ }
            else -> i++
        }
    }
    return tokens
}

private fun parseExpr(tokens: List<String>, pos: Int): Pair<Double, Int> {
    var (left, cur) = parseTerm(tokens, pos)
    while (cur < tokens.size && tokens[cur] in listOf("+", "-")) {
        val op = tokens[cur++]
        val (right, next) = parseTerm(tokens, cur)
        left = if (op == "+") left + right else left - right
        cur = next
    }
    return left to cur
}

private fun parseTerm(tokens: List<String>, pos: Int): Pair<Double, Int> {
    var (left, cur) = parseFactor(tokens, pos)
    while (cur < tokens.size && tokens[cur] in listOf("*", "/")) {
        val op = tokens[cur++]
        val (right, next) = parseFactor(tokens, cur)
        left = if (op == "*") left * right else left / right
        cur = next
    }
    return left to cur
}

private fun parseFactor(tokens: List<String>, pos: Int): Pair<Double, Int> {
    var cur = pos
    if (cur < tokens.size && tokens[cur] == "(") {
        cur++ // skip '('
        val (v, next) = parseExpr(tokens, cur)
        return v to (next + 1) // skip ')'
    }
    if (cur < tokens.size && tokens[cur] == "-") {
        cur++
        val (v, next) = parseFactor(tokens, cur)
        return -v to next
    }
    val v = tokens[cur].toDouble()
    return v to (cur + 1)
}
