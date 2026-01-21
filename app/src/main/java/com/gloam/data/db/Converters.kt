package com.gloam.data.db

import androidx.room.TypeConverter
import com.gloam.data.model.EntryType
import com.gloam.data.model.PromptCategory
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()
    
    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let { LocalDate.ofEpochDay(it) }
    
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? = dateTime?.toString()
    
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }
    
    @TypeConverter
    fun fromEntryType(type: EntryType): String = type.name
    
    @TypeConverter
    fun toEntryType(value: String): EntryType = EntryType.valueOf(value)
    
    @TypeConverter
    fun fromPromptCategory(category: PromptCategory): String = category.name
    
    @TypeConverter
    fun toPromptCategory(value: String): PromptCategory = PromptCategory.valueOf(value)
}
