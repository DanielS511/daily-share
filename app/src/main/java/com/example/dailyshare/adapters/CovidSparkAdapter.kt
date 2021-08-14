package com.example.dailyshare.adapters
/*
This class is an adapter to turn a list of CovidData Object into a Spark view
It extends the SparkAdapter Class
 */
import android.graphics.RectF
import com.example.dailyshare.ChartOptions
import com.example.dailyshare.models.CovidData
import com.robinhood.spark.SparkAdapter

class CovidSparkAdapter(dailyData: List<CovidData>) : SparkAdapter() {
    private val data = dailyData
    var status = ChartOptions.Status.POSITIVE
    var timeline = ChartOptions.Timeline.MAX
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(index: Int): Any {
        return data[index]
    }

    override fun getY(index: Int): Float {
        return when (status){
            ChartOptions.Status.POSITIVE -> data[index].positiveIncrease.toFloat()
            ChartOptions.Status.NEGATIVE -> data[index].negativeIncrease.toFloat()
            ChartOptions.Status.DEATH -> data[index].deathIncrease.toFloat()
        }
    }

    override fun getDataBounds(): RectF {
        val bounds =  super.getDataBounds()

        //if the user choose week or month, we need to reduce display period
        if (timeline != ChartOptions.Timeline.MAX){
            bounds.left = count - timeline.days.toFloat()
        }

        return bounds
    }

}
