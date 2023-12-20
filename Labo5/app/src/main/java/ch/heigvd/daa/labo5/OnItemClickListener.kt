package ch.heigvd.daa.labo5

import java.net.URL

interface OnItemClickListener {
    fun onItemClick(position: Int, items:  List<URL>)
}