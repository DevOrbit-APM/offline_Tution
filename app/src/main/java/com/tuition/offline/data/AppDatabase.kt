package com.tuition.offline.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

class Converters {
    @androidx.room.TypeConverter fun studentStatus(v: StudentStatus) = v.name
    @androidx.room.TypeConverter fun studentStatus(v: String) = StudentStatus.valueOf(v)
    @androidx.room.TypeConverter fun paymentMode(v: PaymentMode) = v.name
    @androidx.room.TypeConverter fun paymentMode(v: String) = PaymentMode.valueOf(v)
    @androidx.room.TypeConverter fun paymentStatus(v: PaymentStatus) = v.name
    @androidx.room.TypeConverter fun paymentStatus(v: String) = PaymentStatus.valueOf(v)
}

@Database(
    entities = [
        StudentEntity::class, FeeRecordEntity::class, PaymentEntity::class,
        PaymentCorrectionEntity::class, PaymentReversalEntity::class,
        AttendanceEntity::class, AuditLogEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tuitionDao(): TuitionDao

    companion object {
        fun create(context: Context) =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "tuition_offline.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
