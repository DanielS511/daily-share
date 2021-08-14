package com.example.dailyshare
/*
This class represent the options of radio buttons
 */
class ChartOptions {
    //User can choose from negative, positive and death in status radio group
    enum class Status{
        NEGATIVE,POSITIVE,DEATH
    }
    //User can choose from week, month and max in status radio group
    enum class Timeline(val days: Int){
        WEEK(7),
        MONTH(30),
        MAX(-1)
    }
}