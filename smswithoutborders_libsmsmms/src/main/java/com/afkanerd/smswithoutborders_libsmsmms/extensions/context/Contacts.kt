package com.afkanerd.smswithoutborders_libsmsmms.extensions.context

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.database.getStringOrNull

fun Context.retrieveContactName(phoneNumber: String): String? {
    val uri = Uri.withAppendedPath(
        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
        Uri.encode(phoneNumber)
    )
    val cursor = contentResolver.query(
        uri,
        arrayOf<String>(ContactsContract.PhoneLookup.DISPLAY_NAME),
        null,
        null, null
    )

    if (cursor == null) return null

    try {
        if (cursor.moveToFirst()) {
            val displayNameIndex =
                cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)
            return cursor.getString(displayNameIndex)
        }
    } finally {
        cursor.close()
    }

    return null
}

fun Context.retrieveContactPhoto(phoneNumber: String?): String? {
    val uri = Uri.withAppendedPath(
        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
        Uri.encode(phoneNumber)
    )
    val cursor = contentResolver.query(
        uri,
        arrayOf<String>(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI),
        null,
        null, null
    )

    var contactPhotoThumbUri: String? = ""
    if (cursor == null) return null

    try {
        if (cursor.moveToFirst()) {
            val displayContactPhoto =
                cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI)
            contactPhotoThumbUri = cursor.getStringOrNull(displayContactPhoto)
        }
    } finally {
        cursor.close()
    }
    return contactPhotoThumbUri
}

fun Context.filterContacts(filter: String): Cursor? {
    if (filter.isEmpty()) return null
    val projection = arrayOf<String?>(
        ContactsContract.CommonDataKinds.Phone._ID,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )
    val selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " IS NOT NULL AND " +
            ContactsContract.CommonDataKinds.Phone.NUMBER + " <> '' AND (" +
            ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE '%" + filter + "%' OR " +
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE '%" + filter + "%')"
    val selectionArgs: Array<String?>? = null
    val sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC"
    return contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        sortOrder
    )
}

fun Context.getPhonebookContacts(): Cursor? {
    val projection = arrayOf<String?>(
        ContactsContract.CommonDataKinds.Phone._ID,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )
    val selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " IS NOT NULL AND " +
            ContactsContract.CommonDataKinds.Phone.NUMBER + " <> ''"
    val selectionArgs: Array<String?>? = null
    val sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC"
    return contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        sortOrder
    )
}


fun Context.retrieveContactUri(phoneNumber: String?): Uri? {
    val uri = Uri.withAppendedPath(
        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
        Uri.encode(phoneNumber)
    )
    val cursor = contentResolver.query(
        uri,
        null,
        null,
        null, null
    )

    if (cursor == null) return null

    try {
        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val lookupKeyIndex = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)
            val id = cursor.getLong(idIndex)
            val lookupKey = cursor.getString(lookupKeyIndex)
            return ContactsContract.Contacts.getLookupUri(id, lookupKey)
        }
    } finally {
        cursor.close()
    }

    return null
}