package com.afkanerd.deku.DefaultSMS.AdaptersViewModels

import android.content.Context
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.afkanerd.deku.DefaultSMS.Commons.ContactDetailsData
import com.afkanerd.deku.DefaultSMS.Models.Contacts
import com.afkanerd.deku.DefaultSMS.Models.E2EEHandler
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.Models.E2EEHandler.isSecured
import com.afkanerd.deku.DefaultSMS.Models.NativeSMSDB

import com.afkanerd.deku.DefaultSMS.R
import com.google.i18n.phonenumbers.NumberParseException
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

        val cursor = Contacts.filterContacts(context, details)
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

                contactsList.add(Contacts(context, id, displayName, number))
            } while (cursor.moveToNext())
            cursor.close()
        }

        if (contactsList.isEmpty() && PhoneNumberUtils.isWellFormedSmsAddress(details)) {
            val contacts = Contacts()
            contacts.contactName = "${context.getString(R.string.send_to)} $details"
            contacts.number = details
            contacts.type = Contacts.TYPE_NEW_CONTACT
            contactsList.add(contacts)
        }
        contactsMutableLiveData?.postValue(contactsList)
    }


    fun loadContacts(context: Context) {
        val cursor = Contacts.getPhonebookContacts(context)
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

                contactsList.add(Contacts(context, id, displayName, number))
            } while (cursor.moveToNext())
            cursor.close()
        }
        contactsMutableLiveData?.postValue(contactsList)
    }

}
