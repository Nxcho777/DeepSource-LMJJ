package cl.duoc.deepsource.ui.component

import cl.duoc.deepsource.R

/** Índices persistidos: 0..3 conservan los iconos originales. */
object ProjectIcons {
    val resources: List<Int> = listOf(
        R.drawable.ic_icono_1,
        R.drawable.ic_icono_2,
        R.drawable.ic_icono_3,
        R.drawable.ic_icono_4,
        R.drawable.ic_icono_5,
        R.drawable.ic_icono_6,
        R.drawable.ic_icono_7,
        R.drawable.ic_icono_8,
        R.drawable.ic_icono_9,
        R.drawable.ic_icono_10,
        R.drawable.ic_icono_11,
        R.drawable.ic_icono_12,
        R.drawable.ic_icono_13,
        R.drawable.ic_icono_14,
        R.drawable.ic_icono_15,
        R.drawable.ic_icono_16
    )

    fun resource(index: Int): Int = resources.getOrElse(index) { resources.first() }
}
