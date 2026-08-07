package com.example.securedoselog.security

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText

fun EditText.disableCopyCut() {
    customSelectionActionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean = true

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menu?.removeItem(android.R.id.copy)
            menu?.removeItem(android.R.id.cut)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean = false

        override fun onDestroyActionMode(mode: ActionMode?) {}
    }
}