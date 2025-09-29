package dev.gobley.test.jninioperfcomparison

import kotlin.random.Random

data class TheStruct(@JvmField val first: Int, @JvmField val second: Double) {
    companion object {
        fun random(): TheStruct {
            return TheStruct(
                first = Random.nextInt(),
                second = Random.nextDouble(),
            )
        }
    }
}
