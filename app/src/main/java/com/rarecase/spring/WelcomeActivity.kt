package com.rarecase.spring

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import android.widget.Button
import com.rarecase.utils.SpringSharedPref

class WelcomeActivity : AppCompatActivity() {

    lateinit var pref : SpringSharedPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pref = SpringSharedPref(this)
        if (pref.isFirstTime) run {
            setContentView(R.layout.activity_welcome)

            val viewPager = findViewById(R.id.intro_slider_ViewPager) as ViewPager
            val viewPagerAdapter = IntroViewPagerAdapter(this, findViewById(R.id.activityWelcomeLayout))
            viewPager.adapter = viewPagerAdapter

            val btnSkip = findViewById(R.id.btn_skip) as Button
            btnSkip.setOnClickListener({ launchMainActivity() })
        }else{
            launchMainActivity()
        }
    }

    fun launchMainActivity(){
        pref.isFirstTime = false
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        this.finish()
    }
}
