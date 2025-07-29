package com.todo.todolist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todo.todolist.data.Todo
import com.todo.todolist.data.TodoRepositoryInterface
import com.todo.todolist.repository.FakeTodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TestViewModel (private val repository: TodoRepositoryInterface): ViewModel(){
        private val _currentList = MutableLiveData<List<Todo>>()
        val currentList: LiveData<List<Todo>> = _currentList

        private val allTodo: LiveData<List<Todo>> = repository.getAllTodo()
        private val favorites: LiveData<List<Todo>> = repository.getFavorites()
        private val completed: LiveData<List<Todo>> = repository.getCompleted()
        private val sortedByDate: LiveData<List<Todo>> = repository.getSortedByDate()
        private val sortedByPriority: LiveData<List<Todo>> = repository.getSortedByPriority()

        fun insert(todo: Todo) =viewModelScope.launch {
            repository.insert(todo)
        }

        fun update(todo: Todo) =viewModelScope.launch {
            repository.update(todo)
        }

        fun delete(todo: Todo) =viewModelScope.launch {
            repository.delete(todo)
        }
    fun showAll() {
        allTodo.observeForever{_currentList.value = it}
    }

    fun showFavorites() {
        favorites.observeForever{_currentList.value = it}
    }

    fun showCompleted() {
        completed.observeForever{_currentList.value = it}
    }

    fun sortByDate() {
        sortedByDate.observeForever{_currentList.value = it}
    }

    fun sortByPriority() {
        sortedByPriority.observeForever{_currentList.value = it}
    }

    fun filterByCategory(category: String) {
        repository.getByCategory(category).observeForever{_currentList.value = it}
    }
//    @SuppressLint("NewApi")
//    fun autoDeleteOldTasks() =viewModelScope.launch {
//        val yesterday=LocalDate.now().minusDays(1).toString()
//        repository.deleteOldCompleted(yesterday)
//    }

    }
