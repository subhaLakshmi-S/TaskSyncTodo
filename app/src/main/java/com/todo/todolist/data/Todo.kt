package com.todo.todolist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int =0,
    val userId: String,
    var title: String,
    var content: String,
    var startDate: String,
    var endDate: String,
    var isFavorite: Boolean = false,
    var isCompleted: Boolean = false,
    val priority: String,
    val category: String
)
