package com.blockchain.swap.nabu.models.responses.banktransfer

data class CreateLinkBankResponse(
    val partner: String,
    val id: String,
    val attributes: LinkBankAttrsResponse?
) {
    companion object {
        const val YODLEE_PARTNER = "YODLEE"
    }
}

data class CreateLinkBankRequestBody(
    val currency: String,
    val userId: String,
    val attributes: LinkBankAttributes?
)

data class LinkBankAttributes(
    val userOverride: String = "sbMem5fb5284b1f71e3"
)

data class LinkBankAttrsResponse(
    val token: String?,
    val fastlinkUrl: String?
)

data class LinkedBankTransferResponse(
    val id: String,
    val partner: String,
    val currency: String,
    val state: String,
    val details: LinkedBankDetailsResponse?,
    val error: String?
) {
    companion object {
        const val ACTIVE = "ACTIVE"
        const val PENDING = "PENDING"
        const val BLOCKED = "BLOCKED"

        const val ERROR_ALREADY_LINKED = "BANK_TRANSFER_ACCOUNT_ALREADY_LINKED"
        const val ERROR_UNSUPPORTED_ACCOUNT = "BANK_TRANSFER_ACCOUNT_INFO_NOT_FOUND"
    }
}

data class UpdateProviderAccountBody(
    val attributes: ProviderAccountAttrs
)

data class ProviderAccountAttrs(
    val providerAccountId: String,
    val userOverride: String?
)
data class LinkedBankDetailsResponse(
    val accountNumber: String,
    val accountName: String,
    val bankName: String
)