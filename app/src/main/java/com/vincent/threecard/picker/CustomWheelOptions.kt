package com.vincent.threecard.picker

import android.graphics.Typeface
import android.view.View
import com.bigkoo.pickerview.R
import com.bigkoo.pickerview.adapter.ArrayWheelAdapter
import com.bigkoo.pickerview.listener.OnOptionsSelectChangeListener
import com.contrarywind.listener.OnItemSelectedListener
import com.contrarywind.view.WheelView

/**
 * <p>文件描述：自定义滚动View，重写的原因是无法提供默认三级列表数据 <p>
 * <p>@author 烤鱼<p>
 * <p>@date 2019/9/16 0016 <p>
 * <p>@update 2019/9/16 0016<p>
 * <p>版本号：1<p>
 *
 */
class CustomWheelOptions<T>( var view:View,  var isRestoreItem:Boolean) {
    private  val wv_option1: WheelView = view.findViewById(R.id.options1)
    private  val wv_option2: WheelView = view.findViewById(R.id.options2)
    private  val wv_option3: WheelView = view.findViewById(R.id.options3)
    private  var mOptions1Items:MutableList<T> = mutableListOf()
    private  var mOptions2Items:MutableList<T> = mutableListOf()
    private  var mOptions3Items:MutableList<T> = mutableListOf()
    private var linkage = true//默认联动

    //文字的颜色和分割线的颜色
    private var textColorOut: Int = 0
    private var textColorCenter: Int = 0
    private var dividerColor: Int = 0

    private lateinit var dividerType: WheelView.DividerType

    // 条目间距倍数
    private var lineSpacingMultiplier: Float = 0f

    private lateinit var wheelListener_option1: OnItemSelectedListener
    private lateinit var wheelListener_option2: OnItemSelectedListener

      var optionsSelectChangeListener: OnOptionsSelectChangeListener? = null

    fun setPicker(options1Items:MutableList<T>){
        this.mOptions1Items.addAll(options1Items)
        // 选项1
        wv_option1.adapter = ArrayWheelAdapter(mOptions1Items)// 设置显示数据
        wv_option1.currentItem = 0// 初始化时显示的数据

        mOptions2Items.addAll(options1Items)
        mOptions2Items.removeAt(0)
        wv_option2.adapter = ArrayWheelAdapter(mOptions2Items)// 设置显示数据
        wv_option2.currentItem = wv_option2.currentItem// 初始化时显示的数据

        // 选项3
        mOptions3Items.addAll(options1Items)
        mOptions3Items.removeAt(0)
        mOptions3Items.removeAt(0)
        wv_option3.adapter = ArrayWheelAdapter(mOptions3Items)// 设置显示数据
        wv_option3.currentItem = wv_option3.currentItem

        wv_option1.setIsOptions(true)
        wv_option2.setIsOptions(true)
        wv_option3.setIsOptions(true)

        // 第一张扑克牌数据确认
        wheelListener_option1 = OnItemSelectedListener { index ->
            this.mOptions2Items.clear()
            mOptions2Items.addAll(mOptions1Items)
            // 删除第一张扑克选中的牌，保证第二张扑克数据和第一张不一样
            mOptions2Items.removeAt(index)
            wv_option2.adapter = ArrayWheelAdapter(mOptions2Items)
            wv_option2.currentItem = 0
            wheelListener_option2.onItemSelected(0)
            mOptions3Items.clear()
            mOptions3Items.addAll(mOptions1Items)
            // 删除第一张扑克选中的牌，保证第三张扑克数据和第一张不一样
            mOptions3Items.removeAt(index)
            // 删除第二张扑克选中的牌，保证第三张扑克数据和第二张不一样
            mOptions3Items.remove(mOptions2Items[0])
            wv_option3.adapter = ArrayWheelAdapter(mOptions3Items)
            wv_option3.currentItem = 0
        }

        // 第二张扑克牌确认
        wheelListener_option2 = OnItemSelectedListener { index ->
            this.mOptions3Items.clear()
            mOptions3Items.addAll(mOptions1Items)
            // 删除第一张扑克选中的牌，保证第三张扑克数据和第一张不一样
            mOptions3Items.remove(mOptions1Items[wv_option1.currentItem])
            // 删除第二张扑克选中的牌，保证第三张扑克数据和第二张不一样
            mOptions3Items.removeAt(index)
            wv_option3.adapter = ArrayWheelAdapter(mOptions3Items)
            wv_option3.currentItem = 0

//            wheelListener_option2.onItemSelected(0)

            //3级联动数据实时回调
            optionsSelectChangeListener?.onOptionsSelectChanged(wv_option1.currentItem, index, 0)
        }
        if(linkage){
            wv_option1.setOnItemSelectedListener(wheelListener_option1)
            wv_option2.setOnItemSelectedListener(wheelListener_option2)
            wv_option3.setOnItemSelectedListener { index ->
                optionsSelectChangeListener?.onOptionsSelectChanged(
                    wv_option1.currentItem,
                    wv_option2.currentItem,
                    index
                )
            }
        }


    }

    fun setTextContentSize(textSize: Int) {
        wv_option1.setTextSize(textSize.toFloat())
        wv_option2.setTextSize(textSize.toFloat())
        wv_option3.setTextSize(textSize.toFloat())
    }

    private fun setTextColorOut() {
        wv_option1.setTextColorOut(textColorOut)
        wv_option2.setTextColorOut(textColorOut)
        wv_option3.setTextColorOut(textColorOut)
    }

    private fun setTextColorCenter() {
        wv_option1.setTextColorCenter(textColorCenter)
        wv_option2.setTextColorCenter(textColorCenter)
        wv_option3.setTextColorCenter(textColorCenter)
    }

    private fun setDividerColor() {
        wv_option1.setDividerColor(dividerColor)
        wv_option2.setDividerColor(dividerColor)
        wv_option3.setDividerColor(dividerColor)
    }

    private fun setDividerType() {
        wv_option1.setDividerType(dividerType)
        wv_option2.setDividerType(dividerType)
        wv_option3.setDividerType(dividerType)
    }

    private fun setLineSpacingMultiplier() {
        wv_option1.setLineSpacingMultiplier(lineSpacingMultiplier)
        wv_option2.setLineSpacingMultiplier(lineSpacingMultiplier)
        wv_option3.setLineSpacingMultiplier(lineSpacingMultiplier)

    }

    /**
     * 设置选项的单位
     *
     * @param label1 单位
     * @param label2 单位
     * @param label3 单位
     */
    fun setLabels(label1: String?, label2: String?, label3: String?) {
        if (label1 != null) {
            wv_option1.setLabel(label1)
        }
        if (label2 != null) {
            wv_option2.setLabel(label2)
        }
        if (label3 != null) {
            wv_option3.setLabel(label3)
        }
    }

    /**
     * 设置x轴偏移量
     */
    fun setTextXOffset(x_offset_one: Int, x_offset_two: Int, x_offset_three: Int) {
        wv_option1.setTextXOffset(x_offset_one)
        wv_option2.setTextXOffset(x_offset_two)
        wv_option3.setTextXOffset(x_offset_three)
    }

    /**
     * 设置是否循环滚动
     *
     * @param cyclic 是否循环
     */
    fun setCyclic(cyclic: Boolean) {
        wv_option1.setCyclic(cyclic)
        wv_option2.setCyclic(cyclic)
        wv_option3.setCyclic(cyclic)
    }

    /**
     * 设置字体样式
     *
     * @param font 系统提供的几种样式
     */
    fun setTypeface(font: Typeface) {
        wv_option1.setTypeface(font)
        wv_option2.setTypeface(font)
        wv_option3.setTypeface(font)
    }

    /**
     * 设置间距倍数,但是只能在1.2-4.0f之间
     *
     * @param lineSpacingMultiplier
     */
    fun setLineSpacingMultiplier(lineSpacingMultiplier: Float) {
        this.lineSpacingMultiplier = lineSpacingMultiplier
        setLineSpacingMultiplier()
    }

    /**
     * 设置分割线的颜色
     *
     * @param dividerColor
     */
    fun setDividerColor(dividerColor: Int) {
        this.dividerColor = dividerColor
        setDividerColor()
    }

    /**
     * 设置分割线的类型
     *
     * @param dividerType
     */
    fun setDividerType(dividerType: WheelView.DividerType) {
        this.dividerType = dividerType
        setDividerType()
    }

    /**
     * 设置分割线以外文字的颜色
     *
     * @param textColorOut
     */
    fun setTextColorOut(textColorOut: Int) {
        this.textColorOut = textColorOut
        setTextColorOut()
    }

    /**
     * 设置分割线之间的文字的颜色
     *
     * @param textColorCenter
     */
    fun setTextColorCenter(textColorCenter: Int) {
        this.textColorCenter = textColorCenter
        setTextColorCenter()
    }

    /**
     * 返回当前选中的结果对应的位置数组 因为支持三级联动效果，分三个级别索引，0，1，2。
     * 在快速滑动未停止时，点击确定按钮，会进行判断，如果匹配数据越界，则设为0，防止index出错导致崩溃。
     *
     * @return 索引数组
     */
    fun getCurrentItems(): IntArray {
        val currentItems = IntArray(3)
        currentItems[0] = wv_option1.currentItem

        currentItems[1] = wv_option2.currentItem

        currentItems[2] = wv_option3.currentItem

        return currentItems
    }

    fun setCurrentItems(option1: Int, option2: Int, option3: Int) {
        if (linkage) {
            itemSelected(option1, option2, option3)
        } else {
            wv_option1.currentItem = option1
            wv_option2.currentItem = option2
            wv_option3.currentItem = option3
        }
    }

    /**
     * 当选中以后，需要对第二张扑克和第三张扑克数据进行处理
     */
    private fun itemSelected(opt1Select: Int, opt2Select: Int, opt3Select: Int) {
        wv_option1.currentItem = opt1Select
        mOptions2Items.clear()
        mOptions2Items.addAll(mOptions1Items)
        // 第一张扑克选择以后，第二张扑克数据需要进行修改，以保证第二张扑克数据和第一张不一样
        mOptions2Items.removeAt(opt1Select)
        wv_option2.adapter = ArrayWheelAdapter(mOptions2Items)
        wv_option2.currentItem = opt2Select
        mOptions3Items.clear()
        mOptions3Items.addAll(mOptions1Items)
        // 第三张扑克数据需要进行修改，以保证第三张扑克数据和第一张不一样
        mOptions3Items.remove(mOptions1Items[opt1Select])
        // 第三张扑克数据需要进行修改，以保证第三张扑克数据和第一张不一样
        mOptions3Items.remove(mOptions2Items[opt2Select])
        wv_option3.adapter = ArrayWheelAdapter(mOptions3Items)
        wv_option3.currentItem = opt3Select
    }


}