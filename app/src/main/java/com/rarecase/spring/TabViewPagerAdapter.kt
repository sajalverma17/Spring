package com.rarecase.spring

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class TabViewPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {


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