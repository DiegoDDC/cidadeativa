package com.cidadeativa.app.ui.reports

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth

fun corDoStatus(status: String): Color = when (status) {
    StatusDenuncia.EM_ABERTO -> Color(0xFFFFB74D)
    StatusDenuncia.EM_ANALISE -> Color(0xFF4FC3F7)
    StatusDenuncia.RESOLVIDO -> Color(0xFF66BB6A)
    else -> Color(0xFF9E9E9E)
}

@Composable
fun StatusBadge(status: String) {
    val (fundo, texto) = when (status) {
        StatusDenuncia.EM_ABERTO -> Color(0xFF3E2A12) to Color(0xFFFFB74D)
        StatusDenuncia.EM_ANALISE -> Color(0xFF12303F) to Color(0xFF4FC3F7)
        StatusDenuncia.RESOLVIDO -> Color(0xFF1B3A24) to Color(0xFF81C784)
        else -> Color(0xFF2A2A2A) to Color(0xFFBDBDBD)
    }
    Etiqueta(status, fundo, texto)
}

@Composable
fun DuplicadaBadge() {
    Etiqueta("Possível Duplicada", Color(0xFF3E1A1A), Color(0xFFEF9A9A))
}

@Composable
fun TextoRecebimento(recebida: Boolean) {
    Text(
        text = if (recebida) "Recebimento confirmado pelo órgão" else "Aguardando confirmação do órgão",
        style = MaterialTheme.typography.bodySmall,
        color = if (recebida) Color(0xFF81C784) else Color(0xFFFFB74D)
    )
}

@Composable
fun TituloSecao(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun Etiqueta(texto: String, fundo: Color, corTexto: Color) {
    Surface(color = fundo, shape = RoundedCornerShape(50)) {
        Text(
            text = texto,
            color = corTexto,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(titulo: String, onVoltar: () -> Unit) {
    TopAppBar(
        title = { Text(titulo) },
        navigationIcon = {
            TextButton(onClick = onVoltar) { Text("Voltar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoSelecao(
    rotulo: String,
    valor: String,
    opcoes: List<String>,
    dica: String = "",
    onSelecionar: (String) -> Unit
) {
    var aberto by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = aberto,
        onExpandedChange = { aberto = it }
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            label = { Text(rotulo) },
            placeholder = { Text(dica) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aberto) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = aberto,
            onDismissRequest = { aberto = false }
        ) {
            opcoes.forEach { opcao ->
                DropdownMenuItem(
                    text = { Text(opcao) },
                    onClick = {
                        onSelecionar(opcao)
                        aberto = false
                    }
                )
            }
        }
    }
}