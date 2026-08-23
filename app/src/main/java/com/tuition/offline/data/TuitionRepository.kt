package com.tuition.offline.data

import androidx.room.withTransaction
import java.util.UUID

class TuitionRepository(private val db: AppDatabase) {
    private val dao = db.tuitionDao()

    fun students() = dao.observeStudents()

    suspend fun addStudent(name: String, parent: String, standard: String, batch: String) {
        val s = StudentEntity(name = name.trim(), parentName = parent.trim(), standard = standard.trim(), batch = batch.trim())
        dao.upsertStudent(s)
        audit("Student", s.studentId, "CREATED", "Student added: ${s.name}")
    }

    suspend fun upsertMonthlyFee(studentId: String, period: String, feeMinor: Long, discountMinor: Long) {
        val old = dao.fee(studentId, period)
        val fee = (old ?: FeeRecordEntity(studentId = studentId, billingPeriod = period, feeAmountMinor = feeMinor, discountMinor = discountMinor))
            .copy(feeAmountMinor = feeMinor, discountMinor = discountMinor, updatedAt = System.currentTimeMillis())
        dao.upsertFee(fee)
        audit("FeeRecord", fee.feeId, if (old == null) "CREATED" else "EDITED", "Fee for $period")
    }

    suspend fun addPayment(
        feeId: String,
        amountMinor: Long,
        date: Long,
        mode: PaymentMode,
        reference: String,
        remark: String
    ): PaymentEntity {
        require(amountMinor > 0) { "Payment must be greater than zero." }
        val receipt = "REC-" + System.currentTimeMillis().toString().takeLast(8)
        val p = PaymentEntity(
            feeId = feeId, amountMinor = amountMinor, paymentDate = date,
            paymentMode = mode, referenceNumber = reference, remark = remark,
            receiptNumber = receipt
        )
        dao.insertPayment(p)
        audit("Payment", p.paymentId, "CREATED", "Payment ${p.amountMinor} received through ${p.paymentMode}")
        return p
    }

    suspend fun correctPayment(
        original: PaymentEntity,
        newAmountMinor: Long,
        newDate: Long,
        newMode: PaymentMode,
        newReference: String,
        newRemark: String,
        reason: String
    ) {
        require(reason.isNotBlank()) { "Reason is required for a financial correction." }
        val corrected = original.copy(
            amountMinor = newAmountMinor, paymentDate = newDate, paymentMode = newMode,
            referenceNumber = newReference, remark = newRemark, updatedAt = System.currentTimeMillis()
        )
        db.withTransaction {
            dao.insertCorrection(
                PaymentCorrectionEntity(
                    paymentId = original.paymentId,
                    oldAmountMinor = original.amountMinor, newAmountMinor = newAmountMinor,
                    oldMode = original.paymentMode, newMode = newMode,
                    oldDate = original.paymentDate, newDate = newDate,
                    oldReference = original.referenceNumber, newReference = newReference,
                    reason = reason
                )
            )
            dao.insertPayment(corrected)
            audit("Payment", original.paymentId, "CORRECTED", reason)
        }
    }

    suspend fun reversePayment(payment: PaymentEntity, reason: String) {
        require(reason.isNotBlank()) { "Reason is required for a reversal." }
        if (payment.status == PaymentStatus.REVERSED) return
        db.withTransaction {
            dao.insertReversal(PaymentReversalEntity(paymentId = payment.paymentId, reversedAmountMinor = payment.amountMinor, reason = reason))
            dao.insertPayment(payment.copy(status = PaymentStatus.REVERSED, updatedAt = System.currentTimeMillis()))
            audit("Payment", payment.paymentId, "REVERSED", reason)
        }
    }

    suspend fun audit(type: String, id: String, action: String, detail: String) {
        dao.audit(AuditLogEntity(entityType = type, entityId = id, action = action, detail = detail))
    }
}
