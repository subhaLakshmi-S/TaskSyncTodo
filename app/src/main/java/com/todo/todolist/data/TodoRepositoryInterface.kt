package com.todo.todolist.data

import androidx.lifecycle.LiveData


interface TodoRepositoryInterface {
     suspend fun insert(todo: Todo)
     suspend fun update(todo: Todo)
     suspend fun delete(todo: Todo)
     fun getAllTodo(): LiveData<List<Todo>>
     fun getFavorites(): LiveData<List<Todo>>
     fun getCompleted(): LiveData<List<Todo>>
     fun getByCategory(category: String): LiveData<List<Todo>>
     fun getSortedByDate(): LiveData<List<Todo>>
     fun getSortedByPriority(): LiveData<List<Todo>>
}