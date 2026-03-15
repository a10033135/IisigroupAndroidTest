package idv.fan.iisigroup.android.test.feature.exchangeRate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import idv.fan.iisigroup.android.test.R

private val buttonRows = listOf(
    listOf("1", "2", "3", "×"),
    listOf("4", "5", "6", "÷"),
    listOf("7", "8", "9", "-"),
    listOf("0", ".", "c", "+"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expression by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CalculatorDisplay(expression = expression, result = result)

            buttonRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { label ->
                        CalculatorButton(
                            label = label,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                handleCalculatorInput(
                                    label = label,
                                    expression = expression,
                                    result = result,
                                    onExpressionChange = { expression = it },
                                    onResultChange = { result = it },
                                )
                            },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CalculatorButton(
                    label = "=",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val eval = evaluateExpression(expression)
                        if (eval != null) {
                            expression = formatNumber(eval)
                            result = ""
                        }
                    },
                )
            }

            Button(
                onClick = {
                    val text = if (result.isNotEmpty()) result else expression
                    val amount = text.toDoubleOrNull() ?: evaluateExpression(expression) ?: 1.0
                    onConfirm(amount)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.exchange_rate_calculator_input))
            }
        }
    }
}

@Composable
private fun CalculatorDisplay(
    expression: String,
    result: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = expression.ifEmpty { "0" },
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
        )
        if (result.isNotEmpty()) {
            Text(
                text = "= $result",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CalculatorButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun handleCalculatorInput(
    label: String,
    expression: String,
    result: String,
    onExpressionChange: (String) -> Unit,
    onResultChange: (String) -> Unit,
) {
    when (label) {
        "c" -> {
            if (expression.isNotEmpty()) {
                onExpressionChange(expression.dropLast(1))
                onResultChange("")
            }
        }
        in listOf("×", "÷", "+", "-") -> {
            val op = when (label) {
                "×" -> "*"
                "÷" -> "/"
                else -> label
            }
            val base = if (result.isNotEmpty() && expression.isEmpty()) result else expression
            if (base.isNotEmpty() && base.last() !in listOf('+', '-', '*', '/')) {
                onExpressionChange(base + op)
                onResultChange("")
            }
        }
        "." -> {
            val parts = expression.split(Regex("[+\\-*/]"))
            val lastPart = parts.lastOrNull() ?: ""
            if (!lastPart.contains('.')) {
                val prefix = if (expression.isEmpty()) "0" else expression
                onExpressionChange("$prefix.")
                onResultChange("")
            }
        }
        else -> {
            val newExpr = expression + label
            onExpressionChange(newExpr)
            val eval = evaluateExpression(newExpr)
            onResultChange(if (eval != null) formatNumber(eval) else "")
        }
    }
}

private fun evaluateExpression(expression: String): Double? {
    if (expression.isEmpty()) return null
    return try {
        val normalized = expression.replace("×", "*").replace("÷", "/")
        val tokens = tokenize(normalized) ?: return null
        parseExpression(tokens)
    } catch (e: Exception) {
        null
    }
}

private fun tokenize(expression: String): List<String>? {
    val tokens = mutableListOf<String>()
    var i = 0
    while (i < expression.length) {
        val ch = expression[i]
        when {
            ch.isDigit() || ch == '.' -> {
                val start = i
                while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) i++
                tokens.add(expression.substring(start, i))
            }
            ch in listOf('+', '-', '*', '/') -> {
                tokens.add(ch.toString())
                i++
            }
            else -> return null
        }
    }
    return tokens
}

private fun parseExpression(tokens: List<String>): Double? {
    if (tokens.isEmpty()) return null
    val mutableTokens = tokens.toMutableList()

    // Process * and /
    var i = 1
    while (i < mutableTokens.size) {
        if (mutableTokens[i] == "*" || mutableTokens[i] == "/") {
            val left = mutableTokens[i - 1].toDoubleOrNull() ?: return null
            val right = mutableTokens[i + 1].toDoubleOrNull() ?: return null
            val result = if (mutableTokens[i] == "*") left * right else {
                if (right == 0.0) return null else left / right
            }
            mutableTokens[i - 1] = result.toString()
            mutableTokens.removeAt(i)
            mutableTokens.removeAt(i)
        } else {
            i += 2
        }
    }

    // Process + and -
    var result = mutableTokens[0].toDoubleOrNull() ?: return null
    i = 1
    while (i < mutableTokens.size) {
        val op = mutableTokens[i]
        val right = mutableTokens[i + 1].toDoubleOrNull() ?: return null
        result = if (op == "+") result + right else result - right
        i += 2
    }
    return result
}

private fun formatNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString()
    else "%.4f".format(value).trimEnd('0').trimEnd('.')
