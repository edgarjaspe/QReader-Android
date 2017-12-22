package com.mobitribe.app.qreader

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.provider.ContactsContract
import android.util.Log
import com.mobitribe.app.qreader.network.RestClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.Map

/**
 * Author: Muhammad Shahab.
 * Organization: Mobitribe
 * Date: 12/19/17
 * Description:
 */


class ContactSyncUp : AsyncTask<Void, Void, Void> {

    @SuppressLint("StaticFieldLeak")
    val activity: BaseMainActivity

    constructor(activity: BaseMainActivity) : super() {
        this.activity = activity
    }


    private lateinit var hashMapContacts: HashMap<String, String>

    override fun doInBackground(vararg voids: Void): Void? {

        val cr = activity.contentResolver //Activity/Application android.content.Context
        val cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

        hashMapContacts = HashMap<String,String>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    val pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id), null)
                    while (pCur != null && pCur.moveToNext()) {


                        var phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        var name = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                        hashMapContacts.put(phoneNumber,name)
                    }
                    pCur.close()
                }

            } while (cursor.moveToNext())
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
       pushContactsToServer()
    }


    private var count: Int = 0;


    /**
     * @usage it pushes the contact of user to server by POST me/realmContact endpoint
     * @param phones
     */
    private fun pushContactsToServer() {

        val it = hashMapContacts.entries.iterator()

        while (it.hasNext()) {

            val contact = getContacts(it)
            RestClient.getRestAdapter().insertContacts(contact,activity.device_UUID).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody> , response: retrofit2.Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Log.d(TAG, count.toString() + "Contact inserted")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                }
            })

        }
    }

    private fun getContacts(it: MutableIterator<MutableMap.MutableEntry<String, String>>): String {
        var contactString = ""
        while (it.hasNext()&&count<chunckLength) {
            count++
            val pair = it.next() as Map.Entry<*, *>
            if(contactString.isEmpty())
            {
                contactString += pair.value.toString() + "|" + pair.key.toString()
            }
            else
            {
                contactString += "," + pair.value.toString() + "|" + pair.key.toString()
            }

        }
        if (count == chunckLength) count = 0;
        Log.d(TAG,contactString)
        return contactString
    }

    companion object {

        private val TAG = "ContactAsyncTask"
        private val chunckLength: Int = 200;
    }


}
