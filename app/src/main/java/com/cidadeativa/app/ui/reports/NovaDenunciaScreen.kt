package com.cidadeativa.app.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val AUDIO_NENHUM = 0
private const val AUDIO_GRAVANDO = 1
private const val AUDIO_GRAVADO = 2

private fun formatarDuracao(segundos: Int): String =
    "%d:%02d".format(segundos / 60, segundos % 60)

@Composable
fun NovaDenunciaScreen(
    viewModel: ReportViewModel,
    onVoltar: () -> Unit,
    onVerDenuncias: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()
    val verDenuncias by rememberUpdatedState(onVerDenuncias)
    val pendente by viewModel.pendente.collectAsStateWithLifecycle()

    var titulo by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf("") }
    var bairro by rememberSaveable { mutableStateOf("") }
    var endereco by rememberSaveable { mutableStateOf("") }
    var descricao by rememberSaveable { mutableStateOf("") }
    var fotoAnexada by rememberSaveable { mutableStateOf(false) }
    var videoAnexado by rememberSaveable { mutableStateOf(false) }
    var audioEstado by rememberSaveable { mutableStateOf(AUDIO_NENHUM) }
    var audioSegundos by rememberSaveable { mutableStateOf(0) }
    var localizacaoGps by rememberSaveable { mutableStateOf(false) }
    var proximoLocal by rememberSaveable { mutableStateOf(0) }
    var localizando by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.resultados.collect { resultado ->
            if (resultado.sucesso) {
                titulo = ""
                categoria = ""
                bairro = ""
                endereco = ""
                descricao = ""
                fotoAnexada = false
                videoAnexado = false
                audioEstado = AUDIO_NENHUM
                audioSegundos = 0
                localizacaoGps = false
            }
            val acao = snackbarHostState.showSnackbar(
                message = resultado.mensagem,
                actionLabel = if (resultado.sucesso) "Ver denúncias" else null,
                duration = SnackbarDuration.Long
            )
            if (acao == SnackbarResult.ActionPerformed) {
                verDenuncias()
            }
        }
    }

    LaunchedEffect(audioEstado) {
        if (audioEstado == AUDIO_GRAVANDO) {
            while (true) {
                delay(1000)
                audioSegundos++
            }
        }
    }

    pendente?.let { duplicidade ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelarEnvio() },
            title = { Text("Possível denúncia duplicada") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Já existe uma ocorrência semelhante registrada:")
                    Text(
                        text = "#${duplicidade.existente.id} ${duplicidade.existente.titulo}",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = duplicidade.existente.endereco,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${duplicidade.existente.confirmacoes} confirmação(ões) até agora",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.confirmarExistente() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Confirmar existente") }
                    OutlinedButton(
                        onClick = { viewModel.enviarMesmoAssim() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Enviar mesmo assim") }
                    TextButton(
                        onClick = { viewModel.cancelarEnvio() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cancelar") }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        topBar = { BarraSuperior("Nova Denúncia", onVoltar) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TituloSecao("Ocorrência")

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            CampoSelecao(
                rotulo = "Categoria",
                valor = categoria,
                opcoes = CATEGORIAS,
                dica = "A triagem por IA pode ajustar"
            ) { categoria = it }

            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            TituloSecao("Localização")

            OutlinedButton(
                onClick = {
                    if (!localizando) {
                        escopo.launch {
                            localizando = true
                            delay(1200)
                            val local = LOCAIS_SIMULADOS[proximoLocal % LOCAIS_SIMULADOS.size]
                            proximoLocal++
                            endereco = local.endereco
                            bairro = local.bairro
                            localizacaoGps = true
                            localizando = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (localizando) "Obtendo localização..." else "Usar minha localização (simulado)")
            }
            if (localizacaoGps) {
                Text(
                    text = "Localização obtida via GPS (simulado)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            CampoSelecao(
                rotulo = "Bairro",
                valor = bairro,
                opcoes = BAIRROS,
                dica = "Selecione o bairro"
            ) {
                bairro = it
                localizacaoGps = false
            }

            OutlinedTextField(
                value = endereco,
                onValueChange = {
                    endereco = it
                    localizacaoGps = false
                },
                label = { Text("Endereço") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            TituloSecao("Anexos (simulados)")

            if (fotoAnexada) {
                LinhaAnexo("foto_denuncia.jpg") { fotoAnexada = false }
            } else {
                OutlinedButton(
                    onClick = { fotoAnexada = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) { Text("Anexar foto") }
            }

            if (videoAnexado) {
                LinhaAnexo("video_denuncia.mp4") { videoAnexado = false }
            } else {
                OutlinedButton(
                    onClick = { videoAnexado = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) { Text("Anexar vídeo") }
            }

            when (audioEstado) {
                AUDIO_GRAVANDO -> Button(
                    onClick = { audioEstado = AUDIO_GRAVADO },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) { Text("Parar gravação - ${formatarDuracao(audioSegundos)}") }

                AUDIO_GRAVADO -> LinhaAnexo("audio_denuncia.m4a (${formatarDuracao(audioSegundos)})") {
                    audioEstado = AUDIO_NENHUM
                    audioSegundos = 0
                }

                else -> OutlinedButton(
                    onClick = {
                        audioSegundos = 0
                        audioEstado = AUDIO_GRAVANDO
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) { Text("Gravar áudio") }
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    val anexos = buildList<String> {
                        if (fotoAnexada) add("Foto")
                        if (videoAnexado) add("Vídeo")
                        if (audioEstado == AUDIO_GRAVADO) add("Áudio (${formatarDuracao(audioSegundos)})")
                    }
                    viewModel.adicionarDenuncia(
                        NovaDenuncia(
                            titulo = titulo,
                            descricao = descricao,
                            categoria = categoria,
                            endereco = endereco,
                            bairro = bairro,
                            anexos = anexos
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Enviar Denúncia")
            }
        }
    }
}

@Composable
private fun LinhaAnexo(nome: String, onRemover: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = nome,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onRemover) { Text("Remover") }
    }
}