package com.peihua.plugin.test

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.peihua.plugin.test.theme.ComposePluginTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val pluginView = PluginViewImpl()
        setContent {
            ComposePluginTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Greeting("Android", modifier = Modifier.clickable {
                        })

                        Log.d("MainActivity", "444loadApk21212: $pluginView")
                        SimpleAndroidView(
                            factory = {
                                Log.d(
                                    "MainActivity",
                                    "rrrr44444pluginViewqq111111: $pluginView"
                                )
                                println("pluginView: >lllll33232>>>>")
                                val result = pluginView.pluginView(it)
                                Log.d(
                                    "MainActivity",
                                    "44444pluginViewqq222222: $result"
                                )
                                result
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun <T : View> SimpleAndroidView(factory: (Context) -> T) {
    AndroidView(
        factory = { context -> factory(context) },
        modifier = Modifier.wrapContentSize()
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComposePluginTheme {
        Greeting("Android")
    }
}