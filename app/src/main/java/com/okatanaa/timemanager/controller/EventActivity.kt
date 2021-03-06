package com.okatanaa.timemanager.controller

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.okatanaa.timemanager.R
import com.okatanaa.timemanager.model.Event
import kotlinx.android.synthetic.main.activity_event.*
import android.app.Activity
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.okatanaa.timemanager.additional_classes.TextClickedListener
import com.okatanaa.timemanager.model.Time
import com.okatanaa.timemanager.services.JsonHelper
import com.okatanaa.timemanager.utilities.*
import kotlinx.android.synthetic.main.content_event.*
import org.json.JSONObject
import kotlin.math.round


class EventActivity : AppCompatActivity() {

    lateinit var event : Event
    val EVENT_NAME = "Event Name"
    val DESCRIPTION = "Description"

    var topTimeBorder = 0
    var bottomTimeBorder = Time.MINUTES_IN_DAY

    var currentStartTime = 0
    var currentEndTime = Time.MINUTES_IN_DAY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)
        setSupportActionBar(toolbar)
        setEventData()
        setTextViews()
        setTimeBars()
        setTimeBarListeners()
    }

    fun setEventData() {
        val eventJsonString = intent.getStringExtra(EXTRA_EVENT_JSON)
        val eventJson = JSONObject(eventJsonString)
        this.event = JsonHelper.eventFromJson(eventJson)
        this.topTimeBorder = intent.getIntExtra(EXTRA_TOP_TIME_BORDER, this.topTimeBorder)
        this.bottomTimeBorder = intent.getIntExtra(EXTRA_BOTTOM_TIME_BORDER, this.bottomTimeBorder)
        this.currentStartTime = event.startTime.toMinutes()
        this.currentEndTime = event.endTime.toMinutes()
    }

    fun setTextViews() {
        eventNameTxt.text = event.name
        eventNameTxt.setOnClickListener{TextClickedListener.onClick(this, this.EVENT_NAME, eventNameTxt.text.toString())}

        eventDescriptionTxt.text = event.description
        eventDescriptionTxt.setOnClickListener{TextClickedListener.onClick(this, this.DESCRIPTION, eventDescriptionTxt.text.toString())}
        inWhatDayTxt.text = event?.inDay.toString()

        startTimeDynamicTxt.text = this.event.startTime.toString()
        endTimeDynamicTxt.text = this.event.endTime.toString()
    }

    fun setTimeBars() {
        startTimeBar.progress = ((1.0 * (this.currentStartTime - this.topTimeBorder) / (this.currentEndTime - this.topTimeBorder)) * 100).toInt()
        endTimeBar.progress = ((1.0 * (this.currentEndTime - this.currentStartTime) / (this.bottomTimeBorder - this.currentStartTime)) * 100).toInt()
    }

    fun setTimeBarListeners() {
        startTimeBar.setOnSeekBarChangeListener(StartTimeBarListener())
        endTimeBar.setOnSeekBarChangeListener(EndTimeBarListener())
    }

    fun updateTimeFields() {
        startTimeDynamicTxt.text = Time(this.currentStartTime).toString()
        endTimeDynamicTxt.text = Time(this.currentEndTime).toString()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_event, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_delete_event -> actionDeleteEvent()
            R.id.action_save_event -> actionSaveEvent()
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun actionDeleteEvent(): Boolean {
        val resultIntent = Intent()
        resultIntent.putExtra(EXTRA_ACTION, ACTION_DELETE)
        setResult(Activity.RESULT_OK, resultIntent)
        println("Finish")
        finish()
        return true
    }

    fun actionSaveEvent(): Boolean {
        // Save changed data
        event.name = eventNameTxt.text.toString()
        event.description = eventDescriptionTxt.text.toString()
        event.smartSetStartTime(Time(this.currentStartTime))
        event.smartSetEndTime(Time(this.currentEndTime))
        val resultIntent = Intent()
        resultIntent.putExtra(EXTRA_EVENT_JSON, JsonHelper.eventToJson(event).toString())
        resultIntent.putExtra(EXTRA_ACTION, ACTION_SAVE)
        setResult(Activity.RESULT_OK, resultIntent)
        println("Finish")
        finish()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK)
            when(data?.getStringExtra(EXTRA_EDITED_NAME)) {
                EVENT_NAME->
                    eventNameTxt.text = data?.getStringExtra(EXTRA_EDITED_VALUE)
                DESCRIPTION ->
                    eventDescriptionTxt.text = data?.getStringExtra(EXTRA_EDITED_VALUE)
                else ->
                    Unit
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        setResult(Activity.RESULT_CANCELED, Intent())
        finish()
    }

    inner class StartTimeBarListener: SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            val timeBetweenCurrentAndTop = this@EventActivity.currentEndTime - this@EventActivity.topTimeBorder
            this@EventActivity.currentStartTime = this@EventActivity.topTimeBorder + (timeBetweenCurrentAndTop * 1.0 * progress / 100.0).toInt()
            this@EventActivity.updateTimeFields()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }

    }

    inner class EndTimeBarListener: SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            val timeBetweenCurrentAndBot = this@EventActivity.bottomTimeBorder - this@EventActivity.currentStartTime
            this@EventActivity.currentEndTime = this@EventActivity.currentStartTime + (timeBetweenCurrentAndBot * 1.0 * progress / 100.0).toInt()
            this@EventActivity.updateTimeFields()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }

    }

}
