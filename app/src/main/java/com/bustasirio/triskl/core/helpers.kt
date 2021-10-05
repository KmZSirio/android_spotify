package com.bustasirio.triskl.core

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.fragment.app.DialogFragment.STYLE_NO_FRAME
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.bustasirio.triskl.MyApplication
import com.bustasirio.triskl.R
import com.bustasirio.triskl.data.model.AuthorizationModel
import com.bustasirio.triskl.data.model.Playlist
import com.bustasirio.triskl.data.model.Track
import com.bustasirio.triskl.ui.view.fragments.BottomSheetFragment
import com.bustasirio.triskl.ui.view.fragments.PlaylistFragment
import com.bustasirio.triskl.ui.view.fragments.SavedFragment
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
        val fragToDelete = supportFragManager.fragments[sizeFrags - 1]
        supportFragManager.beginTransaction()
            .remove(fragToDelete).commit()
        return true
    }
    return false
}

// * Used on onCreateView methods from navbar frags
fun removeAnnoyingFrags(supportFragManager: FragmentManager): Boolean {
    when (supportFragManager.fragments.size) {
        2 -> {
            val fragToDelete = supportFragManager.fragments[1]
            supportFragManager.beginTransaction()
                .remove(fragToDelete).commit()
        }
        3 -> {
            val fragToDelete3 = supportFragManager.fragments[2]
            supportFragManager.beginTransaction()
                .remove(fragToDelete3).commit()
            val fragToDelete2 = supportFragManager.fragments[1]
            supportFragManager.beginTransaction()
                .remove(fragToDelete2).commit()
            return true
        }
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

fun addFrag(activity: FragmentActivity, fragment: Fragment) {
    val transaction = activity.supportFragmentManager.beginTransaction()
    transaction.add(R.id.nav_host_fragment_activity_lobby, fragment)
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


fun fragTransPlaylist(
    activity: FragmentActivity,
    keyPlaylist: String,
    playlist: Playlist,
    keyBoolean: String,
    isOwner: Boolean
) {
    val fragment = PlaylistFragment()

    val bundle = Bundle()
    bundle.putParcelable(keyPlaylist, playlist)
    bundle.putBoolean(keyBoolean, isOwner)
    fragment.arguments = bundle

    replaceFrag(activity, fragment)
}


fun fragAddPlaylist(activity: FragmentActivity, key: String, playlist: Playlist) {
    val fragment = PlaylistFragment()

    val bundle = Bundle()
    bundle.putParcelable(key, playlist)
    fragment.arguments = bundle

    addFrag(activity, fragment)
}


fun fragTransSaved(activity: FragmentActivity, key: String, type: String) {
    val fragment = SavedFragment()

    val bundle = Bundle()
    bundle.putString(key, type)
    fragment.arguments = bundle

    replaceFrag(activity, fragment)
}


fun showBottomSheet(activity: FragmentActivity, key: String, it: Track) {
    val bottomSheetFrag = BottomSheetFragment()
    bottomSheetFrag.setStyle(STYLE_NO_FRAME, R.style.SheetDialog)

    val bundle = Bundle()
    bundle.putParcelable(key, it)
    bottomSheetFrag.arguments = bundle

    bottomSheetFrag.show(activity.supportFragmentManager, "BottomSheetDialog")
}


fun errorToast(text: String, context: Context) {
    if (text == "empty")
        showToast(context, context.getString(R.string.no_data_create))
    if (text == "internet")
        showToast(context, context.getString(R.string.internet_problem))
    if (text == "network")
        showToast(context, context.getString(R.string.network_problem))
    if (text == "conversion")
        showToast(context, context.getString(R.string.conversion_problem))
    else
        showToast(context, context.getString(R.string.something_wrong))
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


fun reproduce(context: Context, text: String, url: String?) {
    if (url != null) PlayerSingleton.getPlayerInstance(context, url)!!.start()
    else {
        Toast.makeText(
            context,
            text,
            Toast.LENGTH_SHORT
        ).show()
    }
}


//        val size = screenSize(requireActivity())
//        behavior.peekHeight = (size.heightPixels * 0.7).toInt()
fun screenSize(activity: FragmentActivity): DisplayMetrics {
    val outMetrics = DisplayMetrics()

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        val display = activity.display
        display?.getRealMetrics(outMetrics)
    } else {
        @Suppress("DEPRECATION")
        val display = activity.windowManager.defaultDisplay
        @Suppress("DEPRECATION")
        display.getMetrics(outMetrics)
    }
    return outMetrics
}

fun showToast(context: Context, text: String, long: Boolean = false) =
    Toast.makeText(context, text, if(long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()

fun isItInstalled(name: String, packageManager: PackageManager): Boolean {
    return try {
        packageManager.getPackageInfo(name, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}


@SuppressLint("ObsoleteSdkInt")
@Suppress("DEPRECATION")
fun hasInternetConnection(connectivityManager: ConnectivityManager): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } else {
        connectivityManager.activeNetworkInfo?.run {
            return when(type) {
                ConnectivityManager.TYPE_WIFI -> true
                ConnectivityManager.TYPE_MOBILE -> true
                ConnectivityManager.TYPE_ETHERNET -> true
                else -> false
            }
        }
    }
    return false
}