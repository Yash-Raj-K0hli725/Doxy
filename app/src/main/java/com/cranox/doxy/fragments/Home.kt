package com.cranox.doxy.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.cranox.doxy.DialogFragments.FileSelectionMethodDialog
import com.cranox.doxy.R
import com.cranox.doxy.databinding.FragmentHomeBinding


class Home : Fragment() {
    private lateinit var bind:FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false)
        bind.lifecycleOwner = viewLifecycleOwner
        bind.home = this
//        bind.home = this
        // Inflate the layout for this fragment
        return bind.root
    }

    fun onAddCardClickListener(){
        FileSelectionMethodDialog().show(childFragmentManager,"FileSelection")
    }
}