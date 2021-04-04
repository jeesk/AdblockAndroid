package io.github.edsuns.adfilter.script

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.anthonycr.mezzanine.FileStream
import com.anthonycr.mezzanine.MezzanineGenerator
import io.github.edsuns.adfilter.AbstractDetector
import timber.log.Timber

/**
 * Created by Edsuns@qq.com on 2021/4/3.
 */
class Scriptlet internal constructor(private val detector: AbstractDetector) {

    // due to there is a limit of file size, split it into 3 files
    @FileStream("src/main/js/scriptlets/scriptlets.js-1")
    interface Scriptlets1 {
        fun js(): String
    }

    @FileStream("src/main/js/scriptlets/scriptlets.js-2")
    interface Scriptlets2 {
        fun js(): String
    }

    @FileStream("src/main/js/scriptlets/scriptlets.js-3")
    interface Scriptlets3 {
        fun js(): String
    }

    @FileStream("src/main/js/scriptlets_inject.js")
    interface ScriptletsInjection {
        fun js(): String
    }

    private val scriptletsJS: String by lazy {
        var js = MezzanineGenerator.Scriptlets1().js()
        js += MezzanineGenerator.Scriptlets2().js()
        js += MezzanineGenerator.Scriptlets3().js()
        js += ScriptInjection.parseScript(this, MezzanineGenerator.ScriptletsInjection().js())
        js
    }

    fun perform(webView: WebView?, url: String?) {
        webView?.evaluateJavascript(scriptletsJS, null)
        Timber.v("Evaluated Scriptlets Javascript for $url")
    }

    @JavascriptInterface
    fun getScriptlets(documentUrl: String): String {
        val list = detector.getScriptlets(documentUrl)
        val json = list.toScriptletsJSON()
        Timber.v("offer scriptlets: $json")
        return json
    }

    private fun Collection<String>.toScriptletsJSON(): String {
        val builder = StringBuilder()
        for (str in this) {
            if (builder.isNotEmpty()) {
                builder.append(',')
            }
            builder.append('[').append(str).append(']')
        }
        builder.insert(0, '[')
        builder.append(']')
        return builder.toString().replace('\'', '"')// only allow double quotes
    }
}