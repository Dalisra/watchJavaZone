ALTER TABLE video_events      ADD COLUMN ip_address TEXT, ADD COLUMN user_agent TEXT;
ALTER TABLE search_logs       ADD COLUMN ip_address TEXT, ADD COLUMN user_agent TEXT;
ALTER TABLE video_impressions ADD COLUMN ip_address TEXT, ADD COLUMN user_agent TEXT;
