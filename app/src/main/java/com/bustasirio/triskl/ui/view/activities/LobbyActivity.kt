package com.bustasirio.triskl.ui.view.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bustasirio.triskl.R
import com.bustasirio.triskl.core.removeAnnoyingFrag
import com.bustasirio.triskl.databinding.ActivityLobbyBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LobbyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLobbyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_lobby)
        navController.setGraph(R.navigation.mobile_navigation)
        navView.setupWithNavController(navController)
    }

    // * New approach, users either using my back button or android's will call removeAnnoyingFrag :)
    override fun onBackPressed() {
        val count = supportFragmentManager.fragments.size
        if (count < 2) super.onBackPressed()
        else removeAnnoyingFrag(supportFragmentManager)
    }
}