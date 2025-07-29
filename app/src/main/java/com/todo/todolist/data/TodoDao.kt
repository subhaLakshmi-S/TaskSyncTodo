package com.todo.todolist.data

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface TodoDao {
    @Insert suspend fun insert(todo: Todo)
    @Update suspend fun update(todo: Todo)
    @Delete suspend fun delete(todo: Todo)

    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY id DESC")
    fun getAllTodo(userId: String): LiveData<List<Todo>>

    @Query("SELECT * FROM notes WHERE userId = :userId AND isFavorite = 1")
    fun getFavorites(userId: String): LiveData<List<Todo>>

    @Query("SELECT * FROM notes WHERE userId = :userId AND isCompleted = 1")
    fun getCompleted(userId: String): LiveData<List<Todo>>

    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY startDate ASC")
    fun getSortedByDate(userId: String): LiveData<List<Todo>>

    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY priority ASC")
    fun getSortedByPriority(userId: String): LiveData<List<Todo>>

    @Query("SELECT * FROM notes WHERE userId = :userId AND category = :category")
    fun getByCategory(userId: String,category: String): LiveData<List<Todo>>

    @Query("SELECT * FROM notes WHERE userId = :userId AND isCompleted = 1 AND endDate <= :yesterday")
    fun deleteOldCompleted(userId: String,yesterday: String): LiveData<List<Todo>>
}