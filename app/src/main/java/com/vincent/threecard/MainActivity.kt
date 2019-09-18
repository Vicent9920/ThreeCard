package com.vincent.threecard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bigkoo.pickerview.configure.PickerOptions
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.jaeger.library.StatusBarUtil
import com.vincent.threecard.dao.CardType
import com.vincent.threecard.picker.OptionsPickerView
import com.vincent.threecard.services.CardServices
import com.vincent.threecard.services.Total
import com.vincent.threecard.services.init
import kotlinx.android.synthetic.main.activity_main.*
import org.litepal.LitePal
import org.litepal.extension.find
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity() {

    private val cards = mutableListOf<String>()

    private lateinit var handler:Handler
    private lateinit var progressDialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StatusBarUtil.setTranslucent(this, 0x99)
        initData()
        initEvent()

    }

    private fun initEvent() {
        tv_choose.setOnClickListener {
            chooseCard()
        }
        tv_random.setOnClickListener {

            val a = (1..Total).random()
            var b = (1..Total).random()
            while (a == b) {
                b = (1..Total).random()
            }
            var c = (1..Total).random()
            while (a == c || b == c) {
                c = (1..Total).random()
            }
            Log.e("TAG","a:$a  b:$b  c:$c")
            iv_first.setImageResource(getImgRes(a))
            iv_second.setImageResource(getImgRes(b))
            iv_third.setImageResource(getImgRes(c))
            calculateProbability(CardType(a, b, c))
        }
        tv_clean.setOnClickListener {
            iv_first.setImageResource(getImgRes(0))
            iv_second.setImageResource(getImgRes(0))
            iv_third.setImageResource(getImgRes(0))
            tv_result.text = "在牌型中排位是 \n得胜率为 "
        }
        handler = @SuppressLint("HandlerLeak")
        object :Handler(){
            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                val result = PreferenceManager.getDefaultSharedPreferences(application).getBoolean(init, false)
                if(result){
                    progressDialog.dismiss()
                }else{
                    sendEmptyMessage(100)
                }
            }
        }
    }

    private fun initData() {
        val isInit = PreferenceManager.getDefaultSharedPreferences(application).getBoolean(init, false)
        if (!isInit) {
            startService(Intent(this, CardServices::class.java))
            progressDialog = AlertDialog.Builder(this,R.style.NoBackGroundDialog)
                .setView(R.layout.dialog_layout)
                .show()
            progressDialog.setCancelable(false)
            progressDialog.setCanceledOnTouchOutside(false)
            handler.sendEmptyMessage(100)
        }
        for (i in 1..52) {
            cards.add(makeCard(i))
        }
    }

    private fun makeCard(i: Int): String {
        var number = if (i % 4 != 0) {
            i / 4 + 1
        } else {
            i / 4
        }
        if (i < 5) {
            number += 13
        }
        val value = when (number) {
            14 -> "A"
            13 -> "K"
            12 -> "Q"
            11 -> "J"
            else -> number.toString()
        }
        return when (i % 4) {
            3 -> "黑桃$value"
            2 -> "梅花$value"
            1 -> "方块$value"
            else -> "红桃$value"
        }

    }

    private fun chooseCard() {

        if (!PreferenceManager.getDefaultSharedPreferences(application).getBoolean(init, false)) {
            Toast.makeText(this, "数据正在实例化，请稍候！", Toast.LENGTH_SHORT).show()
            return
        }

        val pickerOptions = PickerOptions(PickerOptions.TYPE_PICKER_OPTIONS)
        pickerOptions.context = this
        pickerOptions.optionsSelectListener = OnOptionsSelectListener { options1, option2, options3, _ ->

            iv_first.setImageResource(getImgRes(options1 + 1))
            iv_second.setImageResource(getImgRes(option2 + 1))
            iv_third.setImageResource(getImgRes(options3 + 1))
            val card = CardType(options1 + 1, option2 + 1, options3 + 1)
            calculateProbability(card)
        }
        val pvOptions = OptionsPickerView<String>(pickerOptions)
        //条件选择器
        pvOptions.setPicker(cards)
        pvOptions.show()
    }

    /**
     * 计算牌型概率
     */
    private fun calculateProbability(card: CardType) {
        val result =
            LitePal.where("type = ? and max = ?", card.getType().toString(), card.getMax().toString()).find<CardType>()
        for (item in result) {
            if (card.getTotalCard() == item.getTotalCard()) {
                tv_result.text =
                    "在牌型中排位是${item.number}\n得胜率为${String.format("%.2f", item.number.toFloat() / 22100 * 100)}% "
                break
            }
        }
    }

    private fun getImgRes(position: Int): Int {
        return when (position) {
            1 -> R.mipmap.card_1_0
            5 -> R.mipmap.card_1_1
            9 -> R.mipmap.card_1_2
            13 -> R.mipmap.card_1_3
            17 -> R.mipmap.card_1_4
            21 -> R.mipmap.card_1_5
            25 -> R.mipmap.card_1_6
            29 -> R.mipmap.card_1_7
            33 -> R.mipmap.card_1_8
            37 -> R.mipmap.card_1_9
            41 -> R.mipmap.card_1_10
            45 -> R.mipmap.card_1_11
            49 -> R.mipmap.card_1_12
            2 -> R.mipmap.card_3_0
            6 -> R.mipmap.card_3_1
            10 -> R.mipmap.card_3_2
            14 -> R.mipmap.card_3_3
            18 -> R.mipmap.card_3_4
            22 -> R.mipmap.card_3_5
            26 -> R.mipmap.card_3_6
            30 -> R.mipmap.card_3_7
            34 -> R.mipmap.card_3_8
            38 -> R.mipmap.card_3_9
            42 -> R.mipmap.card_3_10
            46 -> R.mipmap.card_3_11
            50 -> R.mipmap.card_3_12
            3 -> R.mipmap.card_0_0
            7 -> R.mipmap.card_0_1
            11 -> R.mipmap.card_0_2
            15 -> R.mipmap.card_0_3
            19 -> R.mipmap.card_0_4
            23 -> R.mipmap.card_0_5
            27 -> R.mipmap.card_0_6
            31 -> R.mipmap.card_0_7
            35 -> R.mipmap.card_0_8
            39 -> R.mipmap.card_0_9
            43 -> R.mipmap.card_0_10
            47 -> R.mipmap.card_0_11
            51 -> R.mipmap.card_0_12
            4 -> R.mipmap.card_2_0
            8 -> R.mipmap.card_2_1
            12 -> R.mipmap.card_2_2
            16 -> R.mipmap.card_2_3
            20 -> R.mipmap.card_2_4
            24 -> R.mipmap.card_2_5
            28 -> R.mipmap.card_2_6
            32 -> R.mipmap.card_2_7
            36 -> R.mipmap.card_2_8
            40 -> R.mipmap.card_2_9
            44 -> R.mipmap.card_2_10
            48 -> R.mipmap.card_2_11
            52 -> R.mipmap.card_2_12
            else -> R.mipmap.default_card
        }
    }
}
