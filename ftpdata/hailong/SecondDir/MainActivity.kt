package com.example.studentmanagerapp.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.widget.SearchView
import android.widget.Toast

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.studentmanagerapp.R
import com.example.studentmanagerapp.database.Student
import com.example.studentmanagerapp.adapter.StudentListAdapter
import com.example.studentmanagerapp.viewmodel.StudentViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity: AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: StudentViewModel
    private lateinit var adapter: StudentListAdapter
    private lateinit var fabAddStudent: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.student_recycler_view)
        fabAddStudent = findViewById(R.id.fab_add_student)
        viewModel = ViewModelProvider(this)[StudentViewModel::class.java]

        recyclerView.layoutManager = LinearLayoutManager(this)
        updateStudentList("")

        fabAddStudent.setOnClickListener {
            openAddStudentActivity()
        }

        adapter = StudentListAdapter { student, action ->
            handleItemAction(student, action)
        }
        recyclerView.adapter = adapter

        viewModel.allStudents.observe(this) { students ->
            adapter.submitList(students)
        }

        registerForContextMenu(recyclerView)

        setupSearchView()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                lastClickedStudent?.let { sendSms(it) }
            } else {
                Toast.makeText(this, "Cần cấp quyền để gửi tin nhắn", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    private fun handleItemAction(student: Student, action: String) {
        when (action) {
            "update" -> updateStudent(student)
            "delete" -> confirmDeleteStudent(student)
            "call" -> callStudent(student.phone)
            "email" -> emailStudent(student.email)
            "sms" -> {
                lastClickedStudent = student
                sendSms(student)
            }
        }
    }

    private fun setupSearchView() {
        val searchView = findViewById<SearchView>(R.id.search_view)

        searchView.isIconified = true

        searchView.setOnCloseListener {
            updateStudentList("")
            false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                updateStudentList(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                updateStudentList(newText)
                return true
            }
        })

        searchView.setOnClickListener {
            searchView.isIconified = false
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == ADD_STUDENT_REQUEST || requestCode == UPDATE_STUDENT_REQUEST) && resultCode == RESULT_OK) {
            updateStudentList()
        }
    }

    @Suppress("DEPRECATION")
    private fun openAddStudentActivity() {
        val intent = Intent(this, EditStudentActivity::class.java)
        startActivityForResult(intent, ADD_STUDENT_REQUEST)
    }

    @Suppress("DEPRECATION")
    private fun updateStudent(student: Student) {
        val intent = Intent(this, EditStudentActivity::class.java).apply {
            putExtra("STUDENT", student)
            putExtra("IS_UPDATE", true)
        }
        startActivityForResult(intent, UPDATE_STUDENT_REQUEST)
    }

    private fun updateStudentList(query: String = "") {
        val students = if (query.isEmpty()) {
            viewModel.allStudents
        } else {
            viewModel.searchResults
        }

        students.observe(this) { student ->
            adapter.submitList(student)
        }
    }

    private fun confirmDeleteStudent(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa sinh viên ${student.fullName}?")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.delete(student)
                updateStudentList()
                Toast.makeText(this, "Đã xóa sinh viên", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun callStudent(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = "tel:$phoneNumber".toUri()
        }
        startActivity(intent)
    }

    private fun emailStudent(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        }
        startActivity(Intent.createChooser(intent, "Gửi email bằng..."))
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun sendSms(student: Student) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                SMS_PERMISSION_REQUEST_CODE
            )
            return
        }

        val uri = "smsto:${student.phone}".toUri()
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", "Xin chào ${student.fullName},\n")
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Không tìm thấy ứng dụng nhắn tin", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val ADD_STUDENT_REQUEST = 1
        const val UPDATE_STUDENT_REQUEST = 2

        private const val SMS_PERMISSION_REQUEST_CODE = 101
        private var lastClickedStudent: Student? = null
    }
}