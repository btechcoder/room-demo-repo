package com.example.pavneet_singh.room_demo_kotin_mvvm_dagger.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.example.pavneet_singh.room_demo_kotin_mvvm_dagger.R
import com.example.pavneet_singh.room_demo_kotin_mvvm_dagger.adapter.NotesAdapter
import com.example.pavneet_singh.room_demo_kotin_mvvm_dagger.databinding.ActivityMainBinding
import com.example.pavneet_singh.room_demo_kotin_mvvm_dagger.notedb.NoteDataBase
import com.example.pavneet_singh.room_demo_kotin_mvvm_dagger.notedb.model.Note
import com.example.pavneet_singh.room_demo_kotin_mvvm_dagger.util.DependenciesProvider
import com.example.pavneet_singh.room_demo_kotin_mvvm_dagger.viewmodels.NoteListViewModel

/**
 * Created by Pavneet_Singh on 2020-01-25.
 */

class NoteListActivity : AppCompatActivity(), NotesAdapter.OnNoteItemClick {
    private lateinit var noteDatabase: NoteDataBase
    private lateinit var notes: MutableList<Note>
    private lateinit var notesAdapter: NotesAdapter
    private var pos = 0
    private lateinit var optionDialog: AlertDialog
    private lateinit var binding: ActivityMainBinding
    private lateinit var note: Note


    private val noteListViewModel: NoteListViewModel by viewModels {
        DependenciesProvider.getNoteListFactory(this@NoteListActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        initializeVies()
        displayList()
    }

    /**
     * Setup the database and live observer to show the list
     * You can use anonymous class instead of
     * [Lambda expressions or Method reference](https://developer.android.com/studio/write/java8-support#supported_features) for observer as
     * <pre>`noteDatabase
     * .getNoteDao()
     * .getNotes()
     * .observe(NoteListActivity.this, object: Observer<List<Note>>{
     *     override fun onChanged(t: List<Note>?) {
     *         // process notes result
     *     }
     * }`</pre>
     */
    private fun displayList() {
        binding.noteListViewModel = noteListViewModel
        binding.notesAdapter = notesAdapter
        noteListViewModel.notesLiveData.observe(this@NoteListActivity) {
            noteListViewModel.isListEmpty.set(it.isEmpty())
        }
    }

    private fun initializeVies() {
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val fab = binding.fab
        fab.setOnClickListener(listener)
        notes = mutableListOf()
        notesAdapter = NotesAdapter(
            notes,
            this@NoteListActivity
        )
        optionDialog = getItemOptionBuilder()
    }

    private val listener =
        View.OnClickListener {
            startActivity(
                Intent(
                    this@NoteListActivity,
                    AddNoteActivity::class.java
                )
            )
        }

    override fun onNoteClick(note: Note) {
        this.note = note
        optionDialog.show()
    }

    private fun getItemOptionBuilder(): AlertDialog {
        return AlertDialog.Builder(this@NoteListActivity)
            .setTitle("Select Options")
            .setItems(
                arrayOf("View", "Update", "Delete")
            ) { _: DialogInterface?, i: Int ->
                when (i) {
                    0 -> showNoteDetails()
                    1 -> startActivityToAddNote()
                    2 -> deleteNote()
                }
            }.create()
    }

    private fun startActivityToAddNote() {
        startActivity(
            getAddActivityIntent()
        )
    }

    private fun showNoteDetails() {
        startActivity(
            getAddActivityIntent()
                .putExtra("viewNote", true)
        )
    }

    private fun getAddActivityIntent(): Intent {
        return Intent(
            this@NoteListActivity,
            AddNoteActivity::class.java
        ).putExtra("note", note)
    }

    private fun deleteNote() {
        noteListViewModel.deleteNote(note).observe(this) {
            if (it < 0) {
                Toast.makeText(
                    this,
                    "Deletion failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        cleanUp()
        super.onDestroy()
    }

    private fun cleanUp() {
        noteDatabase.cleanUp()
        if (optionDialog.isShowing) {
            optionDialog.dismiss()
        }
    }
}