package com.example.gamevault

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun convertTimestamp(timestamp: Long): String {
    val date = Date(timestamp * 1000)
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(date)
}