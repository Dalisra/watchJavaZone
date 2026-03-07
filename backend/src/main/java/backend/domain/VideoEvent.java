package backend.domain;

import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.time.Instant;
import java.util.UUID;

@MappedEntity("video_events")
public class VideoEvent {

    @Id
    @AutoPopulated
    private UUID id;

    private String videoId;
    private String userUuid;
    private String eventType;
    private Integer playbackPositionSecs;
    private String ipAddress;
    private String userAgent;
    private Instant createdAt;

    public VideoEvent() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Integer getPlaybackPositionSecs() {
        return playbackPositionSecs;
    }

    public void setPlaybackPositionSecs(Integer playbackPositionSecs) {
        this.playbackPositionSecs = playbackPositionSecs;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
