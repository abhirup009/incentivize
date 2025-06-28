-- Initial schema for Incentivization Platform

CREATE TYPE campaign_type AS ENUM ('SIMPLE', 'QUEST');
CREATE TYPE campaign_status AS ENUM ('DRAFT', 'ACTIVE', 'PAUSED', 'ENDED');
CREATE TYPE incentive_type AS ENUM ('CASHBACK', 'POINTS', 'COUPON');

CREATE TABLE campaign (
    campaign_id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name TEXT NOT NULL,
    type campaign_type NOT NULL,
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    status campaign_status NOT NULL DEFAULT 'DRAFT',
    rule_json JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE incentive (
    id UUID PRIMARY KEY,
    type incentive_type NOT NULL,
    reward_currency VARCHAR(10) NOT NULL,
    reward_amount NUMERIC NOT NULL
);

CREATE TABLE user_aggregation (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    campaign_id UUID NOT NULL REFERENCES campaign(campaign_id),
    action_code TEXT NOT NULL,
    incentive_id UUID REFERENCES incentive(id),
    completed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes to speed lookups
CREATE INDEX idx_user_agg_user_campaign ON user_aggregation (user_id, campaign_id);
