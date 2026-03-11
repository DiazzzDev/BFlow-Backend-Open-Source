package bflow.wallet;

import bflow.wallet.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RepositoryWallet extends JpaRepository<Wallet, UUID> {

}
