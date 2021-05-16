package com.example.stesttransactor

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.text.format.DateUtils
import android.view.Menu
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.DTO.PerdometerData
import com.example.stesttransactor.constn.Companion.COL_DAY
import kotlinx.android.synthetic.main.activity_main.*

import kotlinx.android.synthetic.main.fragment_day.*
import kotlinx.android.synthetic.main.fragment_month.*
import kotlinx.android.synthetic.main.fragment_week.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule

var running = false
var sensorManager: SensorManager? = null

//    var chronometer: Chronometer = findViewById(R.id.chronometer)
var runing = false
var pauseOffset: Long = 0
var mStepCounter = 0
var mStepDetector = 0
var isCounterSensor: Boolean = true
var isDetectorSensor: Boolean = true
lateinit var dbHelper: MyDBHelper
var steps:Int = 0
var distances:Float =0f
var speeds:Float =0F
var Last_CaloriesValues:Double = 0.0
var reBooted:Boolean = false
var checkSteps:Int =0
var stepGet:Int =0
var getSteps:Int = 0
var loadstep:Int=0
var selected:Int =1
private  lateinit var comunicators: comunicator
val fragment1 =Fragment1()
val fragment2 =Fragment2()
val fragment3 =newFragment()
class MainActivity : AppCompatActivity() ,SensorEventListener{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment1).commit()

        dbHelper = MyDBHelper(this)
//        dbHelper.deleteTitle()
        val date = Date()
        val strDateFormat = "MM/dd/yyyy"
        val sdfday = SimpleDateFormat(strDateFormat)
        val todayDate =sdfday.format(date)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val formatter = SimpleDateFormat(" MM/dd/yyyy")
        val format = formatter.format(calendar.time)
        val getdata = dbHelper.getWeekPerdometerData()
        var s:String =""
        for(element in getdata){
            s =s+ element.day.toString()

        }
        Toast.makeText(applicationContext, s.toString(), Toast.LENGTH_SHORT).show()
        //        Toast.makeText(applicationContext,"SELECT * FROM "+ constn.TABLE_PERDOMETER + " WHERE " + COL_DAY + " BETWEEN"+format+" AND "+todayDate, Toast.LENGTH_LONG).show()
//deo dc :(

