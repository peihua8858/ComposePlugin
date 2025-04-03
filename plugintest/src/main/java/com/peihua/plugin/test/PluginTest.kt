package com.peihua.plugin.test

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun PluginView() {
    println("pluginView: <<><><><><><><><>4444444")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = "Hello World plugin11111")
        Row {
            Image(
                painter = painterResource(id = R.mipmap.logo),
                contentDescription = null
            )
            Text(text = "Hello World plugin11111")
        }
    }
}

@Composable
fun PluginView2(content: String) {
    println("pluginView: <<><><><><><><><>555555555")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Row {
            Image(
                painter = painterResource(id = R.mipmap.logo),
                contentDescription = null
            )
            Text(text = "Hello World plugin22222,content: $content")
        }
    }
}