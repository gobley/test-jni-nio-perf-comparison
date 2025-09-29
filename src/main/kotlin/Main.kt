package dev.gobley.test.jninioperfcomparison

import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

fun main() {
    // Warming up
    testWithArraySize(10)
    for (arraySize in 10_000..100_000 step 10_000) {
        testWithArraySize(arraySize)
    }
}

fun testWithArraySize(arraySize: Int, repeatTimes: Int = 30) {
    println(":::::::::: Test with arraySize = $arraySize ::::::::::")
    val testData = Array(arraySize) { TheStruct.random() }
    val groundTruth = testData.sumOf { it.second.pow(it.first) }
    testUsing("jni", repeatTimes, groundTruth) {
        RustLibrary.testUsingJni(testData)
    }
    testUsing("nio", repeatTimes, groundTruth) {
        RustLibrary.testUsingNio(testData)
    }
    println()
}

@OptIn(ExperimentalTime::class)
fun <R> testUsing(testFnName: String, repeatTimes: Int, groundTruth: R, testFn: () -> R) {
    val elapsedTimeList = Array(repeatTimes) { Duration.ZERO }
    repeat(repeatTimes) {
        val startTime = Clock.System.now()
        val result = testFn()
        val endTime = Clock.System.now()
        assert(groundTruth == result)
        elapsedTimeList[it] = endTime - startTime
    }
    val mean = elapsedTimeList.map { it.inWholeNanoseconds / 1_000_000.0 }.average()
    val variance = elapsedTimeList
        .map {
            val seconds = it.inWholeNanoseconds / 1_000_000.0
            val difference = mean - seconds
            difference * difference
        }
        .average()
    val stddev = sqrt(variance)
    println("$testFnName: mean = $mean seconds, stddev = $stddev seconds")
}