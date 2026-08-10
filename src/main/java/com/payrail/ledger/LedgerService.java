package com.payrail.ledger;

import com.payrail.merchant.Merchant;
import com.payrail.payment.Payment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerService {

    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerAccountRepository ledgerAccountRepository, LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public void postPaymentSuccess(Payment payment) {
        LedgerAccount clearingAccount = getOrCreatePlatformAccount(LedgerAccountType.CLEARING, payment.getCurrency());
        LedgerAccount merchantPayableAccount = getOrCreateMerchantAccount(
                payment.getMerchant(), LedgerAccountType.MERCHANT_PAYABLE, payment.getCurrency());

        ledgerEntryRepository.save(new LedgerEntry(
                payment, clearingAccount, LedgerEntryType.DEBIT, payment.getAmountMinor(), payment.getCurrency()));

        ledgerEntryRepository.save(new LedgerEntry(
                payment, merchantPayableAccount, LedgerEntryType.CREDIT, payment.getAmountMinor(), payment.getCurrency()));
    }

    private LedgerAccount getOrCreatePlatformAccount(LedgerAccountType type, String currency) {
        return ledgerAccountRepository.findByMerchantIsNullAndAccountTypeAndCurrency(type, currency)
                .orElseGet(() -> ledgerAccountRepository.save(new LedgerAccount(null, type, currency)));
    }

    private LedgerAccount getOrCreateMerchantAccount(Merchant merchant, LedgerAccountType type, String currency) {
        return ledgerAccountRepository.findByMerchantAndAccountTypeAndCurrency(merchant, type, currency)
                .orElseGet(() -> ledgerAccountRepository.save(new LedgerAccount(merchant, type, currency)));
    }
}