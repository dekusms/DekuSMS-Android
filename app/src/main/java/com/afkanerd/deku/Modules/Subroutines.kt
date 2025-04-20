package com.afkanerd.deku.Modules

import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import android.provider.Telephony

object Subroutines {

    fun isDefault(context: Context): Boolean {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (context.getSystemService(Context.ROLE_SERVICE) as RoleManager)
                .isRoleHeld(RoleManager.ROLE_SMS)
        } else {
            Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
        }
    }
}