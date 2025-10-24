-- Migrate chat_rooms.subscription_plan_id (VARCHAR) to subscription_plan_ids (JSONB array)
ALTER TABLE IF EXISTS chat_rooms
  ADD COLUMN IF NOT EXISTS subscription_plan_ids JSONB NOT NULL DEFAULT '[]'::jsonb;

-- Populate JSONB array from legacy column if exists
DO $$
BEGIN
  IF EXISTS (
      SELECT 1 FROM information_schema.columns
       WHERE table_name='chat_rooms' AND column_name='subscription_plan_id'
  ) THEN
    UPDATE chat_rooms
       SET subscription_plan_ids = CASE
         WHEN subscription_plan_id IS NULL OR subscription_plan_id = '' THEN '[]'::jsonb
         ELSE to_jsonb(ARRAY[subscription_plan_id])
       END;
    ALTER TABLE chat_rooms DROP COLUMN subscription_plan_id;
  END IF;
END $$;

