package com.cidadeativa.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cidadeativa.app.R

@Composable
fun HomeScreen(
    nomeUsuario: String,
    totalDenuncias: Int,
    aguardandoRecebimento: Int,
    onNewReport: () -> Unit,
    onMyReports: () -> Unit,
    onMap: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_logo_pin),
                contentDescription = null,
                modifier = Modifier.size(width = 42.dp, height = 56.dp)
            )
            Column {
                Text(
                    text = "Cidade Ativa",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (nomeUsuario.isBlank()) "Bem-vindo" else "Olá, $nomeUsuario",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Resumo(
                valor = totalDenuncias,
                rotulo = "Denúncias",
                modifier = Modifier.weight(1f)
            )
            Resumo(
                valor = aguardandoRecebimento,
                rotulo = "Aguardando órgão",
                modifier = Modifier.weight(1f)
            )
        }

        AcaoCard(
            titulo = "Nova Denúncia",
            descricao = "Registre um problema urbano com foto, vídeo ou áudio",
            destaque = true,
            onClick = onNewReport
        )
        AcaoCard(
            titulo = "Minhas Denúncias",
            descricao = "Acompanhe o status e a confirmação de recebimento",
            destaque = false,
            onClick = onMyReports
        )
        AcaoCard(
            titulo = "Mapa Geral",
            descricao = "Veja as ocorrências da cidade por bairro e setor",
            destaque = false,
            onClick = onMap
        )

        TextButton(onClick = onLogout) { Text("Sair") }
    }
}

@Composable
private fun Resumo(valor: Int, rotulo: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = rotulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AcaoCard(
    titulo: String,
    descricao: String,
    destaque: Boolean,
    onClick: () -> Unit
) {
    val corTitulo = if (destaque) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val corDescricao = if (destaque) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (destaque) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        border = BorderStroke(
            1.dp,
            if (destaque) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = corTitulo
            )
            Text(
                text = descricao,
                style = MaterialTheme.typography.bodySmall,
                color = corDescricao
            )
        }
    }
}