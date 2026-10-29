package com.cidadeativa.app.ui.reports

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object StatusDenuncia {
    const val EM_ABERTO = "Em Aberto"
    const val EM_ANALISE = "Em Análise"
    const val RESOLVIDO = "Resolvido"
}

val CATEGORIAS = listOf("Iluminação", "Pavimentação", "Limpeza Urbana", "Outros")

val BAIRROS = listOf(
    "Bairro dos Estados",
    "Bessa",
    "Cabo Branco",
    "Centro",
    "Mangabeira",
    "Manaíra",
    "Tambaú"
)

data class LocalSimulado(val endereco: String, val bairro: String)

// Localizações usadas na simulação do GPS, em sequência a cada captura
val LOCAIS_SIMULADOS = listOf(
    LocalSimulado("Av. Cabo Branco, 1500 - Cabo Branco", "Cabo Branco"),
    LocalSimulado("Rua Duque de Caxias, 350 - Centro", "Centro"),
    LocalSimulado("Av. Epitácio Pessoa, 1200 - Bairro dos Estados", "Bairro dos Estados"),
    LocalSimulado("Av. Flávio Ribeiro Coutinho, 700 - Manaíra", "Manaíra"),
    LocalSimulado("Av. Hilton Souto Maior, 400 - Mangabeira", "Mangabeira")
)

data class Denuncia(
    val id: Int,
    val titulo: String,
    val descricao: String,
    val categoria: String,
    val bairro: String,
    val endereco: String,
    val data: String,
    val status: String,
    val isDuplicada: Boolean = false,
    val anexos: List<String> = emptyList(),
    val confirmacoes: Int = 0,
    val recebidaPeloOrgao: Boolean = false
)

fun orgaoResponsavel(categoria: String): String = when (categoria) {
    "Iluminação" -> "Secretaria de Infraestrutura - Iluminação Pública"
    "Pavimentação" -> "Secretaria de Infraestrutura - Obras Viárias"
    "Limpeza Urbana" -> "Secretaria de Serviços Urbanos - Limpeza"
    else -> "Ouvidoria Geral do Município"
}

// Repositório em memória usado na simulação; os dados são perdidos ao fechar o app
object DenunciaRepository {

    private val _denuncias = MutableStateFlow(
        listOf(
            Denuncia(
                id = 3,
                titulo = "Acúmulo de lixo em Tambaú",
                descricao = "Lixo acumulado na calçada há uma semana, atraindo animais e causando mau cheiro.",
                categoria = "Limpeza Urbana",
                bairro = "Tambaú",
                endereco = "Av. Almirante Tamandaré, 80 - Tambaú",
                data = "08/10/2026",
                status = StatusDenuncia.RESOLVIDO,
                anexos = listOf("Foto"),
                confirmacoes = 4,
                recebidaPeloOrgao = true
            ),
            Denuncia(
                id = 2,
                titulo = "Buraco na Rua Duque de Caxias",
                descricao = "Buraco grande no asfalto em frente ao número 350, com risco para motos e carros.",
                categoria = "Pavimentação",
                bairro = "Centro",
                endereco = "Rua Duque de Caxias, 350 - Centro",
                data = "15/10/2026",
                status = StatusDenuncia.EM_ANALISE,
                anexos = listOf("Foto", "Vídeo"),
                confirmacoes = 2,
                recebidaPeloOrgao = true
            ),
            Denuncia(
                id = 1,
                titulo = "Poste apagado na Av. Epitácio Pessoa",
                descricao = "A luz do poste está apagada há dias e a rua fica totalmente escura à noite.",
                categoria = "Iluminação",
                bairro = "Bairro dos Estados",
                endereco = "Av. Epitácio Pessoa, 1200 - Bairro dos Estados",
                data = "22/10/2026",
                status = StatusDenuncia.EM_ABERTO,
                anexos = listOf("Foto"),
                recebidaPeloOrgao = true
            )
        )
    )

    val denuncias: StateFlow<List<Denuncia>> = _denuncias.asStateFlow()

    fun adicionar(denuncia: Denuncia) {
        _denuncias.update { listOf(denuncia) + it }
    }

    fun confirmar(id: Int) {
        atualizar(id) { it.copy(confirmacoes = it.confirmacoes + 1) }
    }

    fun marcarRecebida(id: Int) {
        atualizar(id) { it.copy(recebidaPeloOrgao = true) }
    }

    fun proximoId(): Int = (_denuncias.value.maxOfOrNull { it.id } ?: 0) + 1

    fun buscarPorId(id: Int): Denuncia? = _denuncias.value.firstOrNull { it.id == id }

    private fun atualizar(id: Int, transformacao: (Denuncia) -> Denuncia) {
        _denuncias.update { lista ->
            lista.map { if (it.id == id) transformacao(it) else it }
        }
    }
}