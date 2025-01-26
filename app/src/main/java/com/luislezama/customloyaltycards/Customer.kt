package com.luislezama.customloyaltycards


import java.util.Calendar
import java.util.Date
import java.util.Random
import kotlin.math.max
import kotlin.math.min

class Customer {
    companion object {
        fun genUid(): String {
            val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            val length = 6
            val random = Random()
            val sb = StringBuilder(length)

            for (i in 0 until length) {
                val randomIndex = random.nextInt(characters.length)
                val randomChar = characters[randomIndex]
                sb.append(randomChar)
            }

            return sb.toString()
        }

        const val VISITS_MAX = 6
        val VISITS_MILESTONES = mapOf<Int, String>(3 to "15% OFF", 6 to "35% OFF")
        const val DEFAULT_EXPIRATION_MONTHS = 6
    }

    var name = ""
        set(value) {
            field = value.substring(0, min(value.length, 120)).trim()
        }

    var phone = ""
        set(value) {
            field = value.substring(0, min(value.length, 10)).trim()
        }

    var expirationDate: Date? = null
        set(value) {
            var date = Calendar.getInstance().apply {
                time = value
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            field = date.time
        }

    fun isValid(): Boolean {
        return expirationDate?.before(Date()) ?: false
    }

    var uid = genUid()
        set(value) {
            field = value.substring(0, min(value.length, 6)).trim()
        }

    var visits: Int = 0
        set(value) { field = min(max(value, 0), Companion.VISITS_MAX) }
}