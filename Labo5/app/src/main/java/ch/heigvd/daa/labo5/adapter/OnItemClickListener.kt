package ch.heigvd.daa.labo5.adapter

import java.net.URL

interface OnItemClickListener {
    fun onItemClick(position: Int, items:  List<URL>)
}