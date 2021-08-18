package com.example.mywebbrowser

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.browse
import org.jetbrains.anko.email
import org.jetbrains.anko.sendSMS
import org.jetbrains.anko.share

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 웹뷰 기본 설정
        webView.apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
        }

        webView.loadUrl("https://www.google.com/?&bih=1292&biw=1232&hl=ko")

        urlEditText.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                webView.loadUrl(urlEditText.text.toString())
                true
            } else false
        }

        registerForContextMenu(webView)
    }

    // 뒤로가기 동작 재정의
    override fun onBackPressed() {
        if (webView.canGoBack())
            webView.goBack()
        else super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu1, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.action_google, R.id.action_home -> {
                webView.loadUrl("https://www.google.com/?&bih=1292&biw=1232&hl=ko")
            }
            R.id.action_naver -> {
                webView.loadUrl("https://m.naver.com/")
            }
            R.id.action_daum -> {
                webView.loadUrl("https://m.daum.net/")
            }
            R.id.action_call -> {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:031-123-4567")
                if (intent.resolveActivity(packageManager) != null)
                    startActivity(intent)
            }
            R.id.aciton_send_text -> {
                sendSMS("031-123-4567", webView.url!!)
            }
            R.id.action_email -> {
                email("hanyes2063@gmail.com", "안드로이드 테스트", webView.url!!)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context1, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.action_share -> {
                share(webView.url!!)
            }
            R.id.action_browser -> {
                browse(webView.url!!)
            }
        }
        return super.onContextItemSelected(item)
    }
}