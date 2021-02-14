package com.rarecase.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment

class SpringSharedPref(context : Context){
    private val SHARED_PREF_NAME = "Spring_Shared_Pref"
    private val FIRST_TIME = "First_Time"

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
}
