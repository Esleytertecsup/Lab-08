package com.example.lab08.data

import androidx.room.*



import kotlinx.coroutines.flow.Flow

@Dao



interface TaskDao {

    @Query("SELECT * FROM tasks")

    fun getAllTasks(): Flow<List<Task>>



    @Query("SELECT * FROM tasks WHERE is_completed = :isCompleted")

    fun getTasksByCompletionStatus(isCompleted: Boolean): Flow<List<Task>>



    @Query("SELECT * FROM tasks WHERE description LIKE '%' || :query || '%'")

    fun searchTasks(query: String): Flow<List<Task>>



    @Insert

    suspend fun insertTask(task: Task)



    @Update

    suspend fun updateTask(task: Task)



    @Delete

    suspend fun deleteTask(task: Task)



    @Query("DELETE FROM tasks")

    suspend fun deleteAllTasks()

}