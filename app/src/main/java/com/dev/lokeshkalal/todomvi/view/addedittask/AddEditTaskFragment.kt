package com.dev.lokeshkalal.todomvi.view.addedittask

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dev.lokeshkalal.todomvi.R
import com.dev.lokeshkalal.todomvi.intent.AddEditTaskIntentFactory
import com.dev.lokeshkalal.todomvi.model.TaskEditorModelStore
import com.dev.lokeshkalal.todomvi.model.TaskEditorState
import com.dev.lokeshkalal.todomvi.view.EventObservable
import com.dev.lokeshkalal.todomvi.view.StateSubscriber
import com.dev.lokeshkalal.todomvi.view.addedittask.AddEditTaskViewEvents
import com.jakewharton.rxbinding2.widget.textChanges
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.addtask_act.fab_edit_task_done
import kotlinx.android.synthetic.main.addtask_frag.*
import javax.inject.Inject

/**
 * Fragment for adding/editing tasks.
 */
class AddEditTaskFragment : Fragment(), EventObservable<AddEditTaskViewEvents>, StateSubscriber<TaskEditorState>{

    @Inject
    lateinit var editorModelStore: TaskEditorModelStore
    @Inject
    lateinit var intentFactory: AddEditTaskIntentFactory

    private val disposables = CompositeDisposable()

    override fun events(): Observable<AddEditTaskViewEvents> {
        return Observable.merge(add_task_title.textChanges().map { AddEditTaskViewEvents.TitleChange(it.toString()) }
            , add_task_description.textChanges().map { AddEditTaskViewEvents.DescriptionChange(it.toString()) })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }
    override fun Observable<TaskEditorState>.subscribeToState(): Disposable {
        return ofType<TaskEditorState.Editing>().firstElement().subscribe { editing ->
            add_task_title.setText(editing.task.title)
            add_task_description.setText(editing.task.description)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.addtask_frag, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.addtask_fragment_menu, menu)
    }


    override fun onResume() {
        super.onResume()
        disposables += editorModelStore.modelState().subscribeToState()
        disposables += events().subscribe(intentFactory::process)
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

}