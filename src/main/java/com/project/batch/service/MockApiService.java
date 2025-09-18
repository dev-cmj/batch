package com.project.batch.service;

import com.project.batch.model.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockApiService {

    private final RestTemplate restTemplate;
    private static final String JSONPLACEHOLDER_URL = "https://jsonplaceholder.typicode.com/posts";

    public List<Post> fetchPostsWithPagination(int page, int pageSize) {
        try {
            String url = String.format("%s?_page=%d&_limit=%d", JSONPLACEHOLDER_URL, page, pageSize);
            log.debug("Fetching posts from: {}", url);

            ResponseEntity<List<Post>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Post>>() {}
            );

            List<Post> posts = response.getBody();
            if (posts == null) {
                posts = new ArrayList<>();
            }

            log.info("Fetched {} posts from page {}", posts.size(), page);
            return posts;

        } catch (Exception e) {
            log.error("Failed to fetch posts from page {}: {}", page, e.getMessage(), e);
            return createMockPosts(page, pageSize);
        }
    }

    public List<Post> fetchAllPosts() {
        try {
            log.debug("Fetching all posts from: {}", JSONPLACEHOLDER_URL);

            ResponseEntity<List<Post>> response = restTemplate.exchange(
                JSONPLACEHOLDER_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Post>>() {}
            );

            List<Post> posts = response.getBody();
            if (posts == null) {
                posts = new ArrayList<>();
            }

            log.info("Fetched {} total posts", posts.size());
            return posts;

        } catch (Exception e) {
            log.error("Failed to fetch all posts: {}", e.getMessage(), e);
            return createMockPosts(1, 100);
        }
    }

    private List<Post> createMockPosts(int page, int pageSize) {
        log.warn("Creating mock posts due to API failure - Page: {}, Size: {}", page, pageSize);
        List<Post> mockPosts = new ArrayList<>();

        int startId = (page - 1) * pageSize + 1;
        for (int i = 0; i < pageSize; i++) {
            long postId = startId + i;
            mockPosts.add(Post.builder()
                .id(postId)
                .userId((postId % 10) + 1)
                .title("Mock Post Title " + postId)
                .body("This is a mock post body for testing purposes. Post ID: " + postId +
                      ". Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
                .build());
        }

        return mockPosts;
    }
}