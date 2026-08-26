package bflow.wallet.repository;

import bflow.wallet.entities.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepositoryWallet extends JpaRepository<Wallet, UUID> {
    /**
    * Retrieves a wallet with a pessimistic write lock.
    * Used to avoid concurrent balance modifications.
    *
    * @param id the wallet identifier.
    * @return an optional containing the locked wallet.
    */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdForUpdate(UUID id);

    /**
     * Sums the current balance across the given wallets.
     *
     * @param walletIds the wallet identifiers to include
     * @return the total balance
     */
    @Query(
    "SELECT COALESCE(SUM(w.balance), 0) FROM Wallet w WHERE w.id IN :walletIds"
    )
    BigDecimal sumBalanceByWalletIds(List<UUID> walletIds);

}
