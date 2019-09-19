package com.vincent.threecard

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
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

    private val handler: Handler by lazy {
        @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                // 检查是否初始化完毕
                val result = PreferenceManager.getDefaultSharedPreferences(application).getBoolean(init, false)
                if (result) {
                    // 关掉对话框
                    progressDialog.dismiss()
                } else {
                    // 下一秒钟继续检查初始化状态
                    sendEmptyMessageDelayed(100,1000)
                }
            }
        }
    }
    private val progressDialog: AlertDialog by lazy {
        AlertDialog.Builder(this, R.style.NoBackGroundDialog)
            .setView(R.layout.dialog_layout)
            .setCancelable(false)
            .create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 设置状态栏透明
        StatusBarUtil.setTranslucent(this, 0x99)
        initData()
        initEvent()

    }

    @SuppressLint("SetTextI18n")
    private fun initEvent() {

        tv_choose.setOnClickListener {
            // 选择牌型
            chooseCard()
        }
        tv_random.setOnClickListener {
             // 随机生成一手牌
            val a = (1..Total).random()
            var b = (1..Total).random()
            // 52张牌当中不能有重复
            while (a == b) {
                b = (1..Total).random()
            }
            var c = (1..Total).random()
            // 52张牌当中不能有重复
            while (a == c || b == c) {
                c = (1..Total).random()
            }
            Log.d("TAG", "a:$a  b:$b  c:$c")
            iv_first.setImageResource(getImgRes(a))
            iv_second.setImageResource(getImgRes(b))
            iv_third.setImageResource(getImgRes(c))
            // 计算概率
            calculateProbability(CardType(a, b, c))
        }
        tv_clean.setOnClickListener {
            // 重置牌型
            iv_first.setImageResource(R.mipmap.default_card)
            iv_second.setImageResource(R.mipmap.default_card)
            iv_third.setImageResource(R.mipmap.default_card)
            tv_result.text = "在牌型中排位是 \n得胜率为 "
        }


    }

    private fun initData() {
        // 判断牌型数据是否初始化
        val isInit = PreferenceManager.getDefaultSharedPreferences(application).getBoolean(init, false)
        if (!isInit) {
            // 开启Services进行数据初始化
            startService(Intent(this, CardServices::class.java))
            // 对话框点击外面不可关闭
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()
            // 一秒钟后检查牌型初始化是否结束
            handler.sendEmptyMessageDelayed(100,1000)
        }
        // 初始化52 张牌的数据
        for (i in 1..52) {
            cards.add(makeCard(i))
        }
    }

    // 生成扑克牌
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

    /**
     * 选择牌型
     */
    private fun chooseCard() {
        // 选择之前进行判断
        if (!PreferenceManager.getDefaultSharedPreferences(application).getBoolean(init, false)) {
            Toast.makeText(this, "数据正在初始化，请稍候！", Toast.LENGTH_SHORT).show()
            return
        }
        // 设置选择类型为非时间类型
        val pickerOptions = PickerOptions(PickerOptions.TYPE_PICKER_OPTIONS)
        pickerOptions.context = this
        // 确定选择数据
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
    @SuppressLint("SetTextI18n")
    private fun calculateProbability(card: CardType) {
        val result =
            LitePal.where("type = ? and max = ?", card.getType().toString(), card.getMax().toString()).find<CardType>()
        for (item in result) {
            if (card.getTotalCard() == item.getTotalCard()) {
                val text = "在牌型中排位是${item.number}\n得胜率为${String.format("%.2f", item.number.toFloat() / 22100 * 100)}%"
                val span = SpannableString(text)
                val startIndex = text.indexOf(item.number.toString())
                span.setSpan(ForegroundColorSpan(Color.RED),startIndex,startIndex+item.number.toString().length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                span.setSpan(ForegroundColorSpan(Color.RED),text.indexOf("为")+1,text.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                tv_result.text = span
                return
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
