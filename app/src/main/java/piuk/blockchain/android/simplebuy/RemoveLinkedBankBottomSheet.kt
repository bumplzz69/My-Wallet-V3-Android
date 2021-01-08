package piuk.blockchain.android.simplebuy

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import com.blockchain.koin.scopedInject
import com.blockchain.nabu.datamanagers.CustodialWalletManager
import com.blockchain.nabu.models.data.Bank
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.remove_bank_bottom_sheet.view.*
import kotlinx.android.synthetic.main.remove_bank_bottom_sheet.view.end_digits
import kotlinx.android.synthetic.main.remove_bank_bottom_sheet.view.icon
import kotlinx.android.synthetic.main.remove_bank_bottom_sheet.view.progress
import kotlinx.android.synthetic.main.remove_bank_bottom_sheet.view.title
import kotlinx.android.synthetic.main.remove_card_bottom_sheet.view.*
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.base.SlidingModalBottomDialog
import piuk.blockchain.androidcore.utils.helperfunctions.unsafeLazy
import piuk.blockchain.android.util.visibleIf

class RemoveLinkedBankBottomSheet : SlidingModalBottomDialog() {

    private val compositeDisposable = CompositeDisposable()
    private val custodialWalletManager: CustodialWalletManager by scopedInject()

    private val bank: Bank by unsafeLazy {
        arguments?.getSerializable(BANK_KEY) as Bank
    }
    override val layoutResource: Int = R.layout.remove_bank_bottom_sheet

    override fun initControls(view: View) {
        with(view) {
            title.text = resources.getString(R.string.common_spaced_strings, bank.name, bank.currency)
            end_digits.text = resources.getString(R.string.dotted_suffixed_string, bank.account)
            rmv_bank_btn.setOnClickListener {
                compositeDisposable += custodialWalletManager.deleteBank(bank.id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        updateUi(true)
                    }
                    .doFinally {
                        updateUi(false)
                    }
                    .subscribeBy(onComplete = {
                        analytics.logEvent(SimpleBuyAnalytics.REMOVE_BANK)
                        (parentFragment as? RemovePaymentMethodBottomSheetHost)?.onLinkedBankRemoved(bank.id)
                        dismiss()
                    }, onError = {})
            }
        }
    }

    private fun updateUi(isLoading: Boolean) {
        view?.progress.visibleIf { isLoading }
        view?.icon.visibleIf { !isLoading }
        view?.rmv_card_btn?.isEnabled = !isLoading
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        compositeDisposable.dispose()
    }

    companion object {
        private const val BANK_KEY = "BANK_KEY"

        fun newInstance(bank: Bank) =
            RemoveLinkedBankBottomSheet().apply {
                arguments = Bundle().apply {
                    putSerializable(BANK_KEY, bank)
                }
            }
    }
}