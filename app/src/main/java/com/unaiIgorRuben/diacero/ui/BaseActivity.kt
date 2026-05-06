package com.unaiIgorRuben.diacero.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.unaiIgorRuben.diacero.util.FontResizer

abstract class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(FontResizer.wrap(newBase))
    }
}
