package com.example.studentmanagerapp.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.example.studentmanagerapp.R
import com.example.studentmanagerapp.database.Student

@Suppress("DEPRECATION")
class StudentDetailActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.student_detail_info)

        val student = intent.getParcelableExtra<Student>("STUDENT") ?: run {
            Toast.makeText(this, "Không có dữ liệu sinh viên", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        displayStudentInfo(student)

        findViewById<Button>(R.id.btn_send_email).setOnClickListener {
            sendEmail(student.email)
        }

        findViewById<Button>(R.id.btn_call).setOnClickListener {
            makePhoneCall(student.phone)
        }

        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayStudentInfo(student: Student) {
        findViewById<TextView>(R.id.tv_full_name).text = student.fullName
        findViewById<TextView>(R.id.tv_student_id).text = "MSSV: ${student.studentId}"
        findViewById<TextView>(R.id.tv_email).text = student.email
        findViewById<TextView>(R.id.tv_phone).text = student.phone
    }

    private fun sendEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:$email".toUri()
        }
        startActivity(Intent.createChooser(intent, "Gửi email bằng..."))
    }

    private fun makePhoneCall(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                CALL_PERMISSION_REQUEST_CODE
            )
            return
        }

        val intent = Intent(Intent.ACTION_CALL).apply {
            data = "tel:$phoneNumber".toUri()
        }
        startActivity(intent)
    }

    companion object {
        private const val CALL_PERMISSION_REQUEST_CODE = 101
    }
}