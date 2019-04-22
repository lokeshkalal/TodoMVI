package com.dev.lokeshkalal.todomvi.intent

import com.dev.lokeshkalal.todomvi.model.Task
import com.dev.lokeshkalal.todomvi.model.TaskEditorModelStore
import com.dev.lokeshkalal.todomvi.model.TaskEditorState
import com.dev.lokeshkalal.todomvi.model.TasksModelStore
import com.dev.lokeshkalal.todomvi.view.addedittask.AddEditTaskViewEvents
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AddEditTaskIntentFactory is responsible for turning AddEditTaskViewEvents into
 * Intent<TaskEditorState>, and coordinates with any other dependencies, such as
 * ModelStores, Repositories or Services.
 */
@Singleton
class AddEditTaskIntentFactory @Inject constructor(
    private val taskEditorModelStore: TaskEditorModelStore,
    private val tasksModelStore: TasksModelStore
) {

    fun process(viewEvent: AddEditTaskViewEvents) {
        taskEditorModelStore.process(toIntent(viewEvent))
    }

    private fun toIntent(viewEvent: AddEditTaskViewEvents): Intent<TaskEditorState> {
        return when (viewEvent) {
            is AddEditTaskViewEvents.TitleChange -> buildEditTitleIntent(viewEvent)
            is AddEditTaskViewEvents.DescriptionChange -> buildEditDescriptionIntent(viewEvent)
            AddEditTaskViewEvents.SaveTaskClick -> buildSaveIntent()
            AddEditTaskViewEvents.DeleteTaskClick -> buildDeleteIntent()
            AddEditTaskViewEvents.CancelTaskClick -> buildCancelIntent()
        }
    }

    /**
     * An example of delegating work to an external dependency.
     */
    private fun buildSaveIntent() = editorIntent<TaskEditorState.Editing> {
        // This triggers a state change in another ModelStore.
        save().run {
            // NOTE: When we do this with a real backend + retrofit, it will become asynchronous.
            val intent = TasksIntentFactory.buildAddOrUpdateTaskIntent(task)
            tasksModelStore.process(intent)
            saved()
        }
    }

    private fun buildDeleteIntent() = editorIntent<TaskEditorState.Editing> {
        delete().run {
            // `TasksStore` deletes this task from its internal list.
            val intent = TasksIntentFactory.buildDeleteTaskIntent(taskId)
            tasksModelStore.process(intent)
            deleted()
        }
    }

    companion object {
        /**
         * Creates an intent for the TaskEditor state machine.
         *
         * Utility function to cut down on boilerplate.
         */
        inline fun <reified S : TaskEditorState> editorIntent(
            crossinline block: S.() -> TaskEditorState
        ): Intent<TaskEditorState> {
            return intent {
                (this as? S)?.block()
                    ?: throw IllegalStateException("editorIntent encountered an inconsistent State. [Looking for ${S::class.java} but was ${this.javaClass}]")
            }
        }

        fun buildAddTaskIntent(task: Task) = editorIntent<TaskEditorState.Closed> { addTask(task) }

        fun buildEditTaskIntent(task: Task) = editorIntent<TaskEditorState.Closed> { editTask(task) }

        private fun buildEditTitleIntent(viewEvent: AddEditTaskViewEvents.TitleChange) =
            editorIntent<TaskEditorState.Editing> {
                edit { copy(title = viewEvent.title) }
            }

        private fun buildEditDescriptionIntent(viewEvent: AddEditTaskViewEvents.DescriptionChange) =
            editorIntent<TaskEditorState.Editing> {
                edit { copy(description = viewEvent.description) }
            }

        private fun buildCancelIntent() = editorIntent<TaskEditorState.Editing> { cancel() }
    }
}