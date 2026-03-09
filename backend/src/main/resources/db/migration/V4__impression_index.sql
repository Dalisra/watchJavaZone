-- Efficient counting of detail-view impressions per video
CREATE INDEX video_impressions_video_endpoint_idx ON video_impressions (video_id, endpoint);
