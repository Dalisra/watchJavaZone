# 04 Remove IDs from UI

## Goal
Remove the display of internal video IDs or Conference IDs from the frontend UI, both in lists (e.g., VideoCards) and in the detail view. The user will only see the views, dates, formats, rooms, lengths, etc.

## Proposed Changes
1. **Frontend List View**: Investigate `VideoCard.tsx` (and potentially other list components) to find where `video.id` or `conferenceId` is rendered. Remove this rendering logic.
2. **Frontend Detail View**: Investigate the video detail page (likely `src/routes/video/$id.tsx` or similar) to find where the said ids are rendered. Remove this rendering logic.
3. Test locally to ensure IDs are no longer displayed but navigation still works (IDs are still needed for Routing to the detail page, but should not be visually displayed to the user).

## Implementations Executed
- Removed the rendering of `video.conferenceId.split('_')[1]` (which looked like "2019" for Javazone cases) from the `VideoCard.tsx` component. This was right next to the video views counter.
- Removed the rendering of `video.conferenceId.split('_')[1] || video.conferenceId` from the `video.$id.tsx` component.
- The `viewBoost` count remains and will be explicitly shown to the users instead.
