package com.cidadeativa.app.ui.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DetalheDenunciaScreen(
    viewModel: ReportViewModel,
    denunciaId: Int,
    onVoltar: () -> Unit
) {
    val denuncias by viewModel.minhasDenuncias.collectAsStateWithLifecycle()
    val denuncia = denuncias.firstOrNull { it.id == denunciaId }

    Scaffold(
        topBar = { BarraSuperior("Detalhes da Denúncia", onVoltar) }
    ) { padding ->
        if (denuncia == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Denúncia não encontrada")
            }
            return@Scaffold
        }

        val etapas = listOf(
            "Denúncia registrada em ${denuncia.data}" to true,
            "Recebimento confirmado pelo órgão" to denuncia.recebidaPeloOrgao,
            "Em análise pelo órgão responsável" to (denuncia.status != StatusDenuncia.EM_ABERTO),
            "Problema resolvido" to (denuncia.status == StatusDenuncia.RESOLVIDO)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = denuncia.titulo,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(denuncia.status)
                if (denuncia.isDuplicada) {
                    DuplicadaBadge()
                }
            }
            TextoRecebimento(denuncia.recebidaPeloOrgao)

            Campo("Categoria", denuncia.categoria)
            Campo("Bairro", denuncia.bairro)
            Campo("Endereço", denuncia.endereco)
            Campo("Descrição", denuncia.descricao)
            Campo(
                "Anexos",
                if (denuncia.anexos.isEmpty()) "Nenhum" else denuncia.anexos.joinToString(", ")
            )
            Campo("Órgão responsável", orgaoResponsavel(denuncia.categoria))
            Campo("Confirmações de outros cidadãos", denuncia.confirmacoes.toString())

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TituloSecao("Parecer da IA")
                    Text(
                        text = "Classificada automaticamente como ${denuncia.categoria} " +
                            "a partir do conteúdo enviado e encaminhada ao órgão competente.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (denuncia.isDuplicada) {
                            "Foi encontrada uma ocorrência semelhante no mesmo endereço ou com " +
                                "título parecido. Esta denúncia foi marcada como possível duplicada."
                        } else {
                            "Nenhuma ocorrência semelhante foi encontrada nas proximidades."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            TituloSecao("Histórico de andamento")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                etapas.forEach { (texto, alcancada) ->
                    val cor = if (alcancada) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = if (alcancada) "●" else "○", color = cor)
                        Text(text = texto, color = cor)
                    }
                }
            }
        }
    }
}

@Composable
private fun Campo(rotulo: String, valor: String) {
    Column {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = valor, style = MaterialTheme.typography.bodyLarge)
    }
}