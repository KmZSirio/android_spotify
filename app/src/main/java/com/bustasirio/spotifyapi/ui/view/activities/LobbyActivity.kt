package com.bustasirio.spotifyapi.ui.view.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bustasirio.spotifyapi.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.bustasirio.spotifyapi.databinding.ActivityLobbyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LobbyActivity : AppCompatActivity() {

    // FIXME Creating and recreating fragments trigger unnecessary calls
    private lateinit var binding: ActivityLobbyBinding

//    private val lobbyActivityViewModel: LobbyActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_lobby)
        navController.setGraph(R.navigation.mobile_navigation)
        navView.setupWithNavController(navController)
    }

//    override fun onBackPressed() {
//        // * To prevent that when you minimize the app on lobby, the login appears again when maximizing
//        val setIntent = Intent(Intent.ACTION_MAIN)
//        setIntent.addCategory(Intent.CATEGORY_HOME)
//        setIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        startActivity(setIntent)
//    }
}