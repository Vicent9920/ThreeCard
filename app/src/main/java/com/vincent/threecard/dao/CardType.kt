package com.vincent.threecard.dao

import com.vincent.threecard.services.Total
import org.litepal.crud.LitePalSupport

/**
 * <p>文件描述：牌型实体<p>
 * <p>@author 烤鱼<p>
 * <p>@date 2019/9/16 0016 <p>
 * <p>@update 2019/9/16 0016<p>
 * <p>版本号：1.0<p>
 *
 */

data class CardType(
    var a: Int, var b: Int, var c: Int, var type: Int? = 1,
    var max: Int? = 2, var number: Int = 0
) : LitePalSupport() {
    @Transient
    val id = 0


    // 将 1 换算为 A ，为目前的最大值
    init {
        a = if (a < 5) a + Total else a
        b = if (b < 5) b + Total else b
        c = if (c < 5) c + Total else c
    }

    /**
     * 获取牌的类型
     * 6 炸弹
     * 5 顺金
     * 4 金花
     * 3 顺子
     * 2 对子
     * 1 普通
     */
    fun getType(): Int {

        val a1 = getCardValue(a)
        val b1 = getCardValue(b)
        val c1 = getCardValue(c)
        return when {
            a1 == b1 && b1 == c1 -> 6 // 炸弹 三张牌大小一样
            (a % 4 == b % 4 && b % 4 == c % 4) && isConstant() -> 5 // 金花+顺子 花色必须一样且大小必须有连续性
            a % 4 == b % 4 && b % 4 == c % 4 -> 4 // 金花 牌的大小必须一样
            isConstant() -> 3 // 连子 牌的大小具有连续性 如2,3,4
            a1 == b1 || a1 == c1 || b1 == c1 -> 2 // 对子 三张牌中必须有两张牌大小一样
            else -> 1
        } // 不具有上述特征以外的普通牌型
    }

    /**
     * 计算牌的值，如将 5 转化为花型为 2 的牌值
     */
    private fun getCardValue(x: Int): Int {
        var y = x
        y = if (y % 4 != 0) {
            y / 4 + 1
        } else {
            y / 4
        }
        return y
    }

    // 判断是否是顺子（如2,3,4）
    private fun isConstant(): Boolean {
        var result = false
        val a1 = getCardValue(a)
        val b1 = getCardValue(b)
        val c1 = getCardValue(c)
        when {
            getTotalCard() == 3 * a1 && Math.abs(b1 - c1) == 2 -> result = true
            getTotalCard() == 3 * b1 && Math.abs(a1 - c1) == 2 -> result = true
            getTotalCard() == 3 * c1 && Math.abs(a1 - b1) == 2 -> result = true
        }
        return result
    }

    /**
     * 获取牌型最大值
     */
    fun getMax(): Int {
        return getNumber(1)
    }

    /**
     * 获取牌型的中数（中间值）
     */
    fun getMedia(): Int {
        return getNumber(0)
    }

    /**
     * 获取牌型最小值
     * 当比较最小值的时候，也可以直接比较总值了，
     * 当然比较小值理论上效率大于总值，只是在目前的计算范围，这点效率可以忽略不计
     */
    fun getMin(): Int {
        return getNumber(-1)
    }

    /**
     * 获取不同类型的值
     * 1 最大值
     * 0 中值
     * -1 最小值
     */
    private fun getNumber(type: Int): Int {
        val array = intArrayOf(a, b, c)
        array.sort()
        when (type) {
            1 -> {
                return getCardValue(array[2])
            }
            0 -> {

                return getCardValue(array[1])
            }
            -1 -> {
                return array[0]
            }
            else -> {
                return 0
            }

        }
    }

    /**
     * 当牌型中有两个值一样（对子）时，取对数的值
     */
    fun getPairNumber(): Int {
        return when {
            a / 4 == b / 4 -> a
            a / 4 == c / 4 -> a
            else -> c
        }

    }

    /**
     * 当牌型中有两个值一样（对子）时，取非对数的值
     */
    fun getNotPairNumberWithPair(): Int {
        return when {
            a / 4 == b / 4 -> c
            a / 4 == c / 4 -> b
            else -> a
        }
    }

    /**
     * 当普通牌型的时候，比大小的时候计算牌型的总值
     * 此处已经考虑到花色，因此没有计算牌面值是红桃A还是黑桃10,直接计算的算法值
     */
    fun getTotal(): Int {
        return a + b + c
    }

    /**
     * 计算牌型的总牌值
     * 与<link getTotal()>的区别在于上面计算考虑花型，此处不考虑花型
     */
    fun getTotalCard(): Int {
        val a1 = getCardValue(a)
        val b1 = getCardValue(b)
        val c1 = getCardValue(c)
        return a1 + b1 + c1
    }

    override fun toString(): String {
        return "a--${getCardName(a)}  b--${getCardName(b)}  c--${getCardName(c)}"
    }

    private fun getCardName(value: Int): String {
        val number = when (val cardValue = getCardValue(value)) {
            14 -> "A"
            13 -> "K"
            12 -> "Q"
            11 -> "J"
            else -> cardValue.toString()
        }

        return when (value % 4) {
            3 -> "黑桃$number"
            2 -> "梅花$number"
            1 -> "方块$number"
            else -> "红桃$number"
        }
    }
}