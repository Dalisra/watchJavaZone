package backend.controller;

import backend.importer.VideoImporter;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.scheduling.annotation.ExecuteOn;

import java.util.Map;

@Controller("/api/import")
@ExecuteOn(TaskExecutors.BLOCKING)
public class ImportController {

    private final VideoImporter videoImporter;

    public ImportController(VideoImporter videoImporter) {
        this.videoImporter = videoImporter;
    }

    /**
     * Triggers a full import from SleepingPill, including AI enrichment,
     * search vector rebuild, and view_boost update.
     * This is an expensive operation — run sparingly.
     */
    @Get
    public Map<String, Object> runImport() {
        long start = System.currentTimeMillis();
        Map<String, Integer> summary = videoImporter.importAll();
        long elapsed = System.currentTimeMillis() - start;
        int total = summary.values().stream().mapToInt(i -> i).sum();
        return Map.of(
                "status", "ok",
                "totalVideos", total,
                "elapsedMs", elapsed,
                "byConference", summary);
    }
}
