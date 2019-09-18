package com.vincent.threecard

import com.vincent.threecard.services.Total
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        for(i in 1..200){
            println((1..Total).random())
        }

    }
}
