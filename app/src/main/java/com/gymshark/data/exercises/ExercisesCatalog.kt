package com.gymshark.data.exercises

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.gymshark.R

data class ExercisesJson(
    @SerializedName("version") val version: Int,
    @SerializedName("list") val list: List<ExerciseItem>
)

data class ExerciseItem(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("baseCategory") val baseCategory: String,
    @SerializedName("subCategory") val subCategory: String? = null,
    @SerializedName("difficulty") val difficulty: Int? = null
)

class ExercisesCatalog(
    private val context: Context,
    private val gson: Gson = Gson()
) {
    fun loadCategories(): List<String> = try {
        val json = context.resources.openRawResource(R.raw.exercises)
            .bufferedReader().use { it.readText() }
        val parsed = gson.fromJson(json, ExercisesJson::class.java) ?: return emptyList()
        parsed.list.map { it.baseCategory.trim() }.distinct()
    } catch (e: Exception) {
        emptyList()
    }
}

