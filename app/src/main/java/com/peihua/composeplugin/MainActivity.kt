package com.peihua.composeplugin

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.peihua.composeplugin.ui.theme.ComposePluginTheme
import com.peihua.plugin.PluginManager
import com.peihua.plugin.PluginManager.Companion.PLUGIN_PATH
import com.peihua.plugin.api.IPluginView
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = DemoViewModel(this.application)
        setContent {
            ComposePluginTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        val loadApkSuccess = remember { mutableStateOf<IPluginView?>(null) }
                        Greeting("Android", modifier = Modifier.clickable {
                            viewModel.loadApk(this@MainActivity) {
                                Log.d("MainActivity", "loadApk: $it")
                                loadApkSuccess.value = it
                            }
                        })

                        if (loadApkSuccess.value != null) {
                            val pluginView1 = viewModel.mPluginView2
                            Log.d("MainActivity", "444loadApk21212: $pluginView1")
                            if (pluginView1 != null) {
                                SimpleAndroidView(
                                    factory = {
                                        Log.d("MainActivity", "rrrr44444pluginViewqq111111: $pluginView1")
                                        print("pluginView: >lllll33232>>>>$this")
                                        val result = pluginView1.pluginView(it,"223334")
                                        Log.d("MainActivity", "44444pluginViewqq222222: $result")
                                        result
//                                        TextView(this@MainActivity).apply {
//                                            text = "Hello World<><><><><><><?>"
//                                        }
                                    }
                                )
                            }
                        }
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