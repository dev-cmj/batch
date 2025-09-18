package com.project.batch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    private Long id;
    private Long userId;
    private String title;
    private String body;

    public String getProcessedTitle() {
        return title != null ? title.toUpperCase() : "";
    }

    public String getShortBody() {
        return body != null && body.length() > 50 ? body.substring(0, 50) + "..." : body;
    }
}