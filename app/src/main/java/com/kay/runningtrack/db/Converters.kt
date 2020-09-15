package com.kay.runningtrack.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {

    @TypeConverter
    fun byteArraytoBitmap(byteArray: ByteArray):Bitmap{
        return BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
    }



    @TypeConverter
    fun fromBitmapToByteArray(bmp : Bitmap):ByteArray{
        val os = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG,100,os)
        return os.toByteArray()
    }

}