package com.peihua.composeplugin

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.peihua.composeplugin.ui.theme.ComposePluginTheme
import com.peihua.plugin.api.IPluginView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = DemoViewModel(this.application)
        setContent {
            ComposePluginTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        val loadApkSuccess = remember { mutableStateOf<IPluginView?>(null) }
                        Greeting("Android", modifier = Modifier.clickable {
                            viewModel.loadApk(this@MainActivity) {
                                Log.d("MainActivity", "loadApk: $it")
                                loadApkSuccess.value = it
                            }
                        })

                        if (loadApkSuccess.value != null) {
                            val pluginView1 = loadApkSuccess.value
                            Log.d("MainActivity", "444loadApk21212: $pluginView1")
                            if (pluginView1 != null) {
                                SimpleAndroidView(
                                    factory = {
                                        println("pluginView: >lllll33232>>>>$pluginView1")
                                        val result = pluginView1.pluginView(it, "ssssssssss")
                                        Log.d(
                                            "MainActivity",
                                            "44444pluginViewqq222222: $result"
                                        )
                                        result
                                    }
                                )
                                SimpleAndroidView {
                                    pluginView1.newPluginView(it)
                                }
                                SimpleAndroidView {
                                    pluginView1.pluginView.invoke(it, "999999999")
                                }
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