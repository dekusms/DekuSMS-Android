package com.afkanerd.deku.DefaultSMS.Models

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult

object ExportImportHandlers {

    fun exportInbox(context: Context) {
        // Request code for creating a PDF document.
        val filename = "Deku_SMS_All_Backup" + System.currentTimeMillis() + ".json";
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, filename);

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
//        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(
            context as Activity,
            intent,
            777,
            null
        )
    }
}