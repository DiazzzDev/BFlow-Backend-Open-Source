CREATE TABLE wallet_invitations (
    id UUID PRIMARY KEY,

    wallet_id UUID NOT NULL,
    invited_by_user_id UUID NOT NULL,
    invited_user_id UUID,

    invited_email VARCHAR(255) NOT NULL,

    status VARCHAR(20) NOT NULL,

    token VARCHAR(255) NOT NULL,

    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    responded_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_wallet_invitation_wallet
    FOREIGN KEY (wallet_id)
    REFERENCES wallets(id)
    ON DELETE CASCADE,

    CONSTRAINT fk_wallet_invitation_owner
    FOREIGN KEY (invited_by_user_id)
    REFERENCES users(id)
    ON DELETE CASCADE,

    CONSTRAINT fk_wallet_invitation_user
    FOREIGN KEY (invited_user_id)
    REFERENCES users(id)
    ON DELETE SET NULL
);

CREATE UNIQUE INDEX idx_wallet_invitation_token
    ON wallet_invitations(token);

CREATE INDEX idx_wallet_invitation_email
    ON wallet_invitations(invited_email);

CREATE INDEX idx_wallet_invitation_wallet
    ON wallet_invitations(wallet_id);

CREATE UNIQUE INDEX idx_wallet_pending_invitation
    ON wallet_invitations(wallet_id, invited_email)
    WHERE status = 'PENDING';