package com.dev.lokeshkalal.todomvi.model

import javax.inject.Inject

class TaskEditorModelStore @Inject constructor() : ModelStore<TaskEditorState>(TaskEditorState.Closed) {
}