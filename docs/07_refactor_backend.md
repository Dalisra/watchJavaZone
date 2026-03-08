# 07 Refactor Backend Controllers

## Goal
Clean up the backend controllers by extracting HTTP request helper methods into a utility class and moving asynchronous execution logic out of the controllers and into the service layer using Micronaut's `@Async` annotation.

## Proposed Changes
1. **`HttpRequestUtils.java`**: Create a new utility class in a `utils` package with static methods `extractIp(HttpRequest<?> request)` and `extractUserAgent(HttpRequest<?> request)`.
2. **`EventController.java` & `VideoController.java`**: 
   - Remove the private `extractIp` methods.
   - Use `HttpRequestUtils` to get IP and User-Agent.
3. **`VideoController.java`**:
   - Remove the `asyncTrack` helper method and the `Thread.ofVirtual().start()` call. 
   - Call the `VideoTrackingService` methods directly.
4. **`VideoTrackingService.java`**:
   - Add `@Async(TaskExecutors.BLOCKING)` to tracking methods to ensure they run asynchronously and leverage virtual threads configuration.
