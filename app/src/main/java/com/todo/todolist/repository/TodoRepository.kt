package com.todo.todolist.repository

import androidx.lifecycle.LiveData
import com.todo.todolist.data.Todo
import com.todo.todolist.data.TodoDao
import com.todo.todolist.data.TodoRepositoryInterface
import com.todo.todolist.firebaseAuth.AuthUtil
import javax.inject.Inject

class TodoRepository @Inject constructor(private val dao:TodoDao, private val authUtil: AuthUtil)
     {
     fun getAllTodo(): LiveData<List<Todo>> = dao.getAllTodo(authUtil.getCurrentUserId())
     fun getFavorites(): LiveData<List<Todo>> = dao.getFavorites(authUtil.getCurrentUserId())
     fun getCompleted(): LiveData<List<Todo>> = dao.getCompleted(authUtil.getCurrentUserId())
     fun getSortedByDate(): LiveData<List<Todo>> = dao.getSortedByDate(authUtil.getCurrentUserId())
     fun getSortedByPriority():LiveData<List<Todo>> = dao.getSortedByPriority(authUtil.getCurrentUserId())
     fun getByCategory(category: String):LiveData<List<Todo>> = dao.getByCategory(authUtil.getCurrentUserId(), category)
     suspend fun insert(todo: Todo) = dao.insert(todo)
     suspend fun update(todo: Todo) = dao.update(todo)
     suspend fun delete(todo: Todo) = dao.delete(todo)

    fun deleteOldCompleted(yesterday: String) = dao.deleteOldCompleted(authUtil.getCurrentUserId(),yesterday)
}