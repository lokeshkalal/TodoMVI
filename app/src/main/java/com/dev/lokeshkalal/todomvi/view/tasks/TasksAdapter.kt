package com.dev.lokeshkalal.todomvi.view.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dev.lokeshkalal.todomvi.R
import com.dev.lokeshkalal.todomvi.intent.TasksIntentFactory
import com.dev.lokeshkalal.todomvi.model.Task
import com.dev.lokeshkalal.todomvi.model.TasksModelStore
import com.dev.lokeshkalal.todomvi.model.TasksState
import com.dev.lokeshkalal.todomvi.view.StateSubscriber
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class TasksAdapter @Inject constructor(
    private val layoutInflater: LayoutInflater,
    private val tasksIntent: TasksIntentFactory,
    private val tasksModelStore: TasksModelStore
) : RecyclerView.Adapter<TaskViewHolder>(),
    StateSubscriber<TasksState>
{

    private lateinit var filteredTasks: List<Task>
    private var disposables = CompositeDisposable()

    init {
        setHasStableIds(true)
    }

    override fun Observable<TasksState>.subscribeToState(): Disposable {
        return this
            .map(TasksState::filteredTasks)
            .distinctUntilChanged()
            .subscribe { updatedTasks ->
                filteredTasks = updatedTasks
                notifyDataSetChanged()
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflatedView = layoutInflater.inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(inflatedView).apply {
            events().subscribe(tasksIntent::process)
        }
    }

    override fun getItemCount(): Int = filteredTasks.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(filteredTasks[position])
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        disposables += tasksModelStore.modelState().subscribeToState()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposables.clear()
    }
}

