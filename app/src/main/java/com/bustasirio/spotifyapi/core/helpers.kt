package com.bustasirio.spotifyapi.core

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.bustasirio.spotifyapi.R
import android.util.DisplayMetrics
import kotlin.math.roundToInt


// * Make circular an image
// * Author Praveen stackoverflow.com/questions/3035692/how-to-convert-a-drawable-to-a-bitmap
/* *
*    val icon = BitmapFactory.decodeResource(
*       requireContext().resources,
*       R.drawable.no_image
*    )
* */
fun getCroppedBitmap(bitmap: Bitmap): Bitmap? {
    val output = Bitmap.createBitmap(
        bitmap.width,
        bitmap.height, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(output)
    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, bitmap.width, bitmap.height)
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
    canvas.drawCircle(
        bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2,
        bitmap.width.toFloat() / 2, paint
    )
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, rect, rect, paint)
    //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
    //return _bmp;
    return output
}


// * When creating Frag D (Playlist) and navigating between A B C's (Nav menu fragments)
// * you could see the Frag D between transitions, tried deleting it from the stack with
// * TAG and id, finally made up this fun to remove the exact fragment instance
fun removeAnnoyingFrag(activity: FragmentActivity): Boolean {
    val sizeFrags = activity.supportFragmentManager.fragments.size
    if (sizeFrags > 1) {
        val fragToDelete = activity.supportFragmentManager.fragments[1]
        activity.supportFragmentManager.beginTransaction()
            .remove(fragToDelete).commit()
        return true
    }
    return false
}

fun convertDpToPx(dp: Int, resources: Resources): Int = (dp * (resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()

