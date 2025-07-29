package com.todo.todolist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.todo.todolist.data.Todo
import com.todo.todolist.repository.FakeTodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@ExperimentalCoroutinesApi
class TodoViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var viewModel: TestViewModel
    private lateinit var fakeTodoRepository: FakeTodoRepository

    @Before
    fun setUp(){
        Dispatchers.setMain(StandardTestDispatcher())
        fakeTodoRepository = FakeTodoRepository()
        viewModel = TestViewModel(fakeTodoRepository)
    }

    @Test
    fun insertAndGetTodo() = runTest{
        val todo = Todo(
            id = 1,
            userId = "101",
            title = "Unit Test",
            content = "ViewModel Testing",
            startDate = "2025-07-04",
            endDate = "2025-07-04",
            isFavorite = false,
            isCompleted = false,
            priority= "High",
            category = "work"
        )

        fakeTodoRepository.insert(todo)
        val result = fakeTodoRepository.getAllTodo().getOrAwaitValue()
        assertEquals(1, result.size)
        assertEquals(todo, result[0])
    }

    @Test
    fun updateAndShowTodo() = runTest{
        val todo = Todo(
            id = 2,
            userId = "102",
            title = "Unit Test",
            content = "updating....",
            startDate = "2025-07-03",
            endDate = "2025-07-04",
            isFavorite = false,
            isCompleted = false,
            priority= "Medium",
            category = "personal"
        )
        fakeTodoRepository.insert(todo)

        val updatedTodo = todo.copy(isCompleted = true)
        fakeTodoRepository.update(updatedTodo)

        val result = fakeTodoRepository.getCompleted().getOrAwaitValue()

        assertEquals(1, result.size)
        assertEquals(true, result[0].isCompleted )
    }

    @Test
    fun deleteAndGetTodo() = runTest{
        val todo = Todo(
            id = 3,
            userId = "103",
            title = "Unit Test",
            content = "Deleting...",
            startDate = "2025-07-01",
            endDate = "2025-07-09",
            isFavorite = false,
            isCompleted = false,
            priority= "low",
            category = "shopping"
        )
        fakeTodoRepository.insert(todo)
        fakeTodoRepository.delete(todo)
        val result = fakeTodoRepository.getAllTodo().getOrAwaitValue()
        assertEquals(0, result.size)
    }

    @Test
    fun favAndGetTodo() = runTest{
        val todo = Todo(
            id = 4,
            userId = "104",
            title = "Unit Test",
            content = "Adding Favorites...",
            startDate = "2025-06-04",
            endDate = "2025-07-04",
            isFavorite = false,
            isCompleted = false,
            priority= "High",
            category = "work"
        )
        fakeTodoRepository.insert(todo)
        val updateTodo = todo.copy(isFavorite = true)
        fakeTodoRepository.update(updateTodo)
        val result = fakeTodoRepository.getFavorites().getOrAwaitValue()
        assertEquals(1, result.size)
        assertEquals(true, result[0].isFavorite)
    }

    @Test
    fun getByCategoryAndShowTodo() = runTest{
        val todo = Todo(
            id = 5,
            userId = "105",
            title = "Unit Test",
            content = "Categoring listing...",
            startDate = "2025-07-02",
            endDate = "2025-07-28",
            isFavorite = false,
            isCompleted = false,
            priority= "High",
            category = "work"
        )
        fakeTodoRepository.insert(todo)
        val result = fakeTodoRepository.getByCategory("work").getOrAwaitValue()
        assertEquals(1, result.size)
        assertEquals("work", result[0].category)
    }
    @Test
    fun sortByDate() = runTest{
        val todo1 = Todo(
            id = 1,
            userId = "101",
            title = "Unit Test 1",
            content = "todo 1",
            startDate = "2025-07-04",
            endDate = "2025-07-04",
            isFavorite = true,
            isCompleted = true,
            priority= "High",
            category = "work"
        )

        val todo2 = Todo(
            id = 2,
            userId = "102",
            title = "Unit Test 2",
            content = "todo 2",
            startDate = "2025-07-02",
            endDate = "2025-07-05",
            isFavorite = false,
            isCompleted = true,
            priority= "Low",
            category = "shopping"
        )

        val todo3 = Todo(
            id = 3,
            userId = "103",
            title = "Unit Test 3",
            content = "Todo 3",
            startDate = "2025-06-04",
            endDate = "2025-07-04",
            isFavorite = true,
            isCompleted = false,
            priority= "medium",
            category = "personal"
        )
        fakeTodoRepository.insert(todo1)
        fakeTodoRepository.insert(todo2)
        fakeTodoRepository.insert(todo3)

        val result= fakeTodoRepository.getSortedByDate().getOrAwaitValue()

        assertEquals(listOf(todo3, todo2, todo1),result)
    }

    @Test
    fun sortByPriority() = runTest {
        val todo1 = Todo(
            id = 1,
            userId = "101",
            title = "Unit Test 1",
            content = "todo 1",
            startDate = "2025-07-04",
            endDate = "2025-07-04",
            isFavorite = true,
            isCompleted = true,
            priority = "High",
            category = "work"
        )

        val todo2 = Todo(
            id = 2,
            userId = "102",
            title = "Unit Test 2",
            content = "todo 2",
            startDate = "2025-07-02",
            endDate = "2025-07-05",
            isFavorite = false,
            isCompleted = true,
            priority = "Low",
            category = "shopping"
        )

        val todo3 = Todo(
            id = 3,
            userId = "103",
            title = "Unit Test 3",
            content = "Todo 3",
            startDate = "2025-06-04",
            endDate = "2025-07-04",
            isFavorite = true,
            isCompleted = false,
            priority = "Medium",
            category = "personal"
        )
        fakeTodoRepository.insert(todo1)
        fakeTodoRepository.insert(todo2)
        fakeTodoRepository.insert(todo3)

        val result= fakeTodoRepository.getSortedByPriority().getOrAwaitValue()
        assertEquals(listOf(todo1, todo3, todo2),result)

    }
    fun <T> LiveData<T>.getOrAwaitValue(time: Long =2, timeUnit: TimeUnit = TimeUnit.SECONDS): T{
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T>{
            override fun onChanged(t: T) {
                data = t
                latch.countDown()
                this@getOrAwaitValue.removeObserver(this)
            }
        }
        this.observeForever(observer)
        if(!latch.await(time, timeUnit)){
            throw TimeoutException("LiveData value was never set.")
        }
        @Suppress("UNCHECKED_CAST")
        return data as T
    }
}