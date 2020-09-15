package com.kay.runningtrack.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kay.runningtrack.util.Constants.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class Run(
    var img: Bitmap? = null,
    var timestamp:Long = 0L,
    var avgSpeedInKMH:Float = 0F,
    var distanceInMeters:Int = 0,
    var timeInMillis:Long = 0L,
    var caloriesBurned:Int = 0
){
    @PrimaryKey(autoGenerate = true)
    var id:Int?=null
}