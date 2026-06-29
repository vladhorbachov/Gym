package com.gymshark.data.training.seed

import android.content.Context
import com.gymshark.R
import com.gymshark.data.db.entity.ExercisesEntity
import org.json.JSONObject

class ExercisesSeedDataSourceImpl : ExercisesSeedDataSource {

    override fun load(context: Context): List<ExercisesEntity> {
        val inputStream = context.resources.openRawResource(R.raw.exercises)
        val json = inputStream.bufferedReader().use { it.readText() }

        val root = JSONObject(json)
        val listArray = root.getJSONArray("list")

        val result = mutableListOf<ExercisesEntity>()

        for (i in 0 until listArray.length()) {
            val obj = listArray.getJSONObject(i)
            result.add(
                ExercisesEntity(
                    id = 0,
                    name = obj.getString("name"),
                    baseCategory = obj.getString("baseCategory"),
                    subCategory = obj.getString("subCategory"),
                    difficulty = obj.getInt("difficulty")
                )
            )
        }

        return result
    }
}
