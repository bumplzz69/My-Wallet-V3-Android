package piuk.blockchain.android.data.api.bitpay

import io.reactivex.rxjava3.core.Completable
import piuk.blockchain.android.data.api.bitpay.models.RawPaymentRequest
import io.reactivex.rxjava3.core.Single
import piuk.blockchain.android.data.api.bitpay.models.BitPaymentRequest
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import java.util.Locale

class BitPayDataManager constructor(
    private val bitPayService: BitPayService
) {

    /**
     * Returns a [RawPaymentRequest] object containing details about the BitPay invoice payment
     * request
     *
     * @param invoiceId The id of the BitPay invoice
     *
     * @return A [RawPaymentRequest] object wrapped in a [Single].
     */
    fun getRawPaymentRequest(invoiceId: String, currencyCode: String): Single<RawPaymentRequest> =
        bitPayService.getRawPaymentRequest(
            invoiceId = invoiceId,
            chain = currencyCode.toUpperCase(Locale.getDefault())
        ).applySchedulers()

    fun paymentVerificationRequest(invoiceId: String, paymentRequest: BitPaymentRequest):
        Completable =
        bitPayService.getPaymentVerificationRequest(
            invoiceId = invoiceId,
            body = paymentRequest
        ).applySchedulers()

    fun paymentSubmitRequest(invoiceId: String, paymentRequest: BitPaymentRequest):
        Completable =
        bitPayService.getPaymentSubmitRequest(
            invoiceId = invoiceId,
            body = paymentRequest
        ).applySchedulers()
}