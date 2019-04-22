package com.dev.lokeshkalal.todomvi.view.tasks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.GravityCompat
import com.dev.lokeshkalal.todomvi.R
import com.dev.lokeshkalal.todomvi.intent.TasksIntentFactory
import com.dev.lokeshkalal.todomvi.model.TaskEditorModelStore
import com.dev.lokeshkalal.todomvi.model.TaskEditorState
import com.dev.lokeshkalal.todomvi.util.replaceFragmentInActivity
import com.dev.lokeshkalal.todomvi.util.setupActionBar
import com.dev.lokeshkalal.todomvi.view.EventObservable
import com.dev.lokeshkalal.todomvi.view.StateSubscriber
import com.dev.lokeshkalal.todomvi.view.addedittask.AddEditTaskActivity
import com.dev.lokeshkalal.todomvi.view.statistics.StatisticsActivity
import com.jakewharton.rxbinding2.support.design.widget.itemSelections
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.tasks_act.*
import javax.inject.Inject


/**
 * Tasks Activity houses the Toolbar, the nav UI, the FAB and the fragment holding the tasks list.
 */
class TasksActivity : AppCompatActivity(),
    StateSubscriber<TaskEditorState>,
    EventObservable<TasksViewEvent>
{
    // NOTE: We connect to _editor_ model here.
    @Inject lateinit var editorModelStore: TaskEditorModelStore

    // NOTE: We still only generate "Tasks" ViewEvents.
    @Inject lateinit var tasksIntentFactory: TasksIntentFactory

    private val disposables = CompositeDisposable()

    /**
     * TasksActivity starts the AddEditTaskActivity when it detects the
     * `TaskEditorStore` has transitioned to a `TaskEditorState.Editing` state.
     */
    override fun Observable<TaskEditorState>.subscribeToState(): Disposable {
        return ofType<TaskEditorState.Editing>().subscribe {
            // The Android kind
            val intent = Intent(this@TasksActivity, AddEditTaskActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * TasksActivity owns the Floating Action Button, and is the source
     * for `TasksViewEvent.NewTaskClick` events.
     */
    override fun events(): Observable<TasksViewEvent> {
        return newTaskFloatingActionButton.clicks().map { TasksViewEvent.NewTaskClick }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tasks_act)

        // Set up the toolbar.
        setupActionBar(R.id.toolbar) {
            setHomeAsUpIndicator(R.drawable.ic_menu)
            setDisplayHomeAsUpEnabled(true)
        }

        // Set up the navigation drawer.
        drawerLayout.apply {
            setStatusBarBackground(R.color.colorPrimaryDark)
        }

        // Use existing content fragment, or create one from scratch.
        supportFragmentManager.findFragmentById(R.id.contentFrame) as TasksFragment?
            ?: TasksFragment().also {
                replaceFragmentInActivity(it, R.id.contentFrame)
            }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Open the navigation drawer when the home icon is selected from the toolbar.
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        disposables += subscribeNavHandling()
        disposables += events().subscribe(tasksIntentFactory::process)
        disposables += editorModelStore.modelState().subscribeToState()
    }

    // NOTE: If something doesn't impact your Model/Domain, it's ok to call it "ViewLogic".
    private fun subscribeNavHandling():Disposable {
        return navView.itemSelections().subscribe { menuItem ->
            when (menuItem.itemId) {
                R.id.statistics_navigation_menu_item -> {
                    Intent(this@TasksActivity, StatisticsActivity::class.java)
                        .also { startActivity(it) }
                }
            }
            menuItem.isChecked = false
            drawerLayout.closeDrawers()
        }
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }
}