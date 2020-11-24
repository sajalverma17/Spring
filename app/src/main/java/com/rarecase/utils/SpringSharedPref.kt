package com.rarecase.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment

class SpringSharedPref(context : Context){
    private val SHARED_PREF_NAME = "Spring_Shared_Pref"
    private val FIRST_TIME = "First_Time"
    private val STORAGE_PATH = "Storage_Path"

    val sharedPreferences : SharedPreferences
    val editor : SharedPreferences.Editor

    init {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,0)
        editor = sharedPreferences.edit()
    }

    var isFirstTime : Boolean = true
        get() {
            if (sharedPreferences.contains(FIRST_TIME))
            {
                field = sharedPreferences.getBoolean(FIRST_TIME, true)
            }
            return field
        }
        set(value){
            editor.putBoolean(FIRST_TIME, value)
            editor.apply()
        }

    var storagePath: String? = Environment.getExternalStorageDirectory().absolutePath
        get() {
            if(sharedPreferences.contains(STORAGE_PATH)){
                field = sharedPreferences.getString(STORAGE_PATH,field)
            }
            return field
        }
        set(value){
            editor.putString(STORAGE_PATH,value)
            editor.apply()
        }
}
