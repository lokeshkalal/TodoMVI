package com.dev.lokeshkalal.todomvi.view.addedittask

sealed class AddEditTaskViewEvents {
    data class TitleChange(val title :String) : AddEditTaskViewEvents()
    data class DescriptionChange(val description : String): AddEditTaskViewEvents()
    object  SaveTaskClick : AddEditTaskViewEvents()
    object DeleteTaskClick : AddEditTaskViewEvents()
    object CancelTaskClick : AddEditTaskViewEvents()
}