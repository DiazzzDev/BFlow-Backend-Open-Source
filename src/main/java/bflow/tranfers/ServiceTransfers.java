package bflow.tranfers;

import bflow.auth.repository.RepositoryUser;
import bflow.tranfers.DTO.TransferenceRequest;
import bflow.tranfers.DTO.TransferenceResponse;
import bflow.tranfers.entities.Transfer;
import bflow.tranfers.enums.TransferStatus;
import bflow.wallet.RepositoryWallet;
import bflow.wallet.RepositoryWalletUser;
import bflow.wallet.ServiceWallet;
import bflow.wallet.entities.Wallet;
import bflow.wallet.entities.WalletUser;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ServiceTransfers {
    private final RepositoryTransfers  repositoryTransfers;

    /** The repository for wallet database operations. */
    private final RepositoryWallet repositoryWallet;

    /** The repository for wallet-user relationship operations. */
    private final RepositoryWalletUser repositoryWalletUser;

    /** The repository for user database operations. */
    private final RepositoryUser repositoryUser;

    private final ServiceWallet serviceWallet;

    public TransferenceResponse transfer(
            @Valid final TransferenceRequest request,
            final UUID userId
    ) {
        final BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Amount must be greater than zero");
        }

        // Validate access: check if user is linked to this wallet
        WalletUser originWallet = repositoryWalletUser
                .findByWalletIdAndUserId(request.getFromWalletId(), userId)
                .orElseThrow(() -> new AccessDeniedException(
                        "User does not have access to origin this wallet"
                ));

        // Validate access: check if user is linked to this wallet
        WalletUser destinationWallet = repositoryWalletUser
                .findByWalletIdAndUserId(request.getToWalletId(), userId)
                .orElseThrow(() -> new AccessDeniedException(
                        "User does not have access to this wallet"
                ));

        Wallet fromWallet = originWallet.getWallet();
        Wallet toWallet = destinationWallet.getWallet();

        if (fromWallet.getId().equals(toWallet.getId())) {
            throw new IllegalStateException("Cannot transfer to the same wallet");
        }

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        serviceWallet.subtractBalance(fromWallet, amount);
        serviceWallet.addBalance(toWallet, amount);

        Transfer transfer = buildTransfer(fromWallet, toWallet, request);
        repositoryTransfers.save(transfer);

        return mapToResponse(transfer);
    }

    private Transfer buildTransfer(
            Wallet fromWallet,
            Wallet toWallet,
            TransferenceRequest request
    ) {
        Transfer transfer = new Transfer();
        transfer.setFromWalletId(fromWallet);
        transfer.setToWalletId(toWallet);
        transfer.setAmount(request.getAmount());
        transfer.setDescription(request.getDescription());
        transfer.setStatus(TransferStatus.COMPLETED);
        return transfer;
    }

    private TransferenceResponse mapToResponse(Transfer transfer) {
        TransferenceResponse response = new TransferenceResponse();

        response.setId(transfer.getId());

        response.setFromWalletId(transfer.getFromWalletId().getId());
        response.setFromWalletName(transfer.getFromWalletId().getName());

        response.setToWalletId(transfer.getToWalletId().getId());
        response.setToWalletName(transfer.getToWalletId().getName());

        response.setAmount(transfer.getAmount());
        response.setDescription(transfer.getDescription());
        response.setStatus(transfer.getStatus().name());

        return response;
    }

}
