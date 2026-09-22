package web.financeassistant.app

import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private val splashDuration = 1500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
                R.layout.activity_splash
        )

        Handler(
                Looper.getMainLooper()
        ).postDelayed({

            setContentView(
                    R.layout.activity_main
            )

            // =========================================
            // ACTION BAR
            // =========================================

            supportActionBar?.apply {

                title =
                        "Finance Assistant"

                setDisplayShowHomeEnabled(
                        true
                )

                setDisplayUseLogoEnabled(
                        true
                )

                setLogo(
                        R.mipmap.ic_launcher
                )
            }

            initializeWebView()

        }, splashDuration)
    }

    private fun initializeWebView() {

        swipeRefresh =
                findViewById(
                        R.id.swipeRefresh
                )

        webView =
                findViewById(
                        R.id.webView
                )

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.setSupportMultipleWindows(false)
        webView.settings.cacheMode =
                WebSettings.LOAD_DEFAULT

        val cookieManager =
                CookieManager.getInstance()

        cookieManager.setAcceptCookie(true)

        cookieManager.setAcceptThirdPartyCookies(
                webView,
                true
        )

        // =========================================
        // ANDROID EXPORT PNG BRIDGE
        // =========================================

        webView.addJavascriptInterface(
                AndroidExportBridge(),
                "AndroidExport"
        )

        // =========================================
        // WEBVIEW CLIENT
        // =========================================

        webView.webViewClient =
                object : WebViewClient() {

                    override fun onPageFinished(
                            view: WebView?,
                            url: String?
                    ) {
                        super.onPageFinished(
                                view,
                                url
                        )

                        swipeRefresh.isRefreshing =
                                false
                    }
                }

        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }

        webView.loadUrl(
                "https://financeassistant.web.id/pages/"
        )
    }

    // =============================================
    // ANDROID EXPORT PNG BRIDGE
    // =============================================

    inner class AndroidExportBridge {

        private var fileName =
                "rincian-gaji.png"

        private val pngData =
                ByteArrayOutputStream()

        @JavascriptInterface
        fun startPngExport(
                name: String
        ) {

            fileName =
                    if (
                            name.endsWith(
                                    ".png",
                                    ignoreCase = true
                            )
                    ) {
                        name
                    } else {
                        "$name.png"
                    }

            pngData.reset()
        }

        @JavascriptInterface
        fun appendPngChunk(
                chunk: String
        ) {

            try {

                val bytes =
                        Base64.decode(
                                chunk,
                                Base64.DEFAULT
                        )

                pngData.write(
                        bytes
                )

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }

        @JavascriptInterface
        fun finishPngExport() {

            try {

                val imageBytes =
                        pngData.toByteArray()

                val values =
                        ContentValues().apply {

                            put(
                                    MediaStore.Images.Media.DISPLAY_NAME,
                                    fileName
                            )

                            put(
                                    MediaStore.Images.Media.MIME_TYPE,
                                    "image/png"
                            )

                            put(
                                    MediaStore.Images.Media.RELATIVE_PATH,
                                    "Pictures/Finance Assistant"
                            )
                        }

                val resolver =
                        contentResolver

                val imageUri =
                        resolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                values
                        )

                if (imageUri == null) {

                    showExportMessage(
                            "Gagal membuat file PNG"
                    )

                    return
                }

                resolver.openOutputStream(
                        imageUri
                ).use { outputStream ->

                    outputStream?.write(
                            imageBytes
                    )
                }

                showExportMessage(
                        "PNG berhasil disimpan"
                )

            } catch (e: Exception) {

                e.printStackTrace()

                showExportMessage(
                        "Export PNG gagal"
                )

            } finally {

                pngData.reset()
            }
        }

        @JavascriptInterface
        fun cancelPngExport() {

            pngData.reset()
        }

        private fun showExportMessage(
                message: String
        ) {

            runOnUiThread {

                Toast.makeText(
                        this@MainActivity,
                        message,
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onBackPressed() {

        if (webView.canGoBack()) {

            webView.goBack()

        } else {

            super.onBackPressed()
        }
    }
}