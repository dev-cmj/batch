package com.project.batch.reader;

import com.project.batch.model.Post;
import com.project.batch.service.MockApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@StepScope
public class PartitionedMockApiItemReader implements ItemReader<Post> {

    @Value("#{stepExecutionContext[startPage]}")
    private Integer startPage;

    @Value("#{stepExecutionContext[endPage]}")
    private Integer endPage;

    @Value("#{stepExecutionContext[partitionId]}")
    private Integer partitionId;

    // Getter methods for IDE property recognition
    public Integer getStartPage() { return startPage; }
    public Integer getEndPage() { return endPage; }
    public Integer getPartitionId() { return partitionId; }

    @Autowired
    private MockApiService mockApiService;

    private Queue<Post> itemQueue = new LinkedList<>();
    private int currentPage = 0;
    private boolean initialized = false;

    @Override
    public Post read() throws Exception {
        if (!initialized) {
            initialize();
        }

        if (itemQueue.isEmpty()) {
            loadNextPage();
        }

        return itemQueue.poll();
    }

    private void initialize() {
        if (startPage == null || endPage == null || partitionId == null) {
            throw new IllegalStateException("Partition parameters not properly injected: startPage=" + startPage + ", endPage=" + endPage + ", partitionId=" + partitionId);
        }
        this.currentPage = startPage;
        this.initialized = true;
        log.info("Partition {} initialized: pages {}-{}", partitionId, startPage, endPage);
    }

    private void loadNextPage() throws Exception {
        if (currentPage > endPage) {
            return; // 더 이상 로드할 페이지 없음
        }

        log.info("Partition {} loading page: {}", partitionId, currentPage);

        try {
            // CompletableFuture를 사용한 비동기 로딩
            CompletableFuture<List<Post>> future =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return mockApiService.fetchPostsWithPagination(currentPage, 10);
                    } catch (Exception e) {
                        log.error("Failed to load page {} in partition {}: {}",
                                currentPage, partitionId, e.getMessage());
                        return Collections.emptyList();
                    }
                });

            List<Post> posts = future.get(10, TimeUnit.SECONDS);

            if (posts != null && !posts.isEmpty()) {
                // 파티션 정보 추가
                posts.forEach(post -> {
                    if (post != null && post.getBody() != null) {
                        post.setBody(post.getBody() +
                                String.format(" [Partition: %d, Page: %d, Thread: %s]",
                                        partitionId, currentPage, Thread.currentThread().getName()));
                    }
                });

                itemQueue.addAll(posts);
                log.info("Partition {} loaded {} items from page {}",
                        partitionId, posts.size(), currentPage);
            } else {
                log.info("Partition {} loaded 0 items from page {} (no more data)",
                        partitionId, currentPage);
            }
        } catch (Exception e) {
            log.error("Error loading page {} in partition {}: {}", currentPage, partitionId, e.getMessage(), e);
            throw e;
        } finally {
            currentPage++;
        }
    }
}