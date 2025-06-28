-- Update first 5 campaigns to have a simple ACTION_COUNT rule
WITH to_update AS (
    SELECT id FROM campaign ORDER BY id LIMIT 5
)
UPDATE campaign
SET rules = '[{"type":"ACTION_COUNT","action":"LOGIN","count":1,"windowDays":1}]'::jsonb,
    updated_at = NOW()
WHERE id IN (SELECT id FROM to_update);
