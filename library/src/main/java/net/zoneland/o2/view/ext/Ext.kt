package net.zoneland.o2.view.ext

import android.support.annotation.ColorInt

/**
 * Created by fancyLou on 06/07/2018.
 */

object Ext {

    fun color2RGB(@ColorInt color:Int): IntArray = intArrayOf((color and 0xff0000 shr 16), (color and 0x00ff00 shr 8), (color and 0x0000ff))

    fun isDarkColor(@ColorInt color: Int): Boolean {
        val a = color2RGB(color)
        val grayLevel = a[0]*0.299 + a[1]*0.587 + a[2]*0.114
        if(grayLevel >= 192){
            return false
        }
        return true
    }

}

