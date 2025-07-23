package com.example.studentmanagerapp.database

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

import java.util.UUID

@SuppressLint("ParcelCreator")
@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: String = UUID.randomUUID().toString(),

    val fullName: String,
    val studentId: String,
    val email: String,
    val phone: String,

    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: Long = System.currentTimeMillis()
): Parcelable {
    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        TODO("Not yet implemented")
    }
}