package com.cranox.doxy.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.cranox.doxy.R
import com.cranox.doxy.databinding.FragmentHomeBinding
import com.cranox.doxy.utils.PDFUtils
import com.cranox.doxy.utils.xToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class Home : Fragment() {
    private lateinit var imageContract: ActivityResultLauncher<String>
    private val images: MutableList<Uri> = mutableListOf()

    private val mAdapter: HomeAdapter by lazy {
        HomeAdapter { action ->
            when (action) {
                HomeAdapter.PICK_IMAGE -> imageContract.launch("image/*")
                else -> {
                    images.removeAt(action)
                    mAdapter.submitList(getImages())
                }
            }
        }
    }
    private lateinit var bind: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentHomeBinding.inflate(inflater, container, false)
        imageContract =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { _uri ->
                _uri.forEach { uri ->
                    val mimeType = context?.contentResolver?.getType(uri)
                    if (mimeType?.startsWith("image/") ?: false) {
                        images.add(uri)
                        mAdapter.submitList(getImages())

                        if (bind.mFlow.isVisible) bind.mFlow.visibility = View.GONE
                        bind.step1.visibility = View.VISIBLE
                    } else {
                        xToast(context, "Unsupported File formate")
                    }
                }
            }
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.apply {
            imagesPreview.apply {
                adapter = mAdapter
                layoutManager = GridLayoutManager(requireContext(), 2)
            }

            selectImage.setOnClickListener {
                imageContract.launch("image/*")
            }
            clear.setOnClickListener { clearImages() }
            convert.setOnClickListener {
                if (images.isEmpty()) return@setOnClickListener
                wait(true)
                val timeStamp = SimpleDateFormat("dd_MMM_yyyy", Locale.ENGLISH).format(Date())
                lifecycleScope.launch(Dispatchers.IO) {
                    PDFUtils.createPDF(
                        context,
                        images,
                        "Doxy_PDF_$timeStamp"
                    ) {
                        clearImages()
                        wait(false)
                    }
                }
            }
        }
        requestPermission()
    }

    @SuppressLint("SetTextI18n")
    private fun getImages(): List<Uri> {
        bind.total.text = "${images.size} image selected"
        val _images = mutableListOf<Uri>()
        _images.addAll(images)
        _images.add(HomeAdapter.DUMMY)
        return _images
    }

    private fun clearImages() {
        mAdapter.submitList(listOf())
        images.clear();
        bind.mFlow.visibility = View.VISIBLE
        bind.step1.visibility = View.GONE
    }

    private fun wait(wait: Boolean) {
        if (wait) {
            bind.convert.apply {
                setText(R.string.converting)
                setIconTintResource(R.color.mBlue)
            }
            bind.loader.visibility = View.VISIBLE
        } else {
            bind.convert.apply {
                setText(R.string.convert_to_pdf)
                setIconTintResource(R.color.white)
            }
            bind.loader.visibility = View.GONE
        }
        bind.convert.isEnabled = !wait
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val permission = ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            if (!permission) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    101
                )
            }
        }
    }
}