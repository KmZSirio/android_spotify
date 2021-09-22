package com.bustasirio.spotifyapi.core

import android.content.res.Resources
import android.graphics.*
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.core.util.Preconditions.checkArgument
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.data.model.Playlist
import com.bustasirio.spotifyapi.data.model.Track
import com.bustasirio.spotifyapi.ui.view.fragments.CreateFragment
import com.bustasirio.spotifyapi.ui.view.fragments.LibraryFragment
import com.bustasirio.spotifyapi.ui.view.fragments.PlaylistFragment
import okhttp3.MediaType
import okhttp3.RequestBody
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
    canvas.drawCircle(
        bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2,
        bitmap.width.toFloat() / 2, paint
    )
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, rect, rect, paint)
    return output
}


// * When creating Frag D (Playlist) and navigating between A B C's (Nav menu fragments)
// * you could see the Frag D between transitions, tried deleting it from the stack with
// * TAG and id, finally made up this fun to remove the exact fragment instance
fun removeAnnoyingFrag(supportFragManager: FragmentManager): Boolean {
    val sizeFrags = supportFragManager.fragments.size
    if (sizeFrags > 1) {
        val fragToDelete = supportFragManager.fragments[1]
        supportFragManager.beginTransaction()
            .remove(fragToDelete).commit()
        return true
    }
    return false
}


fun convertDpToPx(dp: Int, resources: Resources): Int =
    (dp * (resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()


fun replaceFrag(activity: FragmentActivity, fragment: Fragment) {
    val transaction = activity.supportFragmentManager.beginTransaction()
    transaction.replace(R.id.nav_host_fragment_activity_lobby, fragment)
    transaction.disallowAddToBackStack()
    transaction.commit()
}


fun tracksToJson(tracks: List<Track>): RequestBody {
    var json = "{\"uris\":["

    tracks.forEach {
        json = "$json\"${it.uri}\","
    }

    json = json.dropLast(1)
    json = "$json]}"
    return RequestBody.create(MediaType.parse("text/plain"), json)
}


fun fragTransPlaylist(activity: FragmentActivity, key: String, playlist: Playlist) {
    val fragment = PlaylistFragment()

    val bundle = Bundle()
    bundle.putParcelable(key, playlist)
    fragment.arguments = bundle

    replaceFrag(activity, fragment)
}