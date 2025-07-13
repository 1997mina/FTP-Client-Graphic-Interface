package com.example.studentmanagerapp.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import com.example.studentmanagerapp.database.Student
import com.example.studentmanagerapp.R
import com.example.studentmanagerapp.viewmodel.StudentViewModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditStudentActivity : AppCompatActivity() {
    private var isUpdate: Boolean = false
    private lateinit var currentStudent: Student

    @SuppressLint("SetTextI18n")
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_student)

        isUpdate = intent.getBooleanExtra("IS_UPDATE", false)

        if (isUpdate) {
            currentStudent = intent.getParcelableExtra("STUDENT")!!
            findViewById<EditText>(R.id.edit_full_name).setText(currentStudent.fullName)
            findViewById<EditText>(R.id.edit_student_id).setText(currentStudent.studentId)
            findViewById<EditText>(R.id.edit_email).setText(currentStudent.email)
            findViewById<EditText>(R.id.edit_phone).setText(currentStudent.phone)
            findViewById<Button>(R.id.save_button).text = "Cập nhật"
            findViewById<TextView>(R.id.add_student_title).text = "Cập nhật thông tin sinh viên"
        }

        findViewById<Button>(R.id.save_button).setOnClickListener {
            saveStudent()
        }

        findViewById<Button>(R.id.exit_button).setOnClickListener {
            finish()
        }
    }

    private fun saveStudent() {
        val fullName = findViewById<EditText>(R.id.edit_full_name).text.toString()
        val studentId = findViewById<EditText>(R.id.edit_student_id).text.toString()
        val email = findViewById<EditText>(R.id.edit_email).text.toString()
        val phone = findViewById<EditText>(R.id.edit_phone).text.toString()

        if (fullName.isNotEmpty() && studentId.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty()) {
            val viewModel = ViewModelProvider(this)[StudentViewModel::class.java]

            viewModel.viewModelScope.launch {
                if (isUpdate) {
                    val updatedStudent = currentStudent.copy(
                        fullName = fullName,
                        studentId = studentId,
                        email = email,
                        phone = phone
                    )
                    viewModel.update(updatedStudent)
                } else {
                    val newStudent = Student(
                        fullName = fullName,
                        studentId = studentId,
                        email = email,
                        phone = phone
                    )
                    viewModel.insert(newStudent)
                }

                withContext(Dispatchers.Main) {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        } else {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin của sinh viên", Toast.LENGTH_SHORT).show()
        }
    }
}