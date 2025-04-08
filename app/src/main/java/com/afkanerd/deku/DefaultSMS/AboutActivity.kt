package com.afkanerd.deku.DefaultSMS

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.afkanerd.deku.MainActivity

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val toolbar = findViewById<Toolbar?>(R.id.about_toolbar)
        setSupportActionBar(toolbar)

        val ab = supportActionBar
        ab!!.title = getString(R.string.about_deku)
        ab.setDisplayHomeAsUpEnabled(true)
        ab.setHomeButtonEnabled(true)

        setVersion()

        setClickListeners()
    }

    private fun setVersion() {
        val textView = findViewById<TextView>(R.id.about_version_text)
        textView.text = BuildConfig.VERSION_NAME
    }

    private fun setClickListeners() {
        val textView = findViewById<TextView>(R.id.about_github_link)
        textView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val url = getString(R.string.about_deku_github_url)
                val shareIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(shareIntent)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
            startActivity(
                Intent(applicationContext, MainActivity::class.java).apply {
                    setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME)
                }
            )
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}