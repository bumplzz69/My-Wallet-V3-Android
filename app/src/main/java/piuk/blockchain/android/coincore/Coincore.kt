package piuk.blockchain.android.coincore

import com.blockchain.logging.CrashLogger
import com.blockchain.wallet.DefaultLabels
import info.blockchain.balance.CryptoCurrency
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import piuk.blockchain.android.coincore.impl.AllWalletsAccount
import piuk.blockchain.android.coincore.impl.TxProcessorFactory
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import timber.log.Timber

private class CoincoreInitFailure(msg: String, e: Throwable) : Exception(msg, e)

class Coincore internal constructor(
    // TODO: Build an interface on PayloadDataManager/PayloadManager for 'global' crypto calls; second password etc?
    private val payloadManager: PayloadDataManager,
    private val assetMap: Map<CryptoCurrency, CryptoAsset>,
    private val txProcessorFactory: TxProcessorFactory,
    private val defaultLabels: DefaultLabels,
    private val fiatAsset: Asset,
    private val crashLogger: CrashLogger
) {
    operator fun get(ccy: CryptoCurrency): CryptoAsset =
        assetMap[ccy] ?: throw IllegalArgumentException(
            "Unknown CryptoCurrency ${ccy.networkTicker}"
        )

    fun init(): Completable =
        Completable.concat(
            assetMap.values.map { asset ->
                Completable.defer { asset.init() }
                    .doOnError {
                        crashLogger.logException(
                            CoincoreInitFailure("Failed init: ${asset.asset.networkTicker}", it)
                        )
                    }
            }.toList()
        ).doOnError {
            Timber.e("Coincore initialisation failed! $it")
        }

    val fiatAssets: Asset = fiatAsset
    val cryptoAssets: Iterable<Asset> = assetMap.values.filter { it.isEnabled }
    val allAssets: Iterable<Asset> = listOf(fiatAsset) + cryptoAssets

    fun validateSecondPassword(secondPassword: String) =
        payloadManager.validateSecondPassword(secondPassword)

    fun allWallets(): Single<AccountGroup> =
        Maybe.concat(
            allAssets.map {
                it.accountGroup().map { grp -> grp.accounts }
            }
        ).reduce { a, l -> a + l }
            .map { list ->
                AllWalletsAccount(list, defaultLabels) as AccountGroup
            }.toSingle()

    fun getTransactionTargets(
        sourceAccount: CryptoAccount,
        action: AssetAction
    ): Single<SingleAccountList> {
        // We only support transfers between similar assets and (soon; to - but not from - fiat)
        // at this time. If and when, say, swap is supported this will need revisiting
        val sameCurrencyTransactionTargets =
            get(sourceAccount.asset).transactionTargets(sourceAccount)

        val fiatTargets = fiatAsset.transactionTargets(sourceAccount)

        val sameCurrencyPlusFiat = sameCurrencyTransactionTargets.zipWith(fiatTargets) { crypto, fiat ->
            crypto + fiat
        }

        return allWallets().map { it.accounts }.flatMap { allWallets ->
            if (action != AssetAction.Swap) {
                sameCurrencyPlusFiat
            } else
                Single.just(allWallets)
        }.map {
            it.filter(getActionFilter(action, sourceAccount))
        }
    }

    private fun getActionFilter(action: AssetAction, sourceAccount: CryptoAccount): (SingleAccount) -> Boolean =
        when (action) {
            AssetAction.Sell -> {
                {
                    it is FiatAccount
                }
            }

            AssetAction.Swap -> {
                {
                    it is CryptoAccount &&
                            it.asset != sourceAccount.asset &&
                            it !is FiatAccount &&
                            it !is InterestAccount &&
                            if (sourceAccount.isCustodial()) it.isCustodial() else true
                }
            }
            AssetAction.Send -> {
                {
                    it !is FiatAccount
                }
            }
            else -> {
                { true }
            }
        }

    fun findAccountByAddress(
        cryptoCurrency: CryptoCurrency,
        address: String
    ): Maybe<SingleAccount> =
        filterAccountsByAddress(assetMap.getValue(cryptoCurrency).accountGroup(AssetFilter.All), address)

    private fun filterAccountsByAddress(
        accountGroup: Maybe<AccountGroup>,
        address: String
    ): Maybe<SingleAccount> =
        accountGroup.map {
            it.accounts
        }.flattenAsObservable { it }
        .flatMapSingle { a ->
            a.receiveAddress
                .map { it as CryptoAddress }
                .onErrorReturn { NullCryptoAddress }
                .map { ca ->
                    if (ca.address.equals(address, true)) {
                        a
                    } else {
                        NullCryptoAccount()
                    }
                }
        }.filter { it != NullCryptoAccount() }
        .toList()
        .flatMapMaybe {
            if (it.isEmpty())
                Maybe.empty<SingleAccount>()
            else
                Maybe.just(it.first())
        }

    fun createTransactionProcessor(
        source: SingleAccount,
        target: TransactionTarget,
        action: AssetAction
    ): Single<TransactionProcessor> =
        txProcessorFactory.createProcessor(
            source as CryptoAccount,
            target,
            action
        )
}
