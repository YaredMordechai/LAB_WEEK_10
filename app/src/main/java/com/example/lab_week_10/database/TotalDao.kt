package com.example.lab_week_10.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TotalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(total: Total)

    @Query("SELECT * FROM total WHERE id = :id LIMIT 1")
    fun getById(id: Long): Total?
}
