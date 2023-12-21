package ch.heigvd.daa.labo5.adapter

import java.net.URL

/**
 * Interface definition for a callback to be invoked when an item in a RecyclerView is clicked.
 * @author Timothée Van Hove, Léo Zmoos
 */
interface OnItemClickListener {
    fun onItemClick(position: Int, items:  List<URL>)
}