package com.bustasirio.spotifyapi.core

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.bustasirio.spotifyapi.R
import com.bustasirio.spotifyapi.data.model.AuthorizationModel
import com.bustasirio.spotifyapi.data.model.Playlist
import com.bustasirio.spotifyapi.data.model.Track
import com.bustasirio.spotifyapi.ui.view.fragments.PlaylistFragment
import com.bustasirio.spotifyapi.ui.view.fragments.SavedFragment
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


fun fragTransSaved(activity: FragmentActivity, key: String, type: String) {
    val fragment = SavedFragment()

    val bundle = Bundle()
    bundle.putString(key, type)
    fragment.arguments = bundle

    replaceFrag(activity, fragment)
}


fun errorToast(it: Int?, context: Context) {
    if (it != null) {
        Toast.makeText(context, "Error: $it, try again later.", Toast.LENGTH_SHORT)
            .show()
    } else {
        Toast.makeText(context, "Error. Try again later.", Toast.LENGTH_SHORT)
            .show()
    }
}


fun saveTokens(it: AuthorizationModel, context: Context) {
    val sharedPrefs = context.getSharedPreferences(
        context.getString(R.string.preference_file_key),
        Context.MODE_PRIVATE
    )
    with(sharedPrefs.edit()) {
        putString(
            context.getString(R.string.spotify_access_token),
            it.accessToken
        )
        putString(
            context.getString(R.string.spotify_token_type),
            it.tokenType
        )
        putBoolean(context.getString(R.string.spotify_logged), true)
        apply()
    }
}