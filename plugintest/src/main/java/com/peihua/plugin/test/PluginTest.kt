package com.peihua.plugin.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun PluginView() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(text = "Hello World plugin11111")
    }
}

@Composable
fun PluginView2(content: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(text = "Hello World plugin22222,content: $content")
    }
}