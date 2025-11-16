package com.cranox.doxy.fragments

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cranox.doxy.databinding.ImageBinding
import com.cranox.doxy.utils.PDFUtils

class HomeAdapter(private val onAction: (Int) -> Unit) :
    ListAdapter<Uri, HomeAdapter.ImageAdapter>(DIFF_UTIL) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageAdapter {
        return ImageAdapter(
            ImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ImageAdapter,
        position: Int
    ) {
        holder.initBinding(position)
    }

    inner class ImageAdapter(private val bind: ImageBinding) : RecyclerView.ViewHolder(bind.root) {
        fun initBinding(position: Int) {
            val cItem = getItem(position)
            bind.apply {
                root.setOnClickListener(null)
                if (cItem == DUMMY) {
                    shapeableImageView.setImageBitmap(null)
                    delete.visibility = View.GONE
                    root.setOnClickListener {
                        onAction.invoke(PICK_IMAGE)
                    }
                } else {
                    delete.setOnClickListener {
                        onAction.invoke(position)
                    }
                    //
                    val cr = root.context.contentResolver
                    val bitmap = PDFUtils.getBitmap(cr, cItem)
                    shapeableImageView.setImageBitmap(bitmap)
                    delete.visibility = View.VISIBLE
                }

            }
        }
    }

    companion object {
        const val PICK_IMAGE = -2
        val DUMMY = "DUMMY".toUri()
    }
}

private val DIFF_UTIL = object : DiffUtil.ItemCallback<Uri>() {
    override fun areItemsTheSame(
        oldItem: Uri,
        newItem: Uri
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: Uri,
        newItem: Uri
    ): Boolean {
        return false
    }
}