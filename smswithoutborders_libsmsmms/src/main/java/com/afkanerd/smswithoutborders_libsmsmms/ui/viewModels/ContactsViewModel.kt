package com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels

import android.content.Context
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.afkanerd.smswithoutborders_libsmsmms.R
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.Contacts
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.filterContacts
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getPhonebookContacts
import java.util.ArrayList

class ContactsViewModel : ViewModel() {
    var contactsMutableLiveData: MutableLiveData<MutableList<Contacts>>? = null

    fun getContacts(context: Context): MutableLiveData<MutableList<Contacts>> {
        if (contactsMutableLiveData == null) {
            contactsMutableLiveData = MutableLiveData<MutableList<Contacts>>()
            loadContacts(context)
        }

        return contactsMutableLiveData!!
    }

    fun filterContact(context: Context, details: String) {
        val contactsList: MutableList<Contacts> = ArrayList<Contacts>()
        if (details.isEmpty()) {
            loadContacts(context)
            return
        }

        context.filterContacts(details)?.let { cursor ->
            if(cursor.moveToFirst()) {
                do {
                    val idIndex = cursor.getColumnIndexOrThrow( ContactsContract
                        .CommonDataKinds.Phone._ID)

                    val displayNameIndex = cursor.getColumnIndexOrThrow(ContactsContract
                        .CommonDataKinds.Phone.DISPLAY_NAME)

                    val numberIndex = cursor.getColumnIndexOrThrow(ContactsContract
                        .CommonDataKinds.Phone.NUMBER)

                    val displayName = cursor.getString(displayNameIndex).toString()

                    val id = cursor.getLong(idIndex)

                    val number = cursor.getString(numberIndex).toString()

                    contactsList.add(Contacts(
                        id = id,
                        displayName = displayName,
                        address = number))
                } while (cursor.moveToNext())
                cursor.close()
            }
        }

        if (contactsList.isEmpty() && PhoneNumberUtils.isWellFormedSmsAddress(details)) {
            contactsList.add(Contacts(
                id = -1,
                displayName = "${context.getString(R.string.send_to)} $details",
                address = details
            ))
        }
        contactsMutableLiveData?.postValue(contactsList)
    }


    fun loadContacts(context: Context) {
        val cursor = context.getPhonebookContacts()
        val contactsList: MutableList<Contacts> = ArrayList<Contacts>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val idIndex =
                    cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone._ID)
                val displayNameIndex =
                    cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex =
                    cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)

                val displayName = cursor.getString(displayNameIndex).toString()
                val id = cursor.getLong(idIndex)
                val number = cursor.getString(numberIndex).toString()

                contactsList.add(Contacts(
                    id,
                    displayName,
                    number))
            } while (cursor.moveToNext())
            cursor.close()
        }
        contactsMutableLiveData?.postValue(contactsList)
    }

}