package com.example.dailyshare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.example.dailyshare.adapters.CovidSparkAdapter
import com.example.dailyshare.models.CovidData
import com.google.gson.GsonBuilder
import com.robinhood.spark.SparkView
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import org.angmarch.views.NiceSpinner
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "COVIDActivity"
private const val BASE_URL = "https://api.covidtracking.com/v1/"
private const val NATIONAL = "All (Nationwide)"
class COVIDActivity : AppCompatActivity() {
    private lateinit var currentShowingDay: List<CovidData>
    private lateinit var adapter: CovidSparkAdapter
    private lateinit var stateDailyData: Map<String, List<CovidData>>
    private lateinit var nationalDailyData: List<CovidData>

    private lateinit var rgStatus: RadioGroup
    private lateinit var rgTimeline: RadioGroup
    private lateinit var rbPositive: RadioButton
    private lateinit var rbNegative: RadioButton
    private lateinit var rbDeath: RadioButton
    private lateinit var rbWeek: RadioButton
    private lateinit var rbMonth: RadioButton
    private lateinit var rbMax: RadioButton
    private lateinit var sparkView: SparkView
    private lateinit var spinnerStates : NiceSpinner
    private lateinit var tickerViewData : TickerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_covidactivity)

        rgStatus = findViewById(R.id.rgStatus)
        rgTimeline = findViewById(R.id.rgTimeline)
        rbPositive = findViewById(R.id.rbPositive)
        rbNegative = findViewById(R.id.rbNegative)
        rbDeath = findViewById(R.id.rbDeath)
        rbWeek = findViewById(R.id.rbWeek)
        rbMax = findViewById(R.id.rbMax)
        rbMonth = findViewById(R.id.rbMonth)
        sparkView = findViewById(R.id.sparkviewCovidChart)
        spinnerStates = findViewById(R.id.spinnerStates)
        tickerViewData = findViewById(R.id.tickerViewNumber)


        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").create()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val covidService = retrofit.create(CovidService::class.java)

        //get the national data
        covidService.getNationalData().enqueue(object : Callback<List<CovidData>>{
            override fun onResponse(
                call: Call<List<CovidData>>,
                response: Response<List<CovidData>>
            ) {
                Log.i(TAG, "onResponse $response")
                val nationalData = response.body()
                //check if we get valid data
                if (nationalData == null){
                    Log.w(TAG, "Didn't get valid data for national level")
                    return
                }
                setUpEventListener()
                nationalDailyData = nationalData.reversed()
                updateDisplayWithData(nationalDailyData)
            }

            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e(TAG, "onFailure $t")
            }

        })

        //get the state data
        covidService.getStatesData().enqueue(object : Callback<List<CovidData>>{
            override fun onResponse(
                call: Call<List<CovidData>>,
                response: Response<List<CovidData>>
            ) {
                Log.i(TAG, "onResponse $response")
                val stateData = response.body()
                //check if we get valid data
                if (stateData == null){
                    Log.w(TAG, "Didn't get valid data for state level")
                    return
                }
                stateDailyData = stateData.reversed().groupBy { it.state }
                updateSpinnerWithStatesData(stateDailyData.keys)
            }

            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e(TAG, "onFailure $t")
            }

        })
    }

    private fun updateSpinnerWithStatesData(statesNames: Set<String>) {
        val statesList = statesNames.toMutableList()
        statesList.sort()
        statesList.add(0, NATIONAL)
        Log.i(TAG, statesList.toString())

        spinnerStates.attachDataSource(statesList)
        spinnerStates.setOnSpinnerItemSelectedListener { parent, _, position, _ ->
            val selectedState = parent.getItemAtPosition(position) as String
            //
            val selectedData = stateDailyData[selectedState] ?: nationalDailyData
            updateDisplayWithData(selectedData)
        }
    }

    /*
    Allow users to scrub through the chart to get data
    Get data corresponds to what user choose in radio button
     */
    private fun setUpEventListener() {
        //Set up the ticker view with a number list
        tickerViewData.setCharacterLists(TickerUtils.provideNumberList())
        //To add a listener which allow user to scrub on the chart
        sparkView.isScrubEnabled = true
        sparkView.setScrubListener { itemData ->
            if (itemData is CovidData){
                updateDateData(itemData)
            }
        }
        //Show the data according to selected radio button
        rgTimeline.setOnCheckedChangeListener { _, checkedId ->
            adapter.timeline = when (checkedId){
                R.id.rbWeek -> ChartOptions.Timeline.WEEK
                R.id.rbMonth -> ChartOptions.Timeline.MONTH
                else -> ChartOptions.Timeline.MAX
            }
            adapter.notifyDataSetChanged()
            updateDateData(currentShowingDay.last())
        }

        rgStatus.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId){
                R.id.rbDeath -> updateChartContent(ChartOptions.Status.DEATH)
                R.id.rbNegative -> updateChartContent(ChartOptions.Status.NEGATIVE)
                else -> updateChartContent(ChartOptions.Status.POSITIVE)
            }
        }
    }

    //update the content in the spark chart
    private fun updateChartContent(status: ChartOptions.Status) {
        //change display color of the chart
        val colorShown = when(status){
            ChartOptions.Status.POSITIVE -> R.color.colorPositive
            ChartOptions.Status.NEGATIVE -> R.color.colorNegative
            ChartOptions.Status.DEATH -> R.color.colorDeath
        }
        @ColorInt val colorInt = ContextCompat.getColor(this, colorShown)
        sparkView.lineColor = colorInt
        tickerViewData.textColor = colorInt

        //set the status of data
        adapter.status = status
        adapter.notifyDataSetChanged()

        //Reset the bottom date and number
        updateDateData(currentShowingDay.last())
    }

    //Update the UI with data
    private fun updateDisplayWithData(dailyData: List<CovidData>) {
        currentShowingDay = dailyData
        adapter = CovidSparkAdapter(dailyData)
        sparkView.adapter = adapter

        //select positive and max radio buttons by default
        rbPositive.isChecked = true
        rbMax.isChecked = true

        //show the last data by default
        updateChartContent(ChartOptions.Status.POSITIVE)
    }

    //Update the date and number in the bottom of UI
    private fun updateDateData(day: CovidData) {

        val tvDate = findViewById<TextView>(R.id.tvDateLebal)

        val curStatus = when (adapter.status){
            ChartOptions.Status.DEATH -> day.deathIncrease
            ChartOptions.Status.NEGATIVE -> day.negativeIncrease
            ChartOptions.Status.POSITIVE -> day.positiveIncrease
        }

        tickerViewData.text = NumberFormat.getInstance().format(curStatus)
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        tvDate.text = dateFormat.format(day.dateChecked)
    }
}