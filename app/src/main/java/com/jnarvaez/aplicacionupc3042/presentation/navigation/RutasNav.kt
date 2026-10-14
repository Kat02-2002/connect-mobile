package com.jnarvaez.aplicacionupc3042.presentation.navigation

object RutasNav {
    const val INICIO = "inicio"
    const val FORMULARIO = "form?id={id}"
    const val BUSCAR = "buscar"
    const val DETALLE = "detalle/{id}"

    fun getTitulo(ruta: String?): String{
        return when{
            ruta == INICIO -> "Catálogo Académico"
            ruta?.startsWith("form") == true -> "Formulario de Libro"
            ruta == BUSCAR -> "Buscar Libro"
            ruta?.startsWith("detalle") == true -> "Detalle del Libro"
            else -> ""
        }
    }
}