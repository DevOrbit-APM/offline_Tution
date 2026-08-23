package com.tuition.offline.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class StudentStatus { ACTIVE, INACTIVE, LEFT }
enum class FeeStatus { PENDING, PARTIAL, PAID, OVERPAID }
enum class PaymentMode { CASH, UPI, BANK_TRANSFER, CARD, CHEQUE, OTHER }
enum class PaymentStatus { ACTIVE, REVERSED }

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val studentId: String = UUID.randomUUID().toString(),
    val name: String,
    val parentName: String = "",
    val standard: String = "",
    val batch: String = "",
    val joiningDate: Long = System.currentTimeMillis(),
    val status: StudentStatus = StudentStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "fee_records",
    indices = [Index(value = ["studentId", "billingPeriod"], unique = true)]
)
data class FeeRecordEntity(
    @PrimaryKey val feeId: String = UUID.randomUUID().toString(),
    val studentId: String,
    val billingPeriod: String, // yyyy-MM
    val feeAmountMinor: Long,
    val discountMinor: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val finalAmountMinor: Long get() = (feeAmountMinor - discountMinor).coerceAtLeast(0)
}

@Entity(tableName = "payments", indices = [Index("feeId")])
data class PaymentEntity(
    @PrimaryKey val paymentId: String = UUID.randomUUID().toString(),
    val feeId: String,
    val amountMinor: Long,
    val paymentDate: Long,
    val paymentMode: PaymentMode,
    val referenceNumber: String = "",
    val remark: String = "",
    val status: PaymentStatus = PaymentStatus.ACTIVE,
    val receiptNumber: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payment_corrections", indices = [Index("paymentId")])
data class PaymentCorrectionEntity(
    @PrimaryKey val correctionId: String = UUID.randomUUID().toString(),
    val paymentId: String,
    val oldAmountMinor: Long,
    val newAmountMinor: Long,
    val oldMode: PaymentMode,
    val newMode: PaymentMode,
    val oldDate: Long,
    val newDate: Long,
    val oldReference: String = "",
    val newReference: String = "",
    val reason: String,
    val correctedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payment_reversals", indices = [Index("paymentId")])
data class PaymentReversalEntity(
    @PrimaryKey val reversalId: String = UUID.randomUUID().toString(),
    val paymentId: String,
    val reversedAmountMinor: Long,
    val reason: String,
    val reversedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "attendance", indices = [Index(value = ["studentId", "date"], unique = true)])
data class AttendanceEntity(
    @PrimaryKey val attendanceId: String = UUID.randomUUID().toString(),
    val studentId: String,
    val date: String, // yyyy-MM-dd
    val present: Boolean,
    val markedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_log")
data class AuditLogEntity(
    @PrimaryKey val auditId: String = UUID.randomUUID().toString(),
    val entityType: String,
    val entityId: String,
    val action: String,
    val detail: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class FeeWithPayments(
    val fee: FeeRecordEntity,
    val payments: List<PaymentEntity>
) {
    val receivedMinor: Long get() = payments.filter { it.status == PaymentStatus.ACTIVE }.sumOf { it.amountMinor }
    val pendingMinor: Long get() = (fee.finalAmountMinor - receivedMinor).coerceAtLeast(0)
    val status: FeeStatus get() = when {
        fee.finalAmountMinor == 0L -> FeeStatus.PAID
        receivedMinor <= 0L -> FeeStatus.PENDING
        receivedMinor < fee.finalAmountMinor -> FeeStatus.PARTIAL
        receivedMinor == fee.finalAmountMinor -> FeeStatus.PAID
        else -> FeeStatus.OVERPAID
    }
}
