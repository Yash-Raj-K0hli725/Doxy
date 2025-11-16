package com.cranox.doxy.utils

import android.content.Context
import android.widget.Toast

fun xToast(ctx: Context?, msg: String) {
    ctx?.let {
        Toast.makeText(it, msg, Toast.LENGTH_SHORT).show()
    }
}