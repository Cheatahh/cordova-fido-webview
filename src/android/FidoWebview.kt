package com.fkmit.fido

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

private fun WebView.configureAsBrowser() {
    @SuppressLint("SetJavaScriptEnabled")
    settings.javaScriptEnabled = true
    settings.userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36 Edg/92.0.902.78"
    settings.domStorageEnabled = true
    @Suppress("DEPRECATION")
    settings.databaseEnabled = true
    settings.loadsImagesAutomatically = true
}

private fun WebView.configureClientController(onPerformJSInjection: WebView.(url: String) -> Unit) {
    webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            onPerformJSInjection(url)
            super.onPageFinished(view, url)
        }
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            onPerformJSInjection(request.url.toString())
            return super.shouldInterceptRequest(view, request)
        }
    }
}

private fun WebView.configurePasskeySupport(): Boolean {
    if(!WebViewFeature.isFeatureSupported(WebViewFeature.WEB_AUTHENTICATION)) return false
    WebSettingsCompat.setWebAuthenticationSupport(settings, WebSettingsCompat.WEB_AUTHENTICATION_SUPPORT_FOR_APP)
    return (WebSettingsCompat.getWebAuthenticationSupport(settings) and WebSettingsCompat.WEB_AUTHENTICATION_SUPPORT_FOR_APP) != 0
}

class FidoWebview : ComponentActivity() {
    @SuppressLint("DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val targetUrl = requireNotNull(intent?.getStringExtra("targetUrl"))
        val injectJsCode = requireNotNull(intent?.getStringExtra("injectJsCode"))
        setContentView(resources.getIdentifier("fido_webview", "layout", packageName))
        findViewById<WebView>(resources.getIdentifier("fidoWebview", "id", packageName)).apply {
            CookieManager.getInstance().removeAllCookies {
                runOnUiThread ui@ {
                    configureAsBrowser()
                    addJavascriptInterface(FidoJsInterface(), "FidoJsInterface")
                    configureClientController { url ->
                        runOnUiThread {
                            evaluateJavascript(injectJsCode,null)
                        }
                    }
                    if(!configurePasskeySupport()) {
                        setContentView(resources.getIdentifier("fido_webview_unsupported", "layout", packageName))
                        return@ui
                    }
                    loadUrl(targetUrl)
                }
            }
        }
    }
    inner class FidoJsInterface {
        @JavascriptInterface
        fun getAssertion(navigatorOptions: JsonString) {

        }
    }
}