package com.cidadeativa.app.ui.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private const val TODOS = "Todos"

@Composable
fun MapaGeralScreen(
    viewModel: ReportViewModel,
    onVoltar: () -> Unit,
    onDenunciaClick: (Int) -> Unit
) {
    val denuncias by viewModel.minhasDenuncias.collectAsStateWithLifecycle()
    var filtroBairro by rememberSaveable { mutableStateOf(TODOS) }
    var filtroSetor by rememberSaveable { mutableStateOf(TODOS) }

    val filtradas = denuncias.filter {
        (filtroBairro == TODOS || it.bairro == filtroBairro) &&
            (filtroSetor == TODOS || it.categoria == filtroSetor)
    }

    Scaffold(
        topBar = { BarraSuperior("Mapa Geral", onVoltar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Bairro",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FiltroChips(
                opcoes = listOf(TODOS) + BAIRROS,
                selecionado = filtroBairro,
                onSelecionar = { filtroBairro = it }
            )

            Text(
                text = "Setor responsável",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FiltroChips(
                opcoes = listOf(TODOS) + CATEGORIAS,
                selecionado = filtroSetor,
                onSelecionar = { filtroSetor = it }
            )

            MapaSimulado(denuncias = filtradas, onDenunciaClick = onDenunciaClick)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ItemLegenda(StatusDenuncia.EM_ABERTO)
                ItemLegenda(StatusDenuncia.EM_ANALISE)
                ItemLegenda(StatusDenuncia.RESOLVIDO)
            }

            Text(
                text = "${filtradas.size} ocorrência(s) exibida(s). Os dados dos autores não são exibidos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FiltroChips(
    opcoes: List<String>,
    selecionado: String,
    onSelecionar: (String) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opcoes.forEach { opcao ->
            FilterChip(
                selected = selecionado == opcao,
                onClick = { onSelecionar(opcao) },
                label = { Text(opcao) }
            )
        }
    }
}

@Composable
private fun MapaSimulado(denuncias: List<Denuncia>, onDenunciaClick: (Int) -> Unit) {
    val corTerra = Color(0xFF1B2A21)
    val corMar = Color(0xFF0E2A3A)
    val corVia = Color(0xFF2C3E33)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        val largura = maxWidth
        val altura = maxHeight

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = corTerra)
            drawRect(
                color = corMar,
                topLeft = Offset(size.width * 0.82f, 0f),
                size = Size(size.width * 0.18f, size.height)
            )
            val espessura = 10.dp.toPx()
            listOf(0.2f, 0.45f, 0.7f).forEach { fx ->
                drawLine(
                    color = corVia,
                    start = Offset(size.width * fx, 0f),
                    end = Offset(size.width * fx, size.height),
                    strokeWidth = espessura
                )
            }
            listOf(0.25f, 0.55f, 0.8f).forEach { fy ->
                drawLine(
                    color = corVia,
                    start = Offset(0f, size.height * fy),
                    end = Offset(size.width * 0.82f, size.height * fy),
                    strokeWidth = espessura
                )
            }
        }

        Text(
            text = "João Pessoa",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )
        Text(
            text = "Oceano\nAtlântico",
            fontSize = 10.sp,
            color = Color(0xFF4FC3F7),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp)
        )

        denuncias.forEach { denuncia ->
            val (fx, fy) = posicaoNoMapa(denuncia.id)
            Box(
                modifier = Modifier
                    .offset(x = largura * fx - 14.dp, y = altura * fy - 14.dp)
                    .size(28.dp)
                    .background(corDoStatus(denuncia.status), CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { onDenunciaClick(denuncia.id) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = denuncia.id.toString(),
                    color = Color(0xFF0D1511),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Posição determinística derivada do id, mantida na área de terra do mapa
private fun posicaoNoMapa(id: Int): Pair<Float, Float> {
    val fx = 0.08f + ((id * 37) % 64) / 100f
    val fy = 0.08f + ((id * 53) % 80) / 100f
    return fx to fy
}

@Composable
private fun ItemLegenda(status: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(corDoStatus(status), CircleShape)
        )
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}