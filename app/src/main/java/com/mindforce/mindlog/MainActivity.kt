package com.mindforce.mindlog

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mindforce.mindlog.ui.navigation.MindLogNavGraph
import com.mindforce.mindlog.ui.theme.MindLogTheme
import com.mindforce.mindlog.ui.theme.MindWhite

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as MindLogApp

        setContent {
            MindLogTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MindWhite) {
                    MindLogNavGraph(app = app)
                }
            }
        }
    }
}
