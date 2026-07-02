-- Demo data for the dev (H2) profile only.
-- Lets you call the API immediately without creating a merchant first.

MERGE INTO merchants (id, merchant_code, business_name, email, kyc_verified, active, created_at, updated_at, version)
KEY (id)
VALUES (
    'demo-merchant-001',
    'DEMO001',
    'Deepak Singh Rajput Demo Store',
    'demo@example.com',
    TRUE,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
);
