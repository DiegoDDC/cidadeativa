package com.cidadeativa.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class NovaDenuncia(
    val titulo: String,
    val descricao: String,
    val categoria: String,
    val endereco: String,
    val bairro: String,
    val anexos: List<String>
)

data class DuplicidadePendente(val dados: NovaDenuncia, val existente: Denuncia)

data class EnvioResultado(val mensagem: String, val sucesso: Boolean)

class ReportViewModel : ViewModel() {

    val minhasDenuncias: StateFlow<List<Denuncia>> = DenunciaRepository.denuncias

    private val _pendente = MutableStateFlow<DuplicidadePendente?>(null)
    val pendente: StateFlow<DuplicidadePendente?> = _pendente.asStateFlow()

    private val canalResultados = Channel<EnvioResultado>(Channel.BUFFERED)
    val resultados: Flow<EnvioResultado> = canalResultados.receiveAsFlow()

    private val palavrasChave = mapOf(
        "Iluminação" to listOf("luz", "poste", "lampada"),
        "Pavimentação" to listOf("buraco", "asfalto"),
        "Limpeza Urbana" to listOf("lixo", "entulho")
    )

    fun buscarPorId(id: Int): Denuncia? = DenunciaRepository.buscarPorId(id)

    // Valida os dados e consulta duplicidade; havendo semelhante, aguarda a decisão do usuário
    fun adicionarDenuncia(dados: NovaDenuncia) {
        if (dados.titulo.isBlank() || dados.descricao.isBlank() ||
            dados.endereco.isBlank() || dados.bairro.isBlank()
        ) {
            canalResultados.trySend(
                EnvioResultado("Preencha título, bairro, endereço e descrição", sucesso = false)
            )
            return
        }

        val semelhante = buscarSemelhante(dados.titulo, dados.endereco)
        if (semelhante != null) {
            _pendente.value = DuplicidadePendente(dados, semelhante)
            return
        }
        registrar(dados, original = null)
    }

    fun enviarMesmoAssim() {
        val pendente = _pendente.value ?: return
        _pendente.value = null
        registrar(pendente.dados, original = pendente.existente)
    }

    fun confirmarExistente() {
        val pendente = _pendente.value ?: return
        _pendente.value = null
        val id = pendente.existente.id
        DenunciaRepository.confirmar(id)
        val total = DenunciaRepository.buscarPorId(id)?.confirmacoes ?: 0
        canalResultados.trySend(
            EnvioResultado("Você confirmou a denúncia #$id. Total de confirmações: $total.", sucesso = true)
        )
    }

    fun cancelarEnvio() {
        _pendente.value = null
    }

    private fun registrar(dados: NovaDenuncia, original: Denuncia?) {
        // Triagem simulada: a categoria detectada nas palavras-chave prevalece sobre a escolhida
        val categoriaIa = classificarCategoria("${dados.titulo} ${dados.descricao}")
        val categoriaFinal = categoriaIa ?: dados.categoria.ifBlank { "Outros" }

        val denuncia = Denuncia(
            id = DenunciaRepository.proximoId(),
            titulo = dados.titulo.trim(),
            descricao = dados.descricao.trim(),
            categoria = categoriaFinal,
            bairro = dados.bairro,
            endereco = dados.endereco.trim(),
            data = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            status = StatusDenuncia.EM_ABERTO,
            isDuplicada = original != null,
            anexos = dados.anexos
        )
        DenunciaRepository.adicionar(denuncia)
        simularRecebimento(denuncia.id)

        val mensagem = buildString {
            append("Denúncia enviada. Triagem por IA: $categoriaFinal.")
            if (original != null) {
                append(" Possível duplicada da denúncia #${original.id}.")
            }
        }
        canalResultados.trySend(EnvioResultado(mensagem, sucesso = true))
    }

    // O órgão confirma o recebimento alguns segundos após o envio
    private fun simularRecebimento(id: Int) {
        viewModelScope.launch {
            delay(6000)
            DenunciaRepository.marcarRecebida(id)
        }
    }

    private fun classificarCategoria(texto: String): String? {
        val normalizado = normalizar(texto)
        return palavrasChave.entries
            .firstOrNull { entrada -> entrada.value.any { normalizado.contains(it) } }
            ?.key
    }

    private fun buscarSemelhante(titulo: String, endereco: String): Denuncia? {
        val enderecoNormalizado = normalizar(endereco)
        val tituloNormalizado = normalizar(titulo)
        return DenunciaRepository.denuncias.value.firstOrNull {
            normalizar(it.endereco) == enderecoNormalizado ||
                titulosSimilares(normalizar(it.titulo), tituloNormalizado)
        }
    }

    // Títulos são similares quando iguais ou quando compartilham ao menos duas palavras relevantes
    private fun titulosSimilares(a: String, b: String): Boolean {
        if (a == b) return true
        val palavrasA = a.split(" ").filter { it.length > 3 }.toSet()
        val palavrasB = b.split(" ").filter { it.length > 3 }.toSet()
        return palavrasA.intersect(palavrasB).size >= 2
    }

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto.lowercase().trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
}