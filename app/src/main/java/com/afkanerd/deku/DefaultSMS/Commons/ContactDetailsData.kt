package com.afkanerd.deku.DefaultSMS.Commons

data class ContactDetailsData(
    val phoneNumber: String,
    val contactPhotoUri: String?,
    val isContact: Boolean,
    val isEncryptionEnabled: Boolean,
    val contactName: String,
    val id: String?
)