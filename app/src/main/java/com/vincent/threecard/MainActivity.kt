package com.vincent.threecard

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import com.jaeger.library.StatusBarUtil
import com.vincent.threecard.services.CardServices
import com.vincent.threecard.services.init
import kotlinx.android.synthetic.main.activity_main.*
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.configure.PickerOptions
import com.bigkoo.pickerview.view.OptionsPickerView
import com.vincent.threecard.dao.Card
import com.vincent.threecard.dao.CardType
import com.vincent.threecard.picker.CustomWheelOptions


class MainActivity : AppCompatActivity() {
    var isChoose = false
    val cards = mutableListOf<Card>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StatusBarUtil.setTranslucent(this, 0x99)
        initData()

        tv_choose.setOnClickListener {
            chooseCard()
        }
    }

    private fun initData() {
        val isInit = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(init, false)
        if(!isInit){
            startService(Intent(this,CardServices::class.java))
        }
        for (i in 1..52){
            cards.add(makeCard(i))
        }
    }
    private fun makeCard(i:Int): Card {
        var number = if (i % 4 != 0) {
            i / 4 + 1
        } else {
            i / 4
        }
        val index = if(i < 5){
            number += 12
            i+52
        } else {
            i
        }
        val value = when(number){
            14 -> "A"
            13 -> "K"
            12 -> "Q"
            11 -> "J"
            else -> number.toString()
        }
        val name = when (i % 4) {
            3 -> "黑桃${value}"
            2 -> "梅花${value}"
            1 -> "方块${value}"
            else -> "红桃${value}"
        }
        return Card(name,index)
    }
    private fun chooseCard() {
        // 防止双击
        if(isChoose)return
        val pickerOptions =  PickerOptions(PickerOptions.TYPE_PICKER_OPTIONS)
        pickerOptions.context = this
        pickerOptions.optionsSelectListener = OnOptionsSelectListener { options1, option2, options3, v -> println() }
        val pvOptions = com.vincent.threecard.picker.OptionsPickerView<Card>(pickerOptions)
        //条件选择器
        pvOptions.setPicker(cards)
        pvOptions.show()
    }
}
