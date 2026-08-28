DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM categories LIMIT 1) THEN

        INSERT INTO categories (id, name, type, system_defined, created_at)
        VALUES
            (gen_random_uuid(), 'TRANSPORTATION', 'EXPENSE', true, NOW()),
            (gen_random_uuid(), 'HEALTHCARE', 'EXPENSE', true, NOW()),
            (gen_random_uuid(), 'INSURANCE', 'EXPENSE', true, NOW()),
            (gen_random_uuid(), 'ENTERTAINMENT', 'EXPENSE', true, NOW()),
            (gen_random_uuid(), 'FOOD', 'EXPENSE', true, NOW()),
            (gen_random_uuid(), 'TRAVEL', 'EXPENSE', true, NOW()),
            (gen_random_uuid(), 'SHOPPING', 'EXPENSE', true, NOW()),
            (gen_random_uuid(), 'SUBSCRIPTIONS', 'EXPENSE', true, NOW()),
            (gen_random_uuid(), 'DEBT_PAYMENT', 'EXPENSE', true, NOW()),
            (gen_random_uuid(), 'LOAN_REPAYMENT', 'EXPENSE', true, NOW()),
            (gen_random_uuid(), 'TAX_PAYMENT', 'EXPENSE', true, NOW()),
            (gen_random_uuid(), 'INVESTMENT_CONTRIBUTION', 'EXPENSE', true, NOW()),
            (gen_random_uuid(), 'EDUCATION', 'EXPENSE', true, NOW()),
            (gen_random_uuid(), 'PET_CARE', 'EXPENSE', true, NOW()),
            (gen_random_uuid(), 'GIFTS_DONATIONS', 'EXPENSE', true, NOW()),
            (gen_random_uuid(), 'OTHER', 'EXPENSE', true, NOW()),
            (gen_random_uuid(), 'SALARY', 'INCOME', true, NOW()),
            (gen_random_uuid(), 'BONUS', 'INCOME', true, NOW()),
            (gen_random_uuid(), 'COMMISSION', 'INCOME', true, NOW()),
            (gen_random_uuid(), 'FREELANCE', 'INCOME', true, NOW()),
            (gen_random_uuid(), 'BUSINESS_PROFIT', 'INCOME', true, NOW()),
            (gen_random_uuid(), 'INVESTMENT_RETURN', 'INCOME', true, NOW()),
            (gen_random_uuid(), 'INTEREST', 'INCOME', true, NOW()),
            (gen_random_uuid(), 'RENTAL_INCOME', 'INCOME', true, NOW()),
            (gen_random_uuid(), 'ROYALTIES', 'INCOME', true, NOW()),
            (gen_random_uuid(), 'SIDE_HUSTLE', 'INCOME', true, NOW()),
            (gen_random_uuid(), 'GIFT', 'INCOME', true, NOW()),
            (gen_random_uuid(), 'REFUND', 'INCOME', true, NOW()),
            (gen_random_uuid(), 'CASHBACK', 'INCOME', true, NOW()),
            (gen_random_uuid(), 'GOVERNMENT_BENEFIT', 'INCOME', true, NOW()),
            (gen_random_uuid(), 'SCHOLARSHIP', 'INCOME', true, NOW()),
            (gen_random_uuid(), 'INSURANCE_PAYOUT', 'INCOME', true, NOW()),
            (gen_random_uuid(), 'LOAN_RECEIVED', 'INCOME', true, NOW()),
            (gen_random_uuid(), 'OTHER', 'INCOME', true, NOW());

END IF;
END $$;