//        val calendar = Calendar.getInstance()
//        calendar.add(Calendar.DAY_OF_YEAR, -6)
//        val formatter = SimpleDateFormat(" MM/dd/yyyy")
//        val format = formatter.format(calendar.time)
//        Toast.makeText(applicationContext,  format.toString(), Toast.LENGTH_SHORT).show()
        if (ContextCompat.checkSelfPermission(this@MainActivity,
                        Manifest.permission.ACTIVITY_RECOGNITION) !==
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                            Manifest.permission.ACTIVITY_RECOGNITION)) {
                ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 1)
            } else {
                ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 1)
            }
        }
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if (sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            var mStepCounter: Sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        }
        if (sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            var mStepDetector: Sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this@MainActivity,
                                    Manifest.permission.ACTIVITY_RECOGNITION) ===
                                    PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
    override fun onResume() {

        super.onResume()
        running = true
        var mStepCounter = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        var mStepDetector = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (mStepCounter == null) {
            Toast.makeText(this, "Khong co cam bien do buoc chan !", Toast.LENGTH_SHORT).show()
        } else if (mStepDetector == null) {
            Toast.makeText(this, "Khong co cam bien dieu tra buoc chan !", Toast.LENGTH_SHORT).show()
        } else if (mStepCounter != null) {

            sensorManager?.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_UI)

            val prefsChom = getSharedPreferences("prefsChrom", Context.MODE_PRIVATE)
            pauseOffset =prefsChom.getLong("pauseOffset",0)
            prefsChom.edit().putLong("pauseOffset", pauseOffset-1500).apply();




        } else {
            sensorManager?.registerListener(this, mStepDetector, SensorManager.SENSOR_DELAY_UI)


        }

    }
    override fun onPause() {
//        pauseChronometer()
        super.onPause()
        running = false
        if (sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {

//           if (reBooted){
            val prefsStep = getSharedPreferences("prefsStep", Context.MODE_PRIVATE)
            val editor = prefsStep.edit()
            editor.putInt("stepGet",loadstep)
            editor.apply()
            Toast.makeText(applicationContext, loadstep.toString(), Toast.LENGTH_LONG).show()
//           }

            dbHelper = MyDBHelper(context = this)
            val data = PerdometerData()
            data.numberSteps = steps
            data.countTime = chronometer.text.toString()

            data.distance = distance.text.toString()
            data.speed =speed.text.toString().toFloat()
            data.caloriesBurned =calories.text.toString().toFloat()
            val date = Date()
            val strDateFormat = "MM/dd/yyyy"
            val sdf = SimpleDateFormat(strDateFormat)
            data.day = System.currentTimeMillis().toString()
            val checked = dbHelper.checkIsSave()
            if(checked == true){
                dbHelper.updatePerdometerData(data)

            }
            else{
                dbHelper.addPerdometerData(data)
//                 Toast.makeText(applicationContext, data.day,Toast.LENGTH_LONG).show()

            }

            sensorManager?.unregisterListener(this, mStepCounter)
        }
        if (sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            sensorManager?.unregisterListener(this, mStepDetector)
        }

    }
    override fun onStop() {
        super.onStop()
        Toast.makeText(applicationContext, "destroy",Toast.LENGTH_LONG).show()

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
    override fun onSensorChanged(event: SensorEvent) {
        if (mStepCounter != null) {
            reBooted = true
            loadstep = event.values[0].toInt()
            val prefsStep = getSharedPreferences("prefsStep", Context.MODE_PRIVATE)
            stepGet =prefsStep.getInt("stepGet",0)
            dbHelper = MyDBHelper(this)

            getSteps =event.values[0].toInt()
            val getdata = dbHelper.getPerdometerData()

            for(element in getdata){

                checkSteps = element.numberSteps

            }
            if(checkSteps!=null &&getSteps ==0){
                steps = checkSteps
                stepsValue.setText(""+steps)

                val prefsStep = getSharedPreferences("prefsStep", Context.MODE_PRIVATE)
                val editor = prefsStep.edit()
                editor.putInt("stepGet",0)
                editor.apply()
                distances = (steps * 0.75).toFloat()
                val elapsedMillis: Long = SystemClock.elapsedRealtime() - chronometer.getBase()
                speeds = distances / (elapsedMillis / 1000f)
                val hours: Float = (elapsedMillis / 1000f /3600f).toFloat()
                val speedInMph: Float = (distances / 1000f / 1.609f / hours).toFloat()
                val metValue = getMetForActivity(speedInMph)
                val constant = 3.5f
                val harrisBenedictRmR: Float = 66.4730f + (5.0033f * 178f) + (13.7516f * 55f) - (6.7550f * 20f);
                val correctedMets = metValue * (constant / harrisBenedictRmR)
                val caloriess: Float =calories.text.toString().toFloat()
                val Calories_value :Float =correctedMets+caloriess
                Last_CaloriesValues = Math.round(Calories_value * 10000).toDouble() / 10000

            }

            else if (checkSteps != getSteps){

                steps = checkSteps  + getSteps - stepGet
                distances = (steps * 0.75).toFloat()
                val elapsedMillis: Long = SystemClock.elapsedRealtime() - chronometer.getBase()
                speeds = distances / (elapsedMillis / 1000f)
                val hours: Float = (elapsedMillis / 1000f /3600f).toFloat()
                val speedInMph: Float = (distances / 1000f / 1.609f / hours).toFloat()
                val metValue = getMetForActivity(speedInMph)
                val constant = 3.5f
                val harrisBenedictRmR: Float = 66.4730f + (5.0033f * 178f) + (13.7516f * 55f) - (6.7550f * 20f);
                val correctedMets = metValue * (constant / harrisBenedictRmR)
                val caloriess: Float =calories.text.toString().toFloat()
                val Calories_value :Float =correctedMets+caloriess
                Last_CaloriesValues = Math.round(Calories_value * 10000).toDouble() / 10000
//                 Toast.makeText(applicationContext, "database"+checkSteps.toString(),Toast.LENGTH_SHORT).show()
//                 Toast.makeText(applicationContext, "-stepget"+stepGet.toString(),Toast.LENGTH_SHORT).show()
//                 Toast.makeText(applicationContext, "câccc"+getSteps.toString(),Toast.LENGTH_SHORT).show()

            }
            else{
                steps =event.values[0].toInt()
                distances = (steps * 0.75).toFloat()
                val elapsedMillis: Long = SystemClock.elapsedRealtime() - chronometer.getBase()
                speeds = distances / (elapsedMillis / 1000f)
                val hours: Float = (elapsedMillis / 1000f /3600f).toFloat()
                val speedInMph: Float = (distances / 1000f / 1.609f / hours).toFloat()
                val metValue = getMetForActivity(speedInMph)
                val constant = 3.5f
                val harrisBenedictRmR: Float = 66.4730f + (5.0033f * 178f) + (13.7516f * 55f) - (6.7550f * 20f);
                val correctedMets = metValue * (constant / harrisBenedictRmR)
                val caloriess: Float =calories.text.toString().toFloat()
                val Calories_value :Float =correctedMets+caloriess
                Last_CaloriesValues = Math.round(Calories_value * 10000).toDouble() / 10000
                Toast.makeText(applicationContext, "cac"+checkSteps,Toast.LENGTH_LONG).show()

            }
            calories.setText("" +Last_CaloriesValues)
            startChronometer()

           if(selected==2){
               stepsValueWeek?.setText("" + steps)
           }else if(selected==1){
               stepsValue?.setText("" + steps)
           }
//            stepsValueWeek?.setText("" + steps)
//            stepsValueMonth?.setText("" + steps)
            distance.setText("" + distances)

            val final_speed = Math.round(speeds * 100).toDouble() / 100
            speed.setText("" + final_speed)
            Timer("SettingUp", false).schedule(1500) {
                pauseChronometer()

            }

        }

    }
    fun startChronometer() {
        if (!runing) {
            val prefsChom = getSharedPreferences("prefsChrom", Context.MODE_PRIVATE)
            pauseOffset =prefsChom.getLong("pauseOffset",0)
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset)
            chronometer.start()
            runing = true

//             runing =prefsChom.getBoolean("runing",false)
        }

    }

    fun pauseChronometer() {
        if (runing) {
            chronometer.stop()
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase()
            val prefsChom = getSharedPreferences("prefsChrom", Context.MODE_PRIVATE)
            val editors = prefsChom.edit()
            editors.putLong("pauseOffset", pauseOffset)
//             editors.putBoolean("runing", runing)
            editors.apply()
            runing = false
        }

    }
    private fun getMetForActivity(speedInMph: Float): Float {
        if (speedInMph < 2.0) {
            return 2.0f
        } else if (java.lang.Float.compare(speedInMph, 2.0f) == 0) {
            return 2.8f
        } else if (java.lang.Float.compare(speedInMph, 2.0f) > 0 && java.lang.Float.compare(speedInMph, 2.7f) <= 0) {
            return 3.0f
        } else if (java.lang.Float.compare(speedInMph, 2.8f) > 0 && java.lang.Float.compare(speedInMph, 3.3f) <= 0) {
            return 3.5f
        } else if (java.lang.Float.compare(speedInMph, 3.4f) > 0 && java.lang.Float.compare(speedInMph, 3.5f) <= 0) {
            return 4.3f
        } else if (java.lang.Float.compare(speedInMph, 3.5f) > 0 && java.lang.Float.compare(speedInMph, 4.0f) <= 0) {
            return 5.0f
        } else if (java.lang.Float.compare(speedInMph, 4.0f) > 0 && java.lang.Float.compare(speedInMph, 4.5f) <= 0) {
            return 7.0f
        } else if (java.lang.Float.compare(speedInMph, 4.5f) > 0 && java.lang.Float.compare(speedInMph, 5.0f) <= 0) {
            return 8.3f
        } else if (java.lang.Float.compare(speedInMph, 5.0f) > 0) {
            return 9.8f
        }
        return 0f

    }

//    override fun passdata(Step: String ,fragment: Fragment) {
//       val bundle = Bundle()
//        bundle.putString("message",Step)
//        val transaction = this.supportFragmentManager.beginTransaction()
//        val fragment2 =Fragment2()
//        fragment2.arguments= bundle
//        transaction.replace(R.id.fragment_container,fragment2)
//        transaction.commit()
//
//    }


}
private fun SensorManager.unregisterListener(mainActivity: MainActivity, mStepCounter: Int) {

}

private fun SensorManager?.registerListener(mainActivity: MainActivity, mStepCounter: Sensor, sensorDelayUi: Int) {

}