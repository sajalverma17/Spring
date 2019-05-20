package com.rarecase.spring

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter

class TabViewPagerAdapter(fm: android.support.v4.app.FragmentManager?) : FragmentPagerAdapter(fm) {


    var activityList = mutableListOf<Fragment>()
    var titleList = mutableListOf<String>()

    fun addFragment(fragment: Fragment, title : String){
        activityList.add(fragment)
        titleList.add(title)
    }

    override fun getItem(position: Int): Fragment {
        return activityList[position]
    }

    override fun getCount(): Int {
        return activityList.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titleList.get(position)
    }
}