package com.unaiIgorRuben.diacero.util

import androidx.room.TypeConverter

class LongListConverter {
    @TypeConverter
    fun fromLongList(value: List<Long>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toLongList(value: String?): List<Long> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").mapNotNull { it.toLongOrNull() }
    }
}
