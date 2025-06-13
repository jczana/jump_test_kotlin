package com.example.jumptest

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import android.widget.Button
import android.widget.TextView
import android.os.CountDownTimer
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.collection.MutableFloatList
import androidx.collection.mutableFloatListOf
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {

    val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)

    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private lateinit var xyChart: XYChart
    private var recordAcceleration = false
    private var timestamps: MutableFloatList = MutableFloatList(1000)
    private var acceleration: List<MutableFloatList> =
        listOf(MutableFloatList(1000), MutableFloatList(1000), MutableFloatList(1000), MutableFloatList(1000))

    private var fIdx = 0

    private fun clearAccelerationData()
    {
        timestamps = MutableFloatList(1000)
        acceleration = listOf(MutableFloatList(1000), MutableFloatList(1000), MutableFloatList(1000), MutableFloatList(1000))
        recordAcceleration = true
    }

    private fun startRecording()
    {
        recordAcceleration = true
    }

    private fun stopRecording()
    {
        recordAcceleration = false
    }

    private fun isRecording(): Boolean
    {
        return recordAcceleration
    }

    private var countdownTimer = object : CountDownTimer(8000, 1000) {

        override fun onTick(millisUntilFinished: Long) {
            if(millisUntilFinished <= 6000 && !recordAcceleration)
                startRecording()
            if(millisUntilFinished <= 6000 && millisUntilFinished >= 3999)
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
            else if(millisUntilFinished <= 4000 && millisUntilFinished > 3000)
                toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 500)
            setOutputText("Seconds remaining: " + millisUntilFinished / 1000)
        }

        override fun onFinish() {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 500)
            stopRecording()
            setOutputText("Done!")
        }
    }

    private fun setOutputText(message : String)
    {
        findViewById<TextView>(R.id.textView).setText(message)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_layout)

        xyChart = findViewById<XYChart>(R.id.xychart)

        findViewById<Button>(R.id.button).setOnClickListener {
            setOutputText("Started!")
            clearAccelerationData()
            countdownTimer.start()
        }

        findViewById<Button>(R.id.button2).setOnClickListener {
            setOutputText("Cancelled!")
            countdownTimer.cancel()
            stopRecording()
        }

        findViewById<Button>(R.id.button3).setOnClickListener {

            var x: MutableFloatList
            var y: List<MutableFloatList>
            if(fIdx % 2 == 0){
                x = mutableFloatListOf(-3F, -2F, -1F, 0F, 1F, 2F, 3F)
                y = listOf(mutableFloatListOf(-9F, -6F, -3F, 0F, 3F, 6F, 9F), mutableFloatListOf(9F, 4F, 1F, 0F, 1F, 4F, 9F),
                    mutableFloatListOf(-3F, -2F, -1F, 0F, 1F, 2F, 3F))
            }
            else {
                x = mutableFloatListOf(-2F, -1F, 0F, 1F, 2F)
                y = listOf(mutableFloatListOf(-6F, -3F, 0F, 3F, 6F), mutableFloatListOf(4F, 1F, 0F, 1F, 4F),
                    mutableFloatListOf(-2F, -1F, 0F, 1F, 2F))
            }
            ++fIdx

            val t0 = timestamps[0]/1000000
            for(i in timestamps.indices)
            {
                timestamps[i] = timestamps[i]/1000000 - t0
            }

            for(i in acceleration[0].indices)
            {
                acceleration[3].add(sqrt(acceleration[0][i] * acceleration[0][i] + acceleration[1][i] * acceleration[1][i] + acceleration[2][i] * acceleration[2][i]).toFloat())
            }
            xyChart.changeData(timestamps, acceleration)
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val message = "x = ${x.toInt()}\ny = ${y.toInt()}\nz = ${z.toInt()}"
            findViewById<TextView>(R.id.textView2).text = message
            if(isRecording())
            {
                timestamps.add(event.timestamp.toFloat())
                acceleration[0].add(x)
                acceleration[1].add(y)
                acceleration[2].add(z)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}
