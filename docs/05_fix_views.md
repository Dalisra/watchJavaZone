# 05 Fix View Counts

## Goal
Investigate and fix the issue where clicking/watching a video does not update the number of views shown on the frontend.

## Investigation Results
- The frontend UI `VideoCard.tsx` and `video.$id.tsx` currently map the view metric to `video.viewBoost || 0`.
- In the backend `VideoResponse` object, `viewBoost` is a statically assigned ranking modifier. The actual dynamically tracked metric for views is `long viewCount`.
- The frontend `api.ts` `VideoResponse` TypeScript interface is missing `viewCount`.

## Proposed Changes
1. **Frontend `api.ts`**: Add `viewCount: number;` to the `VideoResponse` interface.
2. **Frontend `VideoCard.tsx`**: Change `<span>{video.viewBoost || 0} views</span>` to `<span>{video.viewCount || 0} views</span>`.
3. **Frontend `video.$id.tsx`**: Change `{video.viewBoost || 0} views` to `{video.viewCount || 0} views`.

## Verification Plan
1. Check that compiling works.
2. Open a video detail page.
3. Keep track of the view count; it should increment each time the page is uniquely tracked (the backend creates a new `VideoImpression` with endpoint `detail`. Wait: the backend extracts the IP address within `asyncTrack("detail", ip, ua)`. The views will accumulate in `video_impressions`!).

## Implementations Executed
- Upgraded the TypeScript `VideoResponse` interface inside `api.ts` to include the `viewCount` metric properly.
- Switched `VideoCard.tsx` from statically mapping `viewBoost` to the dynamically generated `viewCount`.
- Did the exact same implementation in the `video.$id.tsx` details page component.
- The metrics will now update organically based on backend view tracking!
