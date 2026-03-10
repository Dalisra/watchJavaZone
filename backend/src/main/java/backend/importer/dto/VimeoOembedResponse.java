package backend.importer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@JsonIgnoreProperties(ignoreUnknown = true)
public record VimeoOembedResponse(
                @JsonProperty("type") String type,
                @JsonProperty("version") String version,
                @JsonProperty("provider_name") String providerName,
                @JsonProperty("provider_url") String providerUrl,
                @JsonProperty("title") String title,
                @JsonProperty("author_name") String authorName,
                @JsonProperty("author_url") String authorUrl,
                @JsonProperty("is_plus") String isPlus,
                @JsonProperty("account_type") String accountType,
                @JsonProperty("html") String html,
                @JsonProperty("width") Integer width,
                @JsonProperty("height") Integer height,
                @JsonProperty("duration") Integer duration,
                @JsonProperty("description") String description,
                @JsonProperty("thumbnail_url") String thumbnailUrl,
                @JsonProperty("thumbnail_width") Integer thumbnailWidth,
                @JsonProperty("thumbnail_height") Integer thumbnailHeight,
                @JsonProperty("thumbnail_url_with_play_button") String thumbnailUrlWithPlayButton,
                @JsonProperty("upload_date") String uploadDate,
                @JsonProperty("video_id") Long videoId,
                @JsonProperty("uri") String uri) {
}
