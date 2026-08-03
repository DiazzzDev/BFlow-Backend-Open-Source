package bflow.wallet.entities;

import bflow.auth.entities.User;
import bflow.wallet.enums.WalletInvitationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Invitation sent to collaborate on a shared wallet.
 */
@Entity
@Table(
        name = "wallet_invitations",
        indexes = {
                @Index(
                        name = "idx_wallet_invitation_token",
                        columnList = "token",
                        unique = true
                ),
                @Index(
                        name = "idx_wallet_invitation_wallet_status",
                        columnList = "wallet_id,status"
                ),
                @Index(
                        name = "idx_wallet_invitation_email_status",
                        columnList = "invited_email,status"
                )
        }
)
@Getter
@Setter
public class WalletInvitation {

    /**
     * Unique identifier of the wallet invitation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Wallet where the user will collaborate.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Wallet wallet;

    /**
     * Wallet owner who created the invitation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User invitedByUser;

    /**
     * Destination email.
     * The account may not exist yet.
     */
    @Column(nullable = false)
    private String invitedEmail;

    /**
     * Existing user associated with invitedEmail.
     * Nullable because invitations can be sent before registration.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User invitedUser;

    /**
     * Invitation status.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletInvitationStatus status;

    /**
     * Public token sent through email.
     */
    @Column(nullable = false, unique = true, updatable = false)
    private String token;

    /**
     * Invitation expiration date.
     */
    @Column(nullable = false)
    private Instant expiresAt;

    /**
     * Invitation creation date.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Date when invitation was accepted/rejected/canceled.
     */
    private Instant respondedAt;

    /**
     * Determines whether the invitation has expired.
     *
     * @return {@code true}
     * if the current time is after the expiration date;
     * otherwise {@code false}
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
