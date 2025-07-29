package com.todo.todolist.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.todo.todolist.data.Todo
import com.todo.todolist.data.TodoRepositoryInterface


class FakeTodoRepository : TodoRepositoryInterface {

    private val todoList = mutableListOf<Todo>()
    private val todosFlow = MutableLiveData<List<Todo>>(todoList)

    override fun getAllTodo(): LiveData<List<Todo>> = todosFlow

    override suspend fun insert(todo: Todo) {
        todoList.add(todo)
        todosFlow.value = todoList
    }

    override suspend fun update(todo: Todo) {
        val index = todoList.indexOfFirst { it.id == todo.id }
        if (index != -1) {
            todoList[index] = todo
            todosFlow.value = todoList.toList()
        }
    }

    override suspend fun delete(todo: Todo) {
        todoList.removeAll { it.id == todo.id }
        todosFlow.value = todoList
    }

    override fun getFavorites(): LiveData<List<Todo>> {
        return MutableLiveData(todoList.filter { it.isFavorite })
    }

    override fun getCompleted(): LiveData<List<Todo>> {
        return MutableLiveData(todoList.filter { it.isCompleted })
    }

    override fun getSortedByDate(): LiveData<List<Todo>> {
        return MutableLiveData(todoList.sortedBy { it.startDate })
    }

    override fun getSortedByPriority(): LiveData<List<Todo>> {
        return MutableLiveData(todoList.sortedBy { it.priority })
    }

    override fun getByCategory(category: String): LiveData<List<Todo>> {
        return MutableLiveData(todoList.filter { it.category == category })
    }

    fun deleteOldCompleted(date: String) {
        todoList.removeAll { it.isCompleted && it.endDate < date }
        todosFlow.value = todoList

    }

    private fun emitList() {
        todosFlow.value = todoList.toList()
    }
}

