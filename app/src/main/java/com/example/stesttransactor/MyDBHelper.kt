package com.example.stesttransactor

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.myapplication.DTO.PerdometerData
import com.example.stesttransactor.constn.Companion.COL_BURNED
import com.example.stesttransactor.constn.Companion.COL_DAY
import com.example.stesttransactor.constn.Companion.COL_DISTANCE
import com.example.stesttransactor.constn.Companion.COL_ID
import com.example.stesttransactor.constn.Companion.COL_SPEED
import com.example.stesttransactor.constn.Companion.COL_STEP_COUNT
import com.example.stesttransactor.constn.Companion.COL_TIMER
import com.example.stesttransactor.constn.Companion.TABLE_PERDOMETER
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Boolean as Boolean1


class MyDBHelper(context: Context) : SQLiteOpenHelper(context, constn.DB_NAME, null, constn.DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createPerdometerTable =
                "CREATE TABLE $TABLE_PERDOMETER (" +
                        "$COL_ID integer PRIMARY KEY AUTOINCREMENT," +
                        "$COL_DAY datetime DEFAULT CURRENT_TIMESTAMP," +
                        "$COL_STEP_COUNT int," +
                        "$COL_TIMER varchar," +
                        "$COL_DISTANCE varchar," +
                        "$COL_BURNED float," +
                        "$COL_SPEED float);"
        db?.execSQL(createPerdometerTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun addPerdometerData(item: PerdometerData): Boolean1 {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put(COL_DAY, item.day)
        cv.put(COL_STEP_COUNT, item.numberSteps)
        cv.put(COL_DISTANCE, item.distance)
        cv.put(COL_TIMER, item.countTime)
        cv.put(COL_SPEED, item.speed)
        cv.put(COL_BURNED, item.caloriesBurned)

        val result = db.insert(TABLE_PERDOMETER, null, cv)
        return result != (-1).toLong()
        db.close()
    }

    fun updatePerdometerData(item: PerdometerData) {
        val db = writableDatabase
        val date = Date()
        val strDateFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(strDateFormat)
        val todayDate = sdf.format(date)
        val cv = ContentValues()
        cv.put(COL_DAY, item.day)
        cv.put(COL_STEP_COUNT, item.numberSteps)
        cv.put(COL_DISTANCE, item.distance)
        cv.put(COL_TIMER, item.countTime)
        cv.put(COL_SPEED, item.speed)
        cv.put(COL_BURNED, item.caloriesBurned)

        db.update(TABLE_PERDOMETER, cv, "$COL_DAY=?", arrayOf(convertDateToLong(todayDate).toString()))

        db.close()
    }

    fun checkIsSave(): kotlin.Boolean {
        var isDateAlreadyPresent = false
        val date = Date()
        val strDateFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(strDateFormat)
        val todayDate = sdf.format(date)
        val FinalTodayDate = convertDateToLong(todayDate)
        var selectQuery = "SELECT * FROM " + TABLE_PERDOMETER + " WHERE " + COL_DAY + " ='" + FinalTodayDate + "'"
        try {
            val db = this.readableDatabase
            val c: Cursor = db.rawQuery(selectQuery, null)
            if (c.moveToFirst()) {
                do {
                    isDateAlreadyPresent = true

                } while (c.moveToNext())
            } else {
                isDateAlreadyPresent = false
            }
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return isDateAlreadyPresent
    }

    fun getPerdometerData(): MutableList<PerdometerData> {
        val result: MutableList<PerdometerData> = ArrayList()
        val date = Date()
        val strDateFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(strDateFormat)
        val todayDate = sdf.format(date)
        val FinalToDayDate =convertDateToLong(todayDate)
        val db = readableDatabase
        val queryResult = db.rawQuery("SELECT * FROM $TABLE_PERDOMETER WHERE $COL_DAY ='$FinalToDayDate'", null)

        if (queryResult.moveToFirst()) {
            do {
                val item = PerdometerData()
                item.id = queryResult.getLong(queryResult.getColumnIndex(COL_ID))
                item.day = convertLongToTime(queryResult.getLong(queryResult.getColumnIndex(COL_DAY)))
                item.numberSteps = queryResult.getInt(queryResult.getColumnIndex(COL_STEP_COUNT))
                item.countTime = queryResult.getString(queryResult.getColumnIndex(COL_TIMER))
                item.distance = queryResult.getString(queryResult.getColumnIndex(COL_DISTANCE))
                item.speed = queryResult.getFloat(queryResult.getColumnIndex(COL_SPEED))
                item.caloriesBurned = queryResult.getFloat(queryResult.getColumnIndex(COL_BURNED))

                result.add(item)
            } while (queryResult.moveToNext())
        }

        queryResult.close()
        return result
    }

    fun getWeekPerdometerData(): MutableList<PerdometerData> {
        val result: MutableList<PerdometerData> = ArrayList()
        val date = Date()
        val strDateFormat = "dd/MM/yyyy"
        val sdfday = SimpleDateFormat(strDateFormat)
        val todayDate = sdfday.format(date)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        val format = formatter.format(calendar.time)
        val FinalTodayDate = convertDateToLong(todayDate)
        val finalFormat = convertDateToLong(format)
//        Log.i("check", finalFormat.toString())
//        Log.i("check", FinalTodayDate.toString())
        val db = readableDatabase
        val queryResult = db.rawQuery("SELECT * FROM " + constn.TABLE_PERDOMETER, null)
        Log.i("check", queryResult.count.toString())
        if (queryResult.moveToFirst()) {
            do {
                val item = PerdometerData()
                item.id = queryResult.getLong(queryResult.getColumnIndex(COL_ID))
                item.day = convertLongToTime(queryResult.getLong(queryResult.getColumnIndex(COL_DAY)))
                item.numberSteps = queryResult.getInt(queryResult.getColumnIndex(COL_STEP_COUNT))
                item.countTime = queryResult.getString(queryResult.getColumnIndex(COL_TIMER))
                item.distance = queryResult.getString(queryResult.getColumnIndex(COL_DISTANCE))
                item.speed = queryResult.getFloat(queryResult.getColumnIndex(COL_SPEED))
                item.caloriesBurned = queryResult.getFloat(queryResult.getColumnIndex(COL_BURNED))

                result.add(item)
//                Log.i("check", convertLongToTime(queryResult.getLong(queryResult.getColumnIndex(COL_DAY))))
            } while (queryResult.moveToNext())
        }

        queryResult.close()
        return result

    }

    fun addPerdometerDatatest(): Boolean1 {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put(COL_DAY, "05/14/2021")
        cv.put(COL_STEP_COUNT, 1235)
        cv.put(COL_DISTANCE, 1732)
        cv.put(COL_TIMER, "10:20")
        cv.put(COL_SPEED, 12)
        cv.put(COL_BURNED, 150.375)

        val result = db.insert(TABLE_PERDOMETER, null, cv)
        return result != (-1).toLong()
        db.close()
    }

    fun deleteTitle() {
        val db = writableDatabase
        db.delete(TABLE_PERDOMETER, "$COL_ID=?", arrayOf("4"))

    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd/MM/yyyy")
        return format.format(date)
    }

    fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("dd/MM/yyyy")
        return df.parse(date).time
    }

}
//roi do load deo dc :((