package com.example.nfccardrw

import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NfcManager(private val activity: Activity) {
    private val nfcAdapter: NfcAdapter? by lazy { NfcAdapter.getDefaultAdapter(activity) }

    fun isNfcEnabled(): Boolean = nfcAdapter?.isEnabled == true

    suspend fun readNfcTag(tag: Tag): String = withContext(Dispatchers.IO) {
        val ndef = Ndef.get(tag) ?: return@withContext "Error: Tag is not NDEF"

        ndef.connect()
        val ndefMessage = ndef.cachedNdefMessage
        ndef.close()

        val record = ndefMessage.records.firstOrNull()
        val payload = record?.payload

        if (payload != null) {
            String(payload, Charsets.UTF_8)
        } else {
            "Error: No NDEF records found"
        }
    }

    suspend fun writeNfcTag(tag: Tag, message: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                val ndefRecord = NdefRecord.createTextRecord(null, message)
                val ndefMessage = NdefMessage(arrayOf(ndefRecord))
                ndef.writeNdefMessage(ndefMessage)
                ndef.close()
                true
            } else {
                val ndefFormatable = NdefFormatable.get(tag)
                if (ndefFormatable != null) {
                    ndefFormatable.connect()
                    val ndefRecord = NdefRecord.createTextRecord(null, message)
                    val ndefMessage = NdefMessage(arrayOf(ndefRecord))
                    ndefFormatable.format(ndefMessage)
                    ndefFormatable.close()
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
