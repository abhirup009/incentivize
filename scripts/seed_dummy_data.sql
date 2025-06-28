-- Dummy data seed script for Incentivize DB
-- Inserts 20 campaign rows for readability/testing

BEGIN;

-- Reuse a single tenant for simplicity
\set tenant_id '11111111-1111-1111-1111-111111111111'

-- Insert 20 campaigns
INSERT INTO campaign (campaign_id, tenant_id, name, type, rule_json, start_at, end_at)
VALUES
  ('00000000-0000-0000-0000-000000000001', :'tenant_id', 'Login Bonus', 'SIMPLE', '{}'::jsonb, now(), now() + interval '30 days'),
  ('00000000-0000-0000-0000-000000000002', :'tenant_id', 'Purchase Rewards', 'SIMPLE', '{}'::jsonb, now(), now() + interval '30 days'),
  ('00000000-0000-0000-0000-000000000003', :'tenant_id', 'Referral Quest', 'QUEST', '{}'::jsonb, now(), now() + interval '60 days'),
  ('00000000-0000-0000-0000-000000000004', :'tenant_id', 'Summer Sale', 'SIMPLE', '{}'::jsonb, now(), now() + interval '20 days'),
  ('00000000-0000-0000-0000-000000000005', :'tenant_id', 'Winter Cashback', 'SIMPLE', '{}'::jsonb, now(), now() + interval '40 days'),
  ('00000000-0000-0000-0000-000000000006', :'tenant_id', 'Action Marathon', 'QUEST', '{}'::jsonb, now(), now() + interval '15 days'),
  ('00000000-0000-0000-0000-000000000007', :'tenant_id', 'VIP Exclusive', 'SIMPLE', '{}'::jsonb, now(), now() + interval '90 days'),
  ('00000000-0000-0000-0000-000000000008', :'tenant_id', 'Flash Deal', 'SIMPLE', '{}'::jsonb, now(), now() + interval '5 days'),
  ('00000000-0000-0000-0000-000000000009', :'tenant_id', 'Signup Streak', 'QUEST', '{}'::jsonb, now(), now() + interval '45 days'),
  ('00000000-0000-0000-0000-000000000010', :'tenant_id', 'Festival Fiesta', 'SIMPLE', '{}'::jsonb, now(), now() + interval '25 days'),
  ('00000000-0000-0000-0000-000000000011', :'tenant_id', 'Daily Check-in', 'SIMPLE', '{}'::jsonb, now(), now() + interval '30 days'),
  ('00000000-0000-0000-0000-000000000012', :'tenant_id', 'Mega Milestone', 'QUEST', '{}'::jsonb, now(), now() + interval '60 days'),
  ('00000000-0000-0000-0000-000000000013', :'tenant_id', 'Weekend Warrior', 'SIMPLE', '{}'::jsonb, now(), now() + interval '10 days'),
  ('00000000-0000-0000-0000-000000000014', :'tenant_id', 'Holiday Hunt', 'QUEST', '{}'::jsonb, now(), now() + interval '50 days'),
  ('00000000-0000-0000-0000-000000000015', :'tenant_id', 'Loyalty Ladder', 'SIMPLE', '{}'::jsonb, now(), now() + interval '70 days'),
  ('00000000-0000-0000-0000-000000000016', :'tenant_id', 'Early Bird', 'SIMPLE', '{}'::jsonb, now(), now() + interval '7 days'),
  ('00000000-0000-0000-0000-000000000017', :'tenant_id', 'Night Owl', 'SIMPLE', '{}'::jsonb, now(), now() + interval '7 days'),
  ('00000000-0000-0000-0000-000000000018', :'tenant_id', 'Action Combo', 'QUEST', '{}'::jsonb, now(), now() + interval '20 days'),
  ('00000000-0000-0000-0000-000000000019', :'tenant_id', 'Super Saver', 'SIMPLE', '{}'::jsonb, now(), now() + interval '35 days'),
  ('00000000-0000-0000-0000-000000000020', :'tenant_id', 'Anniversary Bash', 'SIMPLE', '{}'::jsonb, now(), now() + interval '30 days');

COMMIT;
