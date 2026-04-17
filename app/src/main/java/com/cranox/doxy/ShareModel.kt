package com.cranox.doxy

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class ShareModel(application: Application): AndroidViewModel(application) {
    private val context by lazy { application.applicationContext }


}