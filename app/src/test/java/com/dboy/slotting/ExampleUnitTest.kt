package com.dboy.slotting

import org.json.JSONObject
import org.junit.Test

import org.junit.Assert.*
import java.lang.Math.random
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val linkedList = LinkedList<Int>()
        linkedList.offer(12)
        linkedList.offer(13)
        linkedList.offer(14)
        linkedList.offer(15)

        while (!linkedList.none()) {
            var i = linkedList.poll()
            println("linked next = $i")
        }
    }
}