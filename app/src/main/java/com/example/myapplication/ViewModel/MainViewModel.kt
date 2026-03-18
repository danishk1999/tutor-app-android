package com.example.myapplication.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.Domain.CategoryModel
import com.example.myapplication.Domain.TutorModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainViewModel : ViewModel() {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    private val _category = MutableLiveData<MutableList<CategoryModel>>()
    private val _tutors = MutableLiveData<MutableList<TutorModel>>()
    private val _filteredTutors = MutableLiveData<MutableList<TutorModel>>() // New LiveData for filtered tutors

    val category: LiveData<MutableList<CategoryModel>> = _category
    val tutors: LiveData<MutableList<TutorModel>> = _tutors
    val filteredTutors: LiveData<MutableList<TutorModel>> = _filteredTutors

    // Load categories from Firebase
    fun loadCategory() {
        val ref = firebaseDatabase.getReference("Category")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<CategoryModel>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(CategoryModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _category.value = lists
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors
            }
        })
    }

    // Load tutors from Firebase
    fun loadTutors() {
        val ref = firebaseDatabase.getReference("Tutors")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<TutorModel>()
                for (childSnapshot in snapshot.children) {
                    val tutor = childSnapshot.getValue(TutorModel::class.java)
                    if (tutor != null) {
                        lists.add(tutor)
                    }
                }
                _tutors.value = lists
                _filteredTutors.value = lists // Initialize filteredTutors with all tutors initially
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors
            }
        })
    }

    // Filter tutors based on subject
    fun filterTutorsBySubject(subject: String) {
        val filteredList = _tutors.value?.filter { it.Special == subject }?.toMutableList()
        _filteredTutors.value = filteredList ?: mutableListOf()
    }
}
