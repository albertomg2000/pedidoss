package com.example.recyclerviewhorizontal.Clases

import com.google.android.gms.common.util.CollectionUtils.listOf

class Pedido(
    var id: String = "",
    var productos: List<String> = listOf(),
    var usuarioId: String = ""
)