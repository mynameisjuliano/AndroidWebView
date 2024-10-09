package jry.androidwebview

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kevinnzou.web.LoadingState
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewNavigator
import com.kevinnzou.web.rememberWebViewState
import jry.androidwebview.ui.theme.AndroidWebViewTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        // TODO: make transparent navigation bar
        setContent {
            AndroidWebViewTheme {
                val webViewState = rememberWebViewState("jw.org")
                val webViewNavigator = rememberWebViewNavigator()


                var showGoAllowedSite by remember { mutableStateOf(false) }
                var siteInputState by remember { mutableStateOf("") }
                Scaffold (
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = {
                                Text("Small Top App Bar")
                            },
                            navigationIcon = {
                                if(webViewNavigator.canGoBack) {
                                    IconButton(onClick = {
                                        if(webViewNavigator.canGoBack)
                                            webViewNavigator.navigateBack()
                                    }) {
                                        Icon(Icons.AutoMirrored.Default.ArrowBack,
                                            "DUMMY: Back")
                                    }

                                }
                            },
                            actions = {
                                IconButton(onClick = { webViewNavigator.reload() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "DUMMY: Refresh")
                                }
                                if(webViewNavigator.canGoForward) {
                                    IconButton(onClick = { webViewNavigator.navigateForward() }) {
                                        Icon(
                                            Icons.AutoMirrored.Default.ArrowForward,
                                            contentDescription = "DUMMY: Forward"
                                        )
                                    }
                                }
                                IconButton(onClick = { showGoAllowedSite = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "DUMMY: Goto")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    val loadingState = webViewState.loadingState
                    if(loadingState is LoadingState.Loading) {
                        LinearProgressIndicator(
                            progress = { loadingState.progress },
                            modifier =  Modifier.fillMaxWidth()
                            )
                    }
                    WebView(state = webViewState,
                        navigator = webViewNavigator,
                        modifier = Modifier.padding(innerPadding),
                        onCreated = {
                            it.settings.javaScriptEnabled = true
                            it.settings.domStorageEnabled = true
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                it.settings.isAlgorithmicDarkeningAllowed = true
                            }
                        })

                    if(showGoAllowedSite) {
                        AlertDialog(
                            onDismissRequest = {
                                showGoAllowedSite = false
                            },
                            // title = { Text(text = "Title") },
                            text = {
                                Column {
                                    // TODO: Enlarge
                                    Row (verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "DUMMY: Go to site",
                                            fontSize = 22.sp,
                                            modifier = Modifier.padding(20.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.Info,
                                            contentDescription =  "DUMMY: Allowed sites",
                                            Modifier.size(18.dp))
                                    }
                                    // TODO: Disable
                                    OutlinedTextField(siteInputState, onValueChange = {
                                        siteInputState = it
                                    })
                                }
                            },
                            confirmButton = {
                                IconButton(onClick = {
                                    webViewNavigator.loadUrl(siteInputState)
                                    showGoAllowedSite = false
                                }) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "DUMMY: Confirm"
                                    )
                                }
                                //TextButton(onClick = { openDialog.value = false }) { Text("Confirm") }
                            },
                            dismissButton = {
                                IconButton(onClick = {showGoAllowedSite = false}) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "DUMMY: Close"
                                    )
                                }
                                //TextButton(onClick = { openDialog.value = false }) { Text("Dismiss") }
                            }
                        )
//                        GoToSiteDialog(siteInputState, onDismissRequest = {
//                            showGoAllowedSite = false
//                        }, onValueChange = { value ->
//                            siteInputState = value
//                        }) {
//                            webViewNavigator.loadUrl(siteInputState)
//                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun GoToSiteDialogPreview() {
    BasicAlertDialog(
        onDismissRequest = {

        }) {
        Card (
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 32.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column (modifier = Modifier.padding(32.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("DUMMY: Go to allowed site", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "DUMMY: Allowed sites",
                        Modifier.size(14.dp)
                    )
                }
                OutlinedTextField("", onValueChange = {

                })
                Row() {
                    // (Modifier.fillMaxWidth())
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "DUMMY: Confirm"
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "DUMMY: Exit"
                        )
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoToSiteDialog(value : String, onDismissRequest : () -> Unit,
                   onValueChange : (String) -> Unit,
                   onConfirm : () -> Unit) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest) {
            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("DUMMY: Go to allowed site")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Info,
                        contentDescription =  "DUMMY: Allowed sites",
                        Modifier.size(14.dp))
                }
                OutlinedTextField(value = value, onValueChange = onValueChange)
                Row() {
                    // (Modifier.fillMaxWidth())
                    IconButton(onClick = onConfirm) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "DUMMY: Confirm"
                        )
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "DUMMY: Exit"
                        )
                    }
                }
        }
    }
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
    AndroidWebViewTheme {
        Greeting("Android")
    }
}