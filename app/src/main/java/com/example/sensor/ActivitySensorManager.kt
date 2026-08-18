package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.data.model.ActivityLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class ActivitySensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val accelSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()

    private val _activityLevel = MutableStateFlow(ActivityLevel.SEDENTARY)
    val activityLevel: StateFlow<ActivityLevel> = _activityLevel.asStateFlow()

    private val _sensorAvailable = MutableStateFlow(false)
    val sensorAvailable: StateFlow<Boolean> = _sensorAvailable.asStateFlow()

    private var initialStepCount = -1
    private var lastAccelMagnitude = 9.8f
    private var simulatedStepsFromAccel = 0

    fun startListening() {
        if (sensorManager == null) return

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
            _sensorAvailable.value = true
        } else if (accelSensor != null) {
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL)
            _sensorAvailable.value = true
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val total = event.values[0].toInt()
                if (initialStepCount < 0) {
                    initialStepCount = total
                }
                val todaySteps = (total - initialStepCount).coerceAtLeast(0)
                _stepCount.value = todaySteps
                _activityLevel.value = ActivityLevel.fromSteps(todaySteps)
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val delta = kotlin.math.abs(magnitude - lastAccelMagnitude)
                lastAccelMagnitude = magnitude

                // Detect step peak
                if (delta > 2.8f) {
                    simulatedStepsFromAccel++
                    if (simulatedStepsFromAccel % 2 == 0) {
                        _stepCount.value = simulatedStepsFromAccel
                        _activityLevel.value = ActivityLevel.fromSteps(simulatedStepsFromAccel)
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setSimulatedSteps(steps: Int) {
        _stepCount.value = steps
        _activityLevel.value = ActivityLevel.fromSteps(steps)
    }
}
