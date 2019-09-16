package com.vincent.threecard.services

import android.app.IntentService
import android.content.Intent
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.vincent.threecard.dao.Card
import com.vincent.threecard.dao.CardType

/**
 * <p>文件描述：<p>
 * <p>@author 烤鱼<p>
 * <p>@date 2019/9/16 0016 <p>
 * <p>@update 2019/9/16 0016<p>
 * <p>版本号：1<p>
 *
 */
const val Total = 52
const val init = "init"
class CardServices:IntentService("CardServices") {
    override fun onHandleIntent(intent: Intent?) {
        val cards = calculateCardType()
        for ((index,card) in cards.withIndex()){
            card.level = index
            card.save()
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit {
            putBoolean(init,true)
        }
    }

    /**
     * 计算牌型组合并按炸金花游戏规则对牌型组合进行排序
     */
    private fun calculateCardType(): MutableList<CardType> {
        val result = mutableListOf<CardType>()
        for (i in 1..Total) {
            for (j in 1..Total) {
                for (k in 1..Total) {
                    if (i != j && i != k && j != k) {

                        if (j > i) {
                            if (k > j) {
                                result.add(CardType(i, j, k))
                            }
                        }

                    }
                }
            }
        }
        return sortCards(result)
    }



    /**
     * 对牌型按照炸金花游戏规则进行排序(牌型大小此处是按照红桃 > 黑桃 > 梅花 > 方块）
     * 普通牌型，如 235
     * 对子牌型，如 223
     * 顺子牌型，如 234
     * 金花牌型，如 红桃2、红桃5、红桃7（换算成当前的具体值，应该是[(2-1)*4+4,(5-1)*4+4,(7-1)*4+4] 即 [8,20,28]）
     * 顺子+金花牌型，如 黑桃A、黑桃K、黑桃Q （换算成当前的具体值，应该是[52+3,(13-1)*4+3,(12-1)*4+3] 即 [55,51,47]）特别注意 1/2/3/4 在牌型的构造方法已经转换成53/54/55/56
     * 炸弹牌型，如 梅花K、方块K、黑桃K（换算成当前的具体值，应该是[(13-1)*4+2,(13-1)*4+1,(13-1)*4+3] 即 [50,49,51]）
     *
     */
    private fun sortCards(mutableList: MutableList<CardType>): MutableList<CardType> {
        // 普通 ==》 对子 ==》 顺子 ==》 金花 ==》 顺子+金花 ==》 炸弹
        var bChange: Boolean // 交换标志
        // 此处使用冒泡排序，为了排序方便将集合转化为数组
        val result: Array<CardType> = mutableList.toTypedArray()
        // 要遍历的次数
        for (i in 0 until result.size - 1) {
            bChange = false
            // 从后向前依次的比较相邻两个数的大小，遍历一次后，把数组中第i小的数放在第i个位置上
            for (j in result.size - 1 downTo i + 1) {
                // 比较相邻的元素，如果前面的数大于后面的数，则交换
                val front = result[j - 1]
                val after = result[j]

                if (front.getType() == after.getType()) {
                    // 炸弹
                    if (front.getType() == 6) {
                        // 直接比一张牌大小即可
                        if (front.a > after.a) {
                            // 交换排序
                            result[j - 1] = after
                            result[j] = front
                            bChange = true
                        }
                    }// 顺子+金花
                    else if (front.getType() == 5 || front.getType() == 3) {
                        // 由于牌型具有连续性，因此直接计算牌型的大小总值
                        if (front.getTotal() > after.getTotal()) {
                            // 交换排序
                            result[j - 1] = after
                            result[j] = front
                            bChange = true
                        }
                    }// 顺子
                    else if (front.getType() == 4) {
                        // 比较最大值
                        if (front.getMax() > after.getMax()) {
                            result[j - 1] = after
                            result[j] = front
                            bChange = true
                        }// 最大值相等，需要比较牌型的中数
                        else if (front.getMax() == after.getMax()) {
                            // 比较中数
                            if (front.getMedia() > after.getMedia()) {
                                result[j - 1] = after
                                result[j] = front
                                bChange = true
                            } // 中数相等，需要比较第三张牌大小
                            else if (front.getMedia() == after.getMedia()) {
                                // 比较最小数，注意，一副牌不可能有完全相同的三张牌，因此没有第三张牌相等的情况
                                if (front.getMin() > after.getMin()) {
                                    result[j - 1] = after
                                    result[j] = front
                                    bChange = true
                                }
                            }
                        }
                    } // 对子
                    else if (front.getType() == 2) {
                        // 比较对子中的对子大小
                        if (front.getPairNumber() > after.getPairNumber()) {
                            result[j - 1] = after
                            result[j] = front
                            bChange = true
                        } // 当对子一样的时候，需要比较另一张非对子的牌大小
                        else if (front.getPairNumber() == after.getPairNumber()) {
                            // 比较另一张非对子的牌大小
                            if (front.getNotPairNumberWithPair() > after.getNotPairNumberWithPair()) {
                                result[j - 1] = after
                                result[j] = front
                                bChange = true
                            } // 当第三张牌大小相同时，需要比较花色
                            else if (front.getNotPairNumberWithPair() == after.getNotPairNumberWithPair()) {
                                // 比较第三张牌的花色
                                if (front.getTotal() > after.getTotal()) {
                                    result[j - 1] = after
                                    result[j] = front
                                    bChange = true
                                }
                            }
                        }
                    }// 普通牌型
                    else {
                        // 直接进行大小比较
                        // 比较最大值
                        if (front.getMax() > after.getMax()) {
                            result[j - 1] = after
                            result[j] = front
                            bChange = true
                        }// 最大值相等，需要比较牌型的中数
                        else if (front.getMax() == after.getMax()) {
                            // 比较中数
                            if (front.getMedia() > after.getMedia()) {
                                result[j - 1] = after
                                result[j] = front
                                bChange = true
                            } // 中数相等，需要比较第三张牌大小
                            else if (front.getMedia() == after.getMedia()) {
                                // 比较最小数，注意，一副牌不可能有完全相同的三张牌，因此没有第三张牌相等的情况
                                if (front.getMin() > after.getMin()) {
                                    result[j - 1] = after
                                    result[j] = front
                                    bChange = true
                                }
                            }
                        }
                    }
                }// 牌型不一致
                else {
                    // 根据牌型进行比较
                    if (front.getType() > after.getType()) {
                        result[j - 1] = after
                        result[j] = front
                        bChange = true
                    }
                }
            }
            // 如果标志为false，说明本轮遍历没有交换，已经是有序数列，可以结束排序
            if (!bChange) break
        }
        return result.toMutableList()
    }
}