CREATE UNIQUE INDEX idx_unique_active_subscription_per_user
ON subscriptions (user_id)
WHERE status = 'ACTIVE';