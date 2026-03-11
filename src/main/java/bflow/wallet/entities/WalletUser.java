package bflow.wallet.entities;

import bflow.auth.entities.User;
import bflow.wallet.enums.WalletRole;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Mapping entity between Wallets and Users.
 */
@Entity
@Table(name = "wallet_users")
@Getter
@Setter
public class WalletUser {

    /** Unique identifier for the relationship record. */
    @Id
    @GeneratedValue
    private UUID id;

    /** The wallet associated with this record. */
    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    /** The user associated with this record. */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The role the user has in this wallet. */
    @Enumerated(EnumType.STRING)
    private WalletRole role; // OWNER, MEMBER
}
