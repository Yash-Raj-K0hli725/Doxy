package com.cranox.doxy.DialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.cranox.doxy.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class SelectItemsDialog : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_items_dialog, container, false)
    }

    override fun getTheme(): Int = R.style.FullScreenBottomSheetDialog
}