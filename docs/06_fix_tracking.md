# 06 Fix View Tracking

## Goal
Improve view count tracking by preventing users from artificially inflating views by repeatedly refreshing the page. Instead of a dummy frontend play event, log a single view when the user first opens the detail page, but do not record it again if a view was recorded for that user within the timespan of the video length (or 2 hours if unknown).

## Proposed Plan

1. **Frontend `video.$id.tsx`**:
    - Remove the automatic firing of `api.recordPlayEvent` on iframe load. This removes the legacy pinging that the user didn't even initiate.

2. **Backend Repository `<VideoImpressionRepository>`**:
    - Add a `boolean existsByVideoIdAndEndpointAndIpAddressAndUserAgentAndCreatedAtGreaterThan` query block.

3. **Backend Service `<VideoTrackingService>`**:
    - Build `trackDetailImpression(String videoId, Integer lengthMinutes, String ipAddress, String userAgent)`.
    - Fetch and test against `exists...`.

4. **Backend Controller `<VideoController>`**:
    - Send the impression tracking to the new method using a virtual thread to prevent blocking.

## Validated Results
- Removed the old `useEffect` and `useRef` iframe onLoad event handlers from `video.$id.tsx`.
- Updated backend repository with a new query to check if a specific IP + UserAgent recently watched that exact video ID within `Instant.now().minus(duration)`.
- Replaced the detail tracker in `VideoController` with a call to the new `trackDetailImpression` method.
- Compiled backend successfully locally to ensure Spring Boot Data JPA syntax errors don't exist in the query syntax. Everything ran successfully.
