package web.financeassistant.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

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

    override fun onBackPressed() {

        if (webView.canGoBack()) {

            webView.goBack()

        } else {

            super.onBackPressed()
        }
    }
}