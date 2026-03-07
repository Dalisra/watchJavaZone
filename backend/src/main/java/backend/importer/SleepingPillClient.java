package backend.importer;

import backend.importer.dto.ConferenceListResponse;
import backend.importer.dto.SessionsResponse;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;

@Client("https://sleepingpill.javazone.no")
public interface SleepingPillClient {

    /** Returns all conferences in chronological order. */
    @Get("/public/allSessions")
    ConferenceListResponse getAllConferences();

    /** Returns all sessions for the given conference slug. */
    @Get("/public/allSessions/{slug}")
    SessionsResponse getSessions(String slug);
}
