package jry.androidwebview

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebResourceRequest
import android.webkit.WebView.HitTestResult
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kevinnzou.web.AccompanistWebViewClient
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


                var showGoAllowedSite by remember { mutableStateOf(false) }
                var siteInputState by remember { mutableStateOf("") }
                var showExitDialog by remember { mutableStateOf(false) }
                
                var topBarMenuExpanded by remember { mutableStateOf(false) }
                var hitUrl by remember { mutableStateOf("") }
                var showHitUrlAnchor by remember { mutableStateOf(false) }
                var showHitImgAnchor by remember { mutableStateOf(false) }


                var initialLink by remember { mutableStateOf(getString(R.string.default_home_page)) }

                if (intent.action == Intent.ACTION_SEND) {
                    initialLink = intent.getStringExtra(Intent.EXTRA_TEXT)?: ""
                } else if (intent.action == Intent.ACTION_VIEW) {
                    initialLink = intent.dataString?: ""
                }

                val webViewState = rememberWebViewState(initialLink)
                val webViewNavigator = rememberWebViewNavigator()
                var lastPageTitle by remember { mutableStateOf(getString(R.string.default_page_title)) }

                if(webViewState.pageTitle != null) {
                    lastPageTitle = webViewState.pageTitle!!
                }

                Scaffold (
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = {
                                Text(
                                    lastPageTitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            navigationIcon = {
                                if(webViewNavigator.canGoBack) {
                                    IconButton(onClick = {
                                        if(webViewNavigator.canGoBack)
                                            webViewNavigator.navigateBack()
                                    }) {
                                        Icon(Icons.AutoMirrored.Default.ArrowBack,
                                            contentDescription = getString(R.string.top_bar_back)
                                        )
                                    }

                                }
                            },
                            actions = {

                                if (webViewNavigator.canGoForward) {
                                    IconButton(onClick = { webViewNavigator.navigateForward() }) {
                                        Icon(
                                            Icons.AutoMirrored.Default.ArrowForward,
                                            contentDescription = getString(R.string.top_bar_forward)
                                        )
                                    }
                                }

                                IconButton(onClick = { webViewNavigator.reload() }) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = getString(R.string.option_refresh)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        siteInputState = webViewState.lastLoadedUrl ?: ""
                                        showGoAllowedSite = true
                                    }) {
                                    Icon(
                                        Icons.AutoMirrored.Default.OpenInNew,
                                        contentDescription = getString(R.string.option_go_to)
                                    )
                                }


                                Box {
                                    IconButton(onClick = { topBarMenuExpanded = true }) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = getString(R.string.top_bar_forward)
                                        )
                                    }

                                    DropdownMenu(expanded = topBarMenuExpanded,
                                        onDismissRequest = { topBarMenuExpanded = false }) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(getString(R.string.option_share))
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Share,
                                                    contentDescription = getString(R.string.option_share)
                                                )
                                            },
                                            onClick = {
                                                shareLink(webViewState.lastLoadedUrl ?: "")
                                                topBarMenuExpanded = false
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = {
                                                Text(getString(R.string.option_exit))
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = getString(R.string.option_exit)
                                                )
                                            },
                                            onClick = {
                                                showExitDialog = true
                                                topBarMenuExpanded = false
                                            }
                                        )

                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    var refreshing by remember { mutableStateOf(false) }

                    PullToRefreshBox (
                        isRefreshing = refreshing,
                        onRefresh = {
                            refreshing = true
                            webViewNavigator.reload()
                            refreshing = false
                        }
                    ) {
                        Column {
                            val loadingState = webViewState.loadingState
                            if (loadingState is LoadingState.Loading) {
                                LinearProgressIndicator(
                                    progress = { loadingState.progress / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.primary
                                )
                            }
                            WebView(state = webViewState,
                                navigator = webViewNavigator,
                                modifier = Modifier.padding(innerPadding)
                                    .fillMaxSize(),
                                onCreated = { webView ->
                                    webView.settings.javaScriptEnabled = true
                                    webView.settings.domStorageEnabled = true
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        webView.settings.isAlgorithmicDarkeningAllowed = true
                                    }

                                    webView.setOnLongClickListener {
                                        hitUrl = webView.hitTestResult.extra?: ""
                                        if(webView.hitTestResult.type == HitTestResult.SRC_ANCHOR_TYPE) {
                                            showHitUrlAnchor = true
                                        } else if (webView.hitTestResult.type == HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                                            showHitImgAnchor = true
                                        }

                                        true
                                    }

                                    webView.setDownloadListener { url, _, _, _, _ ->
                                        downloadInUrl(url?: "")
                                    }
                                },
                                client = remember {
                                    object : AccompanistWebViewClient() {

                                        override fun shouldOverrideUrlLoading(
                                            webView: WebView,
                                            request: WebResourceRequest
                                        ): Boolean { // Toast.makeText(this@MainActivity, "Triggered", Toast.LENGTH_LONG).show()
                                            return false
                                        }

                                    }
                                }

                            )
                        }
                    }

                    if(showHitUrlAnchor) {
                        BasicPopupOptionDialog(
                            hitUrl,
                            onDismissRequest = { showHitUrlAnchor = false }) {
                            PopupDialogOption(
                                Icons.AutoMirrored.Default.OpenInNew,
                                getString(R.string.link_open)
                            ) {
                                webViewNavigator.loadUrl(hitUrl)
                                showHitUrlAnchor = false
                            }
                            PopupDialogOption(
                                Icons.Default.ContentCopy,
                                getString(R.string.link_copy)
                            ) {
                                val clipboardManager =
                                    getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboardManager.setPrimaryClip(
                                    ClipData(
                                        "LINK",
                                        arrayOf<String>(),
                                        ClipData.Item(hitUrl)
                                    )
                                )

                                Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.link_copied),
                                    Toast.LENGTH_LONG
                                ).show()
                                showHitUrlAnchor = false
                            }
                            PopupDialogOption(
                                Icons.Default.Share,
                                getString(R.string.link_share)
                            ) {
                                shareLink(hitUrl)
                                showHitUrlAnchor = false
                            }
                            PopupDialogOption(
                                Icons.Default.OpenInBrowser,
                                getString(R.string.link_open_other)
                            ) {
                                val openIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_VIEW
                                    setData(Uri.parse(hitUrl))
                                }

                                startActivity(openIntent)
                                showHitUrlAnchor = false
                            }
                        }
                    }

                    if(showHitImgAnchor) {
                        BasicPopupOptionDialog(
                            getString(R.string.link_image).format(hitUrl),
                            onDismissRequest = { showHitImgAnchor = false }) {
                            PopupDialogOption(
                                Icons.AutoMirrored.Default.OpenInNew,
                                getString(R.string.link_open_image)
                            ) {
                                webViewNavigator.loadUrl(hitUrl)
                                showHitImgAnchor = false
                            }
                            PopupDialogOption(
                                Icons.Default.Save,
                                getString(R.string.link_save_image)
                            ) {
                                downloadInUrl(hitUrl)
                                showHitImgAnchor = false
                            }
                        }
                    }
                    if(showExitDialog) {
                        AlertDialog(
                            onDismissRequest = { showExitDialog = false },
                            title = { Text(text = getString(R.string.dialog_exit_title)) },
                            text = { Text(text = getString(R.string.dialog_exit_summary)) },
                            confirmButton = { TextButton(onClick = { finish() }) { Text(getString(R.string.dialog_yes) ) } },
                            dismissButton = { TextButton(onClick = { showExitDialog = false }) { Text(getString(R.string.dialog_no) ) } }
                        )
                    }
                    if(showGoAllowedSite) {
                        AlertDialog(
                            onDismissRequest = {
                                showGoAllowedSite = false
                            },
                            text = {
                                Column {
                                    // TODO: Enlarge
                                    Row (verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = getString(R.string.dialog_go_to_header),
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
                                    siteInputState = ""
                                    showGoAllowedSite = false
                                }) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = getString(R.string.dialog_confirm)
                                    )
                                }
                            },
                            dismissButton = {
                                IconButton(onClick = {showGoAllowedSite = false}) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = getString(R.string.dialog_close)
                                    )
                                }

                            }
                        )
                    }
                }
            }
        }
    }

    private fun downloadInUrl(url : String) {
        val downloadManager =
            getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(
            DownloadManager.Request(
                Uri.parse(url)
            )
        )
        /* TEMPORARY: TODO: Implement a Compose equivalent */

        Toast.makeText(
            this,
            getString(R.string.download_message).format(
                url.substring(url.lastIndexOf('/') + 1)
            ),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun shareLink(url : String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }
}

@Composable
fun PopupDialogOption (icon : ImageVector, text : String, onClick : () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.padding(4.dp)
            .fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = text
        )
        Spacer(Modifier.width(4.dp))
        Text(text, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun PopupDialogOption (icon : ImageBitmap, text : String, onClick : () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.padding(4.dp)
            .fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = text
        )
        Spacer(Modifier.width(4.dp))
        Text(text, modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicPopupOptionDialog(header : String, onDismissRequest : () -> Unit, content : @Composable ColumnScope.() -> Unit) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                header,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp, 32.dp, 32.dp, 8.dp)
            )
            Column(modifier = Modifier.padding(12.dp), content = content)
        }
    }
}

