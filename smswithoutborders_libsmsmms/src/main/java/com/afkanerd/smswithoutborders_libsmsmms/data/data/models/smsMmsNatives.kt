package com.afkanerd.smswithoutborders_libsmsmms.data.data.models

import androidx.room.Entity
import kotlinx.serialization.Serializable

object smsMmsNatives {
    @Entity
    @Serializable
    data class Sms(
        val _id: Int,
        val thread_id: Int,
        val address: String?,
        val person: String? = null,
        val date: Int,
        val date_sent: Int,
        val protocol: String? = null,
        val read: Int,
        val status: Int,
        val type: Int,
        val reply_path_present: String? = null,
        val subject: String? = null,
        val body: String,
        val service_center: String? = null,
        val locked: Int,
        val sub_id: Int,
        val error_code: Int,
        val creator: String,
        val seen: Int,
    )

    @Entity
    @Serializable
    data class Mms(
        val _id: Int,
        val thread_id: Int,
        val date: Int,
        val date_sent: Int,
        val msg_box: Int,
        val read: Int,
        val m_id: String??,
        val sub: String?,
        val sub_cs: Int,
        val ct_t: String?,
        val ct_l: String?,
        val exp: String? = null,
        val m_cls: String?,
        val m_type: Int,
        val v: Int,
        val m_size: Int,
        val pri: Int,
        val rr: Int,
        val rpt_a: String? = null,
        val resp_st: String? = null,
        val st: String? = null,
        val tr_id: String? = null,
        val retr_st: String? = null,
        val retr_txt: String? = null,
        val retr_txt_cs: String? = null,
        val read_status: String? = null,
        val ct_cls: String? = null,
        val resp_txt: String? = null,
        val d_tm: String? = null,
        val d_rpt: Int,
        val locked: Int,
        val sub_id: Int,
        val seen: Int,
        val creator: String?,
        val text_only: Int,
    )

    @Entity
    @Serializable
    data class MmsPart(
        val _id: Int,
        val mid: Int,
        val seq: Int,
        val ct: String?,
        val name: String?,
        val chset: Int?,
        val cd: String? = null,
        val fn: String? = null,
        val cid: String?,
        val cl: String?,
        val ctt_s: String? = null,
        val ctt_t: String? = null,
        val _data: String?,
        val text: String?,
        val sub_id: Int,
    )

    @Entity
    @Serializable
    data class MmsAddr(
        val _id: Int,
        val msg_id : String?,
        val contact_id: String?,
        val address: String?,
        val type: String?,
        val charset: String?,
        val sub_id: Int? = null,
    )

    // For exporting
    data class SmsMmsContents(
        val mms: Map<String, ArrayList<Mms>>,
        val mms_addr: Map<String, ArrayList<MmsAddr>>,
        val mms_parts: Map<String, ArrayList<MmsPart>>,
        val sms: Map<String, ArrayList<Sms>>,
    )
}