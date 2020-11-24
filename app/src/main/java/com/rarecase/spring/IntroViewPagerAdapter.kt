package com.rarecase.spring

import android.content.Context
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class IntroViewPagerAdapter(val context: Context,activityWelcomeLayout: View) : PagerAdapter() {

    var arrayIntroSlidePages: IntArray = intArrayOf(R.layout.welcome_slide1,R.layout.welcome_slide2,R.layout.welcome_slide3,R.layout.welcome_slide4)

    //References to dot resources used in updateSliderDots()
    var dots : Array<TextView>
    var arrayColorsDotsActive : IntArray
    var arrayColorsDotsInactive : IntArray
    var layoutDots : LinearLayout
    val btnNext: Button

    init {
        //Add Layout id in this array if you want another intro slider page

        btnNext = activityWelcomeLayout.findViewById(R.id.btn_next) as Button
        val viewPager = activityWelcomeLayout.findViewById(R.id.intro_slider_ViewPager) as ViewPager

        //Get Dot resources
        dots = Array(arrayIntroSlidePages.size,{TextView(context)}) //TextViews as dots
        arrayColorsDotsActive = context.resources.getIntArray(R.array.array_dot_active)
        arrayColorsDotsInactive = context.resources.getIntArray(R.array.array_dot_inactive)
        layoutDots = activityWelcomeLayout.findViewById(R.id.layoutDots) as LinearLayout //Layout that will contain TextViews as dots

        //Onclick
        btnNext.setOnClickListener({
            //Starting a new activity ony when GOT IT clicked
            if(btnNext.text == "GOT IT!"){

                (context as WelcomeActivity).launchMainActivity()

            }
            else
            {
                viewPager.currentItem = viewPager.currentItem + 1
            }
        })

        updateSliderDots(0)
        viewPager.setOnPageChangeListener(PageChangeListener())
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflator : LayoutInflater = LayoutInflater.from(context)
        val introPageView : View = layoutInflator.inflate(arrayIntroSlidePages[position],container,false)
        container.addView(introPageView)
        return introPageView
    }

    override fun getCount(): Int {
        return arrayIntroSlidePages.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    //Listens to page changes either by btnNext click or swiping
    inner class PageChangeListener : ViewPager.OnPageChangeListener{
        override fun onPageScrollStateChanged(state: Int) {

        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

        }

        override fun onPageSelected(position: Int) {
            updateLayout(position)

        }

    }

    //Runs every time the IntroSlider is changed either by swiping or by btnNext.OnClickListener
    //Updates the slider dots at bottom and the Next button text
    private fun updateLayout(position : Int): Unit{
        updateSliderDots(position);
        if (position == arrayIntroSlidePages.size - 1) {
            btnNext.setText(R.string.gotit)
        }else{
            btnNext.setText(R.string.next)
        }
    }

    //Set of Dot colors for each welcome slide. Updates
    private fun updateSliderDots(position: Int): Unit{
        //Resources made global so that they aren't fetched by this method on every slide change
        layoutDots.removeAllViews()
        //First Making all dots inactive
        for (dot in dots){
            dot.setText(Html.fromHtml("&#8226;"))
            dot.setTextSize(35F)
            dot.setTextColor(arrayColorsDotsInactive[position])
            layoutDots.addView(dot)
        }
        //Making dot TextView at current position look active
        if(dots.isNotEmpty()){
            dots[position].setTextColor(arrayColorsDotsActive[position])
        }

    }





}