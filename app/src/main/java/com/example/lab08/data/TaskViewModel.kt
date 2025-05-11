
package com.example.lab08.data

import androidx.lifecycle.ViewModel



import androidx.lifecycle.viewModelScope



import kotlinx.coroutines.flow.MutableStateFlow



import kotlinx.coroutines.flow.SharingStarted



import kotlinx.coroutines.flow.StateFlow



import kotlinx.coroutines.flow.combine



import kotlinx.coroutines.flow.flatMapLatest



import kotlinx.coroutines.flow.onEach



import kotlinx.coroutines.flow.stateIn



import kotlinx.coroutines.launch

class TaskViewModel(private val dao: TaskDao) : ViewModel() {



    private val _filterType = MutableStateFlow(FilterType.ALL)

    private val _searchQuery = MutableStateFlow("")



    private val _filteredTasks = combine(_filterType, _searchQuery) { filterType, query ->

        Pair(filterType, query)

    }.flatMapLatest { (filterType, query) ->

        when (filterType) {

            FilterType.ALL -> {

                if (query.isEmpty()) {

                    dao.getAllTasks()

                } else {

                    dao.searchTasks(query)

                }

            }

            FilterType.COMPLETED -> {

                if (query.isEmpty()) {

                    dao.getTasksByCompletionStatus(true)

                } else {

                    dao.searchTasks(query).onEach { it.filter { task -> task.isCompleted } }

                }

            }

            FilterType.PENDING -> {

                if (query.isEmpty()) {

                    dao.getTasksByCompletionStatus(false)

                } else {

                    dao.searchTasks(query).onEach { it.filter { task -> !task.isCompleted } }

                }

            }

        }

    }



    val tasks: StateFlow<List<Task>> = _filteredTasks.stateIn(

        viewModelScope,

        SharingStarted.WhileSubscribed(5000),

        emptyList()

    )



    fun addTask(description: String) {

        val newTask = Task(description = description)

        viewModelScope.launch {

            dao.insertTask(newTask)

        }

    }



    fun toggleTaskCompletion(task: Task) {

        viewModelScope.launch {

            val updatedTask = task.copy(isCompleted = !task.isCompleted)

            dao.updateTask(updatedTask)

        }

    }



    fun deleteTask(task: Task) {

        viewModelScope.launch {

            dao.deleteTask(task)

        }

    }



    fun deleteAllTasks() {

        viewModelScope.launch {

            dao.deleteAllTasks()

        }

    }



    fun editTask(task: Task, newDescription: String) {

        viewModelScope.launch {

            val updatedTask = task.copy(description = newDescription)

            dao.updateTask(updatedTask)

        }

    }



    fun setFilter(filterType: FilterType) {

        _filterType.value = filterType

    }



    fun setSearchQuery(query: String) {

        _searchQuery.value = query

    }



    enum class FilterType {

        ALL,

        COMPLETED,

        PENDING

    }

}