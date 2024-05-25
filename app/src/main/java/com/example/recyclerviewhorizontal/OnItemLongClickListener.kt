package com.example.griview

import android.view.View
interface OnItemLongClickListener {
    fun onItemLongClick(view: View, position: Int): Boolean
}