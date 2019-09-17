# 观炸金花有感
首先祝各位中秋节快乐！总有人说我们四川人耍就是打“MAJIANG”，四川人出去耍就是换个地方打麻将，其实我们四川人除了麻将还有斗地主和炸金花，而前两天在观看朋友炸金花以后，我就发现一个特别有趣的事情，`52`张牌，在炸金花过程中如果通过算法来分析一下，应该有一些不同的收获。
接下来我就带大家一起来看看，能不能把自己手上的牌进行科学化的分析，看看在组合中自己的牌到底算不算大？此处默认炸金花的游戏规则大家都知道（不知道的请自行百度了解 [炸金花介绍](https://baike.baidu.com/item/%E7%82%B8%E9%87%91%E8%8A%B1/8806924?fr=aladdin)）

## 牌型
众所周知，炸金花需要`52`张牌,那么`52`张牌可以组合多少个结果呢？没错，通过组合排序很容易得到答案——`52*51*50`，计算器一敲结果就出来了——`132600`。如果说，需要对这个结果进行处理，对数据进行进一步加工，我们可以通过下面的方法将所有数据取出来：

```
 /**
     * 计算牌型组合并按炸金花游戏规则对牌型组合进行排序
     */
    private fun calculateCardType(): MutableList<CardType> {
        val result = mutableListOf<CardType>()
        for (i in 1..total) {
            for (j in 1..total) {
                for (k in 1..total) {
                    if (i != j && i != k && j != k) {
                        result.add(CardType(i, j, k))
                        result.last().level = result.size
                    }
                }
            }
        }
        return result
    }
```
和朋友聊起这个结果的时候，大家对这个结果表示了不认同，因为[红桃A,梅花A,方块A]与[红桃A,方块A,梅花A]在组合中来说是两个结果，但是在牌面上来说，其实这是一个结果，不能被重复计算，因此需要进行优化，那么我们来处理一下：

```
/**
     * 计算牌型组合并按炸金花游戏规则对牌型组合进行排序
     */
    private fun calculateCardType(): MutableList<CardType> {
        val result = mutableListOf<CardType>()
        for (i in 1..total) {
            for (j in 1..total) {
                for (k in 1..total) {
                    if (i != j && i != k && j != k) {
                        if(j>i){
                            if(k>j){
                                result.add(CardType(i, j, k))
                                if(i == 5 && j == 9){
                                    println(result.last().toString())
                                }
                            }
                        }

                    }
                }
            }
        }
        return result
    }
```
优化后的数据只有`22100`,较优化之前减少了`80%`，这个算法具体术语我暂时还不知道，知道的大佬可以给我说一下。那么，这个算法是怎么得到的啊？由于数据量太大了，不能用我小学五年级数学那一招推演了！就在我快磕完一包瓜子的时候我想到了两个方法：
* 第一个方法比较笨
> 每次添加以前，遍历数据里面是否包含待添加的数据。当添加的数据越来越多的时候，前面遍历的时间也会越来越长，效率太低被`Pass`了
* 第二个方法就较上一个方法简单
> 假设我们需要排列的数据只有`1234`，那么结果有`4*3*2`——24个不同的组合，但是真正不重复的组合只有下面的四组：
> 
> ```
> 1  2  3
> 1  2  4
> 1  3  4
> 2  3  4
> ```
> 通过观察发现，第二位数始终大于第一位数，然后第三位数始终大于第二位数，根据这个结果，我们写一个算法来检验一下，比如排列的数据为`12345`，我们看看最终的结果是多少啊？
>
> ```
> private val total = 5
> 
> fun main(arrsy:Array<String>) {
>      for (i in 1..total) {
>            for (j in 1..total) {
>                
>                for (k in 1..total) {
>                    if (i != j && i != k && j != k) {
>                        if(j>i){
>                            if(k>j){
>                                println("$i  $j  $k")
>                            }
>                        }
>                        
>                    }
>                }
>            }
>        }
>    
> }
>
> 计算结果：
> 1  2  3
> 1  2  4
> 1  2  5
> 1  3  4
> 1  3  5
> 1  4  5
> 2  3  4
> 2  3  5
> 2  4  5
> 3  4  5
> ```
> 经验证无误，于是有了上面的算法以及`22100`这个结果

## 计算牌型的大小
既然我们知道了牌型里面一共有`22100`个牌型，那么能不能对所有牌型按照从小到大来进行一个排序呢？比如你炸金花的时候，打算弃牌的时候，就可以科学的分析一下当前这手牌满分100分的话，应该可以打多少分，给自己一个科学的依据，那么我们开始吧！

### 规则说明
在计算牌型大小之前，我们还得对`52`张牌进行一个代码上面的转化，`1~52`代表一副去掉大小王的牌,其中`1~4`代表`A`,`5-8`代表`2`,依次类推，`49~52`代表`K`,然后为了对牌型的大小进行比较，因此我们对每个花色也进行了量化，我们根据“红黑梅芳”的规则来进行统计，比如`49~52`分别代表方块`K`(`49`)、梅花`K`(`50`)、黑桃`K`(`51`)、红桃`K`(`52`)。最后一点,由于`A`是单张牌里面最大的，因此在牌型`CardType`这个对象里面，把`1~4`给修改为了`53~56`,方便最后进行计算。

### 牌型封装
由于炸金花都是每人`3`张牌，因此我们把`3`张牌称为一手牌，然后把一手牌的类型等属性进行了封装，先看看一手牌对象`CardType`的代码：

```
/**
 * 牌型
 */
data class CardType(var a: Int, var b: Int, var c: Int, var level: Int? = 0) {

    // 将 1 换算为 A ，为目前的最大值
    init {
        a = if (a < 5) a + 52 else a
        b = if (b < 5) b + 52 else b
        c = if (c < 5) c + 52 else c
    }

    /**
     * 获取牌的类型
     * 6 豹子
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
            a1 == b1 && b1 == c1 -> 6 // 豹子 三张牌大小一样
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
            getTotalCard() == 3 * a1 && abs(b1 - c1) == 2 -> result = true
            getTotalCard() == 3 * b1 && abs(a1 - c1) == 2 -> result = true
            getTotalCard() == 3 * c1 && abs(a1 - b1) == 2 -> result = true
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
    private fun getTotalCard(): Int {
        val a1 = getCardValue(a)
        val b1 = getCardValue(b)
        val c1 = getCardValue(c)
        return a1 + b1 + c1
    }

    override fun toString(): String {
        return "a--$a  b--$b  c--$c"
    }
}
```
### 牌型大小统计并排序
准备工作已经做好了，接下来就需要进行排序。交换排序有多种算法，常见的有快速排序和冒泡排序，此处我直接使用了冒泡排序。首先将集合转化为数组，然后在排序过程中，我们先比较两手牌的类型（`getType()`）是否一致，比如不一致的话，直接比较类型的高低；一致的话需要单独根据各个类型进行比较，每种类型的比较也不一样。当排序结束以后，我们将数组转为集合。此处这部分对集合转数组又转集合是因为在冒泡排序中，使用数组对于交换数据更为方便。

```
/**
     * 对牌型按照炸金花游戏规则进行排序(牌型大小此处是按照红桃 > 黑桃 > 梅花 > 方块）
     * 普通牌型，如 235
     * 对子牌型，如 223
     * 顺子牌型，如 234
     * 金花牌型，如 红桃2、红桃5、红桃7（换算成当前的具体值，应该是[(2-1)*4+4,(5-1)*4+4,(7-1)*4+4] 即 [8,20,28]）
     * 同花顺牌型，如 黑桃A、黑桃K、黑桃Q （换算成当前的具体值，应该是[52+3,(13-1)*4+3,(12-1)*4+3] 即 [55,51,47]）特别注意 1/2/3/4 在牌型的构造方法已经转换成53/54/55/56
     * 豹子牌型，如 梅花K、方块K、黑桃K（换算成当前的具体值，应该是[(13-1)*4+2,(13-1)*4+1,(13-1)*4+3] 即 [50,49,51]）
     *
     */
    private fun sortCards(mutableList: MutableList<CardType>): MutableList<CardType> {
        // 普通 ==》 对子 ==》 顺子 ==》 金花 ==》 同花顺 ==》 豹子
        var bChange: Boolean // 交换标志
        // 此处使用冒泡排序，为了排序方便将集合转化为数组
        val result: Array<CardType> = mutableList.toTypedArray()
        // 要遍历的次数
        for (i in 0 until result.size - 1) {
            bChange = false;
            // 从后向前依次的比较相邻两个数的大小，遍历一次后，把数组中第i小的数放在第i个位置上
            for (j in result.size - 1 downTo i + 1) {
                // 比较相邻的元素，如果前面的数大于后面的数，则交换
                val front = result[j - 1]
                val after = result[j]

                if (front.getType() == after.getType()) {
                    // 豹子
                    if (front.getType() == 6) {
                        // 直接比一张牌大小即可
                        if (front.a > after.a) {
                            // 交换排序
                            result[j - 1] = after
                            result[j] = front
                            bChange = true
                        }
                    }// 同花顺
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
```

排序后的结果：

```
最小：a--方块2  b--方块3  c--梅花5
较小：a--方块2  b--梅花8  c--黑桃J
中等：a--方块2  b--黑桃10  c--红桃K
较大：a--方块2  b--黑桃2  c--梅花Q
最大：a--梅花A  b--黑桃A  c--红桃A
```
## 总结
**根据目前统计和排序结果来看，一副牌里面，有`22100`个组合结果，如果发的是普通牌型且最大牌大于`J`的时候，你应该领先`25%`的人群了，也就是说你的得胜率为`25%`；如果发的是普通牌型且你手里最大牌属于`A`的话，那么你的得胜率为`50%`；如果发的牌型是对子的话，哪怕是最小的对`2`,你的得胜率也高达`75%`了！**
### 特别强调
**以上结论谨作为推算结果，反对赌博，特别反对大额赌博！**

## 延伸

然后大家具体分析一下，不考虑花色的话，一个人拿到三个`A`的概率是`4/52*3/51*2/50`即`1/5525`，那么如果甲乙两个人玩的话，甲拿到三个`A`的概率会不会受到游戏人数的影响？