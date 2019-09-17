package com.vincent.threecard.picker

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.bigkoo.pickerview.R
import com.bigkoo.pickerview.configure.PickerOptions
import com.bigkoo.pickerview.view.BasePickerView

/**
 * <p>文件描述：<p>
 * <p>@author 烤鱼<p>
 * <p>@date 2019/9/16 0016 <p>
 * <p>@update 2019/9/16 0016<p>
 * <p>版本号：1<p>
 *
 */
class OptionsPickerView<T>(pickerOptions: PickerOptions) : BasePickerView(pickerOptions.context), View.OnClickListener {


    private val TAG_SUBMIT = "submit"
    private val TAG_CANCEL = "cancel"

    private lateinit var wheelOptions: CustomWheelOptions<T>

    init {
        mPickerOptions = pickerOptions
        initView(pickerOptions.context)
    }

    private fun initView(context: Context) {
        setDialogOutSideCancelable()
        initViews()
        initAnim()
        initEvents()
        if (mPickerOptions.customListener == null) {
            LayoutInflater.from(context).inflate(mPickerOptions.layoutRes, contentContainer)

            //顶部标题
            val tvTitle = findViewById(R.id.tvTitle) as TextView
            val rv_top_bar = findViewById(R.id.rv_topbar) as RelativeLayout

            //确定和取消按钮
            val btnSubmit = findViewById(R.id.btnSubmit) as Button
            val btnCancel = findViewById(R.id.btnCancel) as Button

            btnSubmit.tag = TAG_SUBMIT
            btnCancel.tag = TAG_CANCEL
            btnSubmit.setOnClickListener(this)
            btnCancel.setOnClickListener(this)

            //设置文字
            btnSubmit.text = if (TextUtils.isEmpty(mPickerOptions.textContentConfirm)) context.resources.getString(
                R.string.pickerview_submit
            ) else mPickerOptions.textContentConfirm
            btnCancel.text =
                if (TextUtils.isEmpty(mPickerOptions.textContentCancel)) context.resources.getString(R.string.pickerview_cancel) else mPickerOptions.textContentCancel
            tvTitle.text =
                if (TextUtils.isEmpty(mPickerOptions.textContentTitle)) "" else mPickerOptions.textContentTitle//默认为空

            //设置color
            btnSubmit.setTextColor(mPickerOptions.textColorConfirm)
            btnCancel.setTextColor(mPickerOptions.textColorCancel)
            tvTitle.setTextColor(mPickerOptions.textColorTitle)
            rv_top_bar.setBackgroundColor(mPickerOptions.bgColorTitle)

            //设置文字大小
            btnSubmit.textSize = mPickerOptions.textSizeSubmitCancel.toFloat()
            btnCancel.textSize = mPickerOptions.textSizeSubmitCancel.toFloat()
            tvTitle.textSize = mPickerOptions.textSizeTitle.toFloat()
        } else {
            mPickerOptions.customListener.customLayout(
                LayoutInflater.from(context).inflate(
                    mPickerOptions.layoutRes,
                    contentContainer
                )
            )
        }

        // ----滚轮布局
        val optionsPicker = findViewById(R.id.optionspicker) as LinearLayout
        optionsPicker.setBackgroundColor(mPickerOptions.bgColorWheel)

        wheelOptions = CustomWheelOptions(optionsPicker, mPickerOptions.isRestoreItem)
        wheelOptions.optionsSelectChangeListener = mPickerOptions.optionsSelectChangeListener

        wheelOptions.setTextContentSize(mPickerOptions.textSizeContent)
        wheelOptions.setLabels(mPickerOptions.label1, mPickerOptions.label2, mPickerOptions.label3)
        wheelOptions.setTextXOffset(
            mPickerOptions.x_offset_one,
            mPickerOptions.x_offset_two,
            mPickerOptions.x_offset_three
        )
        wheelOptions.setCyclic(true)
        wheelOptions.setTypeface(mPickerOptions.font)

        setOutSideCancelable(mPickerOptions.cancelable)

        wheelOptions.setDividerColor(mPickerOptions.dividerColor)
        wheelOptions.setDividerType(mPickerOptions.dividerType)
        wheelOptions.setLineSpacingMultiplier(mPickerOptions.lineSpacingMultiplier)
        wheelOptions.setTextColorOut(mPickerOptions.textColorOut)
        wheelOptions.setTextColorCenter(mPickerOptions.textColorCenter)
    }

    /**
     * 动态设置标题
     *
     * @param text 标题文本内容
     */
    fun setTitleText(text: String) {
        val tvTitle = findViewById(R.id.tvTitle) as TextView
        if (tvTitle != null) {
            tvTitle.text = text
        }
    }

    /**
     * 设置默认选中项
     *
     * @param option1
     */
    fun setSelectOptions(option1: Int) {
        mPickerOptions.option1 = option1
        reSetCurrentItems()
    }


    fun setSelectOptions(option1: Int, option2: Int) {
        mPickerOptions.option1 = option1
        mPickerOptions.option2 = option2
        reSetCurrentItems()
    }

    fun setSelectOptions(option1: Int, option2: Int, option3: Int) {
        mPickerOptions.option1 = option1
        mPickerOptions.option2 = option2
        mPickerOptions.option3 = option3
        reSetCurrentItems()
    }

    private fun reSetCurrentItems() {
        wheelOptions.setCurrentItems(mPickerOptions.option1, mPickerOptions.option2, mPickerOptions.option3)
    }

    fun setPicker(optionsItems: List<T>) {
        this.setPicker(optionsItems, null, null)
    }

    fun setPicker(options1Items: List<T>, options2Items: List<List<T>>) {
        this.setPicker(options1Items, options2Items, null)
    }

    fun setPicker(
        options1Items: List<T>,
        options2Items: List<List<T>>?,
        options3Items: List<List<List<T>>>?
    ) {

        wheelOptions.setPicker(options1Items.toMutableList())
        reSetCurrentItems()
    }


    //不联动情况下调用
    fun setNPicker(
        options1Items: List<T>,
        options2Items: List<T>,
        options3Items: List<T>
    ) {

        reSetCurrentItems()
    }

    override fun onClick(v: View) {
        val tag = v.tag as String
        if (tag == TAG_SUBMIT) {
            returnData()
        } else if (tag == TAG_CANCEL) {
            if (mPickerOptions.cancelListener != null) {
                mPickerOptions.cancelListener.onClick(v)
            }
        }
        dismiss()
    }

    //抽离接口回调的方法
    fun returnData() {
        if (mPickerOptions.optionsSelectListener != null) {
            val optionsCurrentItems = wheelOptions.getCurrentItems()
            mPickerOptions.optionsSelectListener.onOptionsSelect(
                optionsCurrentItems[0],
                optionsCurrentItems[1],
                optionsCurrentItems[2],
                clickView
            )
        }
    }


    override fun isDialog(): Boolean {
        return mPickerOptions.isDialog
    }
}