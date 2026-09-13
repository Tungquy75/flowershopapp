package com.example.appfire.Helper


import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.os.Environment
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import com.example.appfire.model.Items
class TinyDB(private val preferences: SharedPreferences) {

    private var DEFAULT_APP_IMAGEDATA_DIRECTORY: String? = null
    private var lastImagePath = ""

    constructor(appContext: Context) : this(PreferenceManager.getDefaultSharedPreferences(appContext))

    fun isExternalStorageWritable(): Boolean {
        return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    }

    fun isExternalStorageReadable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
    }

    fun getImage(path: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getSavedImagePath(): String {
        return lastImagePath
    }

    fun putImage(theFolder: String?, theImageName: String?, theBitmap: Bitmap?): String? {
        if (theFolder == null || theImageName == null || theBitmap == null) return null
        DEFAULT_APP_IMAGEDATA_DIRECTORY = theFolder
        val mFullPath = setupFullPath(theImageName)
        if (mFullPath != "") {
            lastImagePath = mFullPath
            saveBitmap(mFullPath, theBitmap)
        }
        return mFullPath
    }

    fun putImageWithFullPath(fullPath: String?, theBitmap: Bitmap?): Boolean {
        return !(fullPath == null || theBitmap == null) && saveBitmap(fullPath, theBitmap)
    }

    private fun setupFullPath(imageName: String): String {
        val mFolder = File(Environment.getExternalStorageDirectory(), DEFAULT_APP_IMAGEDATA_DIRECTORY)
        if (isExternalStorageReadable() && isExternalStorageWritable() && !mFolder.exists()) {
            if (!mFolder.mkdirs()) {
                Log.e("ERROR", "Failed to setup folder")
                return ""
            }
        }
        return mFolder.path + '/' + imageName
    }

    private fun saveBitmap(fullPath: String, bitmap: Bitmap): Boolean {
        if (fullPath.isEmpty() || bitmap == null) return false
        var fileCreated = false
        var bitmapCompressed = false
        var streamClosed = false
        val imageFile = File(fullPath)
        if (imageFile.exists()) {
            if (!imageFile.delete()) return false
        }
        try {
            fileCreated = imageFile.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(imageFile)
            bitmapCompressed = bitmap.compress(CompressFormat.PNG, 100, out)
        } catch (e: Exception) {
            e.printStackTrace()
            bitmapCompressed = false
        } finally {
            if (out != null) {
                try {
                    out.flush()
                    out.close()
                    streamClosed = true
                } catch (e: IOException) {
                    e.printStackTrace()
                    streamClosed = false
                }
            }
        }
        return fileCreated && bitmapCompressed && streamClosed
    }

    fun getInt(key: String): Int {
        return preferences.getInt(key, 0)
    }

    fun getListInt(key: String): ArrayList<Int> {
        val myList = TextUtils.split(preferences.getString(key, "")!!, "‚‗‚")
        val newList = ArrayList<Int>()
        for (item in myList) {
            newList.add(item.toInt())
        }
        return newList
    }

    fun getLong(key: String): Long {
        return preferences.getLong(key, 0)
    }

    fun getFloat(key: String): Float {
        return preferences.getFloat(key, 0f)
    }

    fun getDouble(key: String): Double {
        val number = getString(key)
        return try {
            number.toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    fun getListDouble(key: String): ArrayList<Double> {
        val myList = TextUtils.split(preferences.getString(key, "")!!, "‚‗‚")
        val newList = ArrayList<Double>()
        for (item in myList) {
            newList.add(item.toDouble())
        }
        return newList
    }

    fun getListLong(key: String): ArrayList<Long> {
        val myList = TextUtils.split(preferences.getString(key, "")!!, "‚‗‚")
        val newList = ArrayList<Long>()
        for (item in myList) {
            newList.add(item.toLong())
        }
        return newList
    }

    fun getString(key: String): String {
        return preferences.getString(key, "")!!
    }

    fun getListString(key: String): ArrayList<String> {
        val myList = TextUtils.split(preferences.getString(key, "")!!, "‚‗‚")
        return arrayListOf(*myList)
    }

    fun getBoolean(key: String): Boolean {
        return preferences.getBoolean(key, false)
    }

    fun getListBoolean(key: String): ArrayList<Boolean> {
        val myList = getListString(key)
        val newList = ArrayList<Boolean>()
        for (item in myList) {
            newList.add(item == "true")
        }
        return newList
    }

    fun getListObject(key: String): ArrayList<Items> {
        val gson = Gson()
        val json = getString(key)
        val type = object : TypeToken<ArrayList<Items>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            ArrayList()
        }
    }

    inline fun <reified T> getObject(key: String): T {
        val gson = Gson()
        val json = getString(key)
        return gson.fromJson(json, T::class.java)
    }

    fun putInt(key: String, value: Int) {
        checkForNullKey(key)
        preferences.edit().putInt(key, value).apply()
    }

    fun putListInt(key: String, intList: ArrayList<Int>) {
        checkForNullKey(key)
        val joined = TextUtils.join("‚‗‚", intList)
        preferences.edit().putString(key, joined).apply()
    }

    fun putLong(key: String, value: Long) {
        checkForNullKey(key)
        preferences.edit().putLong(key, value).apply()
    }

    fun putListLong(key: String, longList: ArrayList<Long>) {
        checkForNullKey(key)
        val joined = TextUtils.join("‚‗‚", longList)
        preferences.edit().putString(key, joined).apply()
    }

    fun putFloat(key: String, value: Float) {
        checkForNullKey(key)
        preferences.edit().putFloat(key, value).apply()
    }

    fun putDouble(key: String, value: Double) {
        checkForNullKey(key)
        putString(key, value.toString())
    }

    fun putListDouble(key: String, doubleList: ArrayList<Double>) {
        checkForNullKey(key)
        val joined = TextUtils.join("‚‗‚", doubleList)
        preferences.edit().putString(key, joined).apply()
    }

    fun putString(key: String, value: String) {
        checkForNullKey(key)
        checkForNullValue(value)
        preferences.edit().putString(key, value).apply()
    }

    fun putListString(key: String, stringList: ArrayList<String>) {
        checkForNullKey(key)
        val joined = TextUtils.join("‚‗‚", stringList)
        preferences.edit().putString(key, joined).apply()
    }

    fun putBoolean(key: String, value: Boolean) {
        checkForNullKey(key)
        preferences.edit().putBoolean(key, value).apply()
    }

    fun putListBoolean(key: String, boolList: ArrayList<Boolean>) {
        checkForNullKey(key)
        val newList = ArrayList<String>()
        for (item in boolList) {
            newList.add(if (item) "true" else "false")
        }
        putListString(key, newList)
    }

    fun putObject(key: String, obj: Any) {
        checkForNullKey(key)
        val gson = Gson()
        putString(key, gson.toJson(obj))
    }

    fun putListObject(key: String, playerList: ArrayList<Items>) {
        checkForNullKey(key)
        val gson = Gson()
        putString(key, gson.toJson(playerList))
    }

    fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    fun deleteImage(path: String): Boolean {
        return File(path).delete()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    fun getAll(): Map<String, *> {
        return preferences.all
    }

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun checkForNullKey(key: String) {
        if (key == null) {
            throw NullPointerException()
        }
    }

    private fun checkForNullValue(value: String) {
        if (value == null) {
            throw NullPointerException()
        }
    }
}