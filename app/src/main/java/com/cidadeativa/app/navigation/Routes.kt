package com.cidadeativa.app.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val NOVA_DENUNCIA = "nova_denuncia"
    const val MINHAS_DENUNCIAS = "minhas_denuncias"
    const val MAPA = "mapa"

    const val ARG_ID = "id"
    const val DETALHE = "detalhe/{$ARG_ID}"

    fun detalhe(id: Int): String = "detalhe/$id"
}