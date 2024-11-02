package com.afkanerd.deku.DefaultSMS

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.button.MaterialButton

class DefaultCheckActivity : AppCompatActivity() {
    val getDefaultPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            println("Moving to bring up activities")
            if (result.resultCode == RESULT_OK) {
                val sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(applicationContext)
                sharedPreferences.edit()
                    .putBoolean(getString(R.string.configs_load_natives), true)
                    .apply()
                startUserActivities()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default_check)

        val materialButton = findViewById<MaterialButton>(R.id.default_check_make_default_btn)
        materialButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                makeDefault()
            }
        })

        val defaultName = Telephony.Sms.getDefaultSmsPackage(applicationContext)
        // TODO: this is a hack because defaultName is always null in Android SDK 35 (15)
        if(defaultName.isNullOrBlank()) {
            when {
                ContextCompat.checkSelfPermission(applicationContext,
                    Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED -> {
                        startUserActivities()
                }
            }
        }
        else if (packageName ==  defaultName) {
            startUserActivities()
        }
    }

    fun clickPrivacyPolicy(view: View?) {
        val url = getString(R.string.privacy_policy_url)
        val shareIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(shareIntent)
    }

    fun makeDefault() {
        // TODO: replace this with checking other permissions - since this gives null in level 35
        Log.d(getLocalClassName(), "Got into make default function..")
        val roleManagerIntent: Intent = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(ROLE_SERVICE) as RoleManager
             roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
            }
        } else {
            Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
            }
        }

        getDefaultPermission.launch(roleManagerIntent)
    }

    private fun startUserActivities() {
//        val intent = Intent(this, ThreadedConversationsActivity::class.java)

        val intent = Intent(this, ThreadsConversationActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

        finish()
    }

    public override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resultCode, data)

        if (reqCode == 0) {
            if (resultCode == RESULT_OK) {
                val sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                sharedPreferences.edit()
                    .putBoolean(getString(R.string.configs_load_natives), true)
                    .apply()
                startUserActivities()
            }
        }
    }

    fun checkPermissionToReadContacts(): Boolean {
        val check = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)

        return (check == PackageManager.PERMISSION_GRANTED)
    }

    companion object {
        const val READ_SMS_PERMISSION_REQUEST_CODE: Int = 1
        const val READ_CONTACTS_PERMISSION_REQUEST_CODE: Int = 2
    }
}