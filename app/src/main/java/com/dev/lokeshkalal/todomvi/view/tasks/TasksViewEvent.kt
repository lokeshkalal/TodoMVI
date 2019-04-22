package com.dev.lokeshkalal.todomvi.view.tasks

import com.dev.lokeshkalal.todomvi.model.Task

sealed class TasksViewEvent {
    object NewTaskClick : TasksViewEvent()
    object FilterTypeClick : TasksViewEvent()
    object ClearCompletedClick : TasksViewEvent()
    object RefreshTasksClick : TasksViewEvent()
    object RefreshTasksSwipe : TasksViewEvent()
    data class CompleteTaskClick(val task: Task, val checked: Boolean) : TasksViewEvent()
    data class EditTaskClick(val task: Task) : TasksViewEvent()
}