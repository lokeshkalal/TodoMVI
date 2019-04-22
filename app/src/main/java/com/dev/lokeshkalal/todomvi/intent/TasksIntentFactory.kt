package com.dev.lokeshkalal.todomvi.intent

import com.dev.lokeshkalal.todomvi.model.*
import com.dev.lokeshkalal.todomvi.model.backend.TasksRestApi
import com.dev.lokeshkalal.todomvi.view.tasks.TasksViewEvent
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TasksIntentFactory is responsible for turning TasksViewEvent into
 * Intent<TasksState>, and coordinates with any other dependencies such as
 * ModelStores, Repositories or Services.
 *
 * @see AddEditTaskIntentFactory for state machine safety example.
 */
@Singleton
class TasksIntentFactory @Inject constructor(
    private val tasksModelStore: TasksModelStore,
    private val taskEditorModelStore: TaskEditorModelStore,
    private val tasksRestApi: TasksRestApi
){
    fun process(event: TasksViewEvent) {
        tasksModelStore.process(toIntent(event))
    }

    private fun toIntent(viewEvent: TasksViewEvent): Intent<TasksState> {
        return when(viewEvent) {
            TasksViewEvent.ClearCompletedClick -> buildClearCompletedIntent()
            TasksViewEvent.FilterTypeClick -> buildCycleFilterIntent()
            TasksViewEvent.RefreshTasksSwipe, TasksViewEvent.RefreshTasksClick -> buildReloadTasksIntent()
            TasksViewEvent.NewTaskClick -> buildNewTaskIntent()
            is TasksViewEvent.CompleteTaskClick -> buildCompleteTaskClick(viewEvent)
            is TasksViewEvent.EditTaskClick -> buildEditTaskIntent(viewEvent)
        }
    }

    private fun buildEditTaskIntent(viewEvent: TasksViewEvent.EditTaskClick): Intent<TasksState> {
        // We use `sideEffect{}` here since we're entirely delegating the work.
        return sideEffect {
            // We can assert things about the TasksStore state.
            assert(tasks.contains(viewEvent.task))

            // Editing a task then only involves opening it.
            val intent = AddEditTaskIntentFactory.buildEditTaskIntent(viewEvent.task)
            taskEditorModelStore.process(intent)
        }
    }

    private fun buildNewTaskIntent(): Intent<TasksState> = sideEffect {
        val addIntent = AddEditTaskIntentFactory.buildAddTaskIntent(Task())
        taskEditorModelStore.process(addIntent)
    }

    private fun buildCompleteTaskClick(viewEvent: TasksViewEvent.CompleteTaskClick): Intent<TasksState> {
        return intent {
            // We need to operate on the tasks list here.
            val mutableList = tasks.toMutableList()
            // Replaces old task in the list with a new updated copy.
            mutableList[tasks.indexOf(viewEvent.task)] =
                viewEvent.task.copy(completed = viewEvent.checked)
            // Take the modified list, and create a new copy of tasksState with it.
            copy(tasks = mutableList)
        }
    }

    // Getting comfortable with simple DSL-style-builders is valuable in MVI.
    private fun chainedIntent(block:TasksState.()->TasksState) =
        tasksModelStore.process(intent(block))

    private fun buildReloadTasksIntent(): Intent<TasksState> {
        return intent {

            assert(syncState == SyncState.IDLE)

            fun retrofitSuccess(loadedTasks:List<Task>) = chainedIntent {
                assert(syncState is SyncState.PROCESS && syncState.type == SyncState.PROCESS.Type.REFRESH)
                copy(tasks = loadedTasks, syncState = SyncState.IDLE)
            }

            fun retrofitError(throwable:Throwable) = chainedIntent {
                assert(syncState is SyncState.PROCESS && syncState.type == SyncState.PROCESS.Type.REFRESH)
                copy(syncState = SyncState.ERROR(throwable))
            }

            val disposable = tasksRestApi.getTasks()
                .map { it.values.toList() }
                .subscribeOn(Schedulers.io())
                .subscribe(::retrofitSuccess, ::retrofitError)

            copy( syncState = SyncState.PROCESS(SyncState.PROCESS.Type.REFRESH, disposable::dispose))
        }
    }

    private fun buildCycleFilterIntent(): Intent<TasksState> {
        return intent {
            copy( filter = when(filter) {
                FilterType.ANY -> FilterType.ACTIVE
                FilterType.ACTIVE -> FilterType.COMPLETE
                FilterType.COMPLETE -> FilterType.ANY
            })
        }
    }

    private fun buildClearCompletedIntent(): Intent<TasksState> {
        return intent {
            copy(tasks = tasks.filter { !it.completed }.toList())
        }
    }

    companion object {
        /** Allows an external Model to save a task. */
        fun buildAddOrUpdateTaskIntent(task:Task) : Intent<TasksState> = intent {
            tasks.toMutableList().let { newList ->
                newList.find { task.id == it.id }?.let {
                    newList[newList.indexOf(it)] = task
                } ?: newList.add(task)
                copy(tasks = newList)
            }
        }

        /** Allows an external model to delete a task. */
        fun buildDeleteTaskIntent(taskId:String): Intent<TasksState> = intent {
            copy( tasks = tasks.toMutableList().apply {
                find { it.id == taskId }?.also { remove(it) }
            })
        }
    }
}