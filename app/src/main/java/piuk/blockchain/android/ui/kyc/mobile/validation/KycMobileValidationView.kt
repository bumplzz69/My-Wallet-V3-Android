package piuk.blockchain.android.ui.kyc.mobile.validation

import androidx.annotation.StringRes
import piuk.blockchain.android.ui.kyc.mobile.entry.models.PhoneVerificationModel
import io.reactivex.rxjava3.core.Observable
import piuk.blockchain.android.ui.base.View
import piuk.blockchain.androidcore.data.settings.PhoneNumber

interface KycMobileValidationView : View {

    val uiStateObservable: Observable<Pair<PhoneVerificationModel, Unit>>

    val resendObservable: Observable<Pair<PhoneNumber, Unit>>

    fun showProgressDialog()

    fun dismissProgressDialog()

    fun continueSignUp()

    fun displayErrorDialog(@StringRes message: Int)

    fun theCodeWasResent()
}
