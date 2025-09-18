package com.project.batch.reader;

import com.project.batch.model.Post;
import com.project.batch.service.MockApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class MockApiItemReader implements ItemReader<Post> {

    private final MockApiService mockApiService;
    private final ConcurrentLinkedQueue<Post> itemQueue = new ConcurrentLinkedQueue<>();

    private int currentPage = 1;
    private final int pageSize = 10;
    private boolean allDataFetched = false;
    private final int maxPages = 10; // 대용량 테스트를 위해 제한

    @Override
    public Post read() throws Exception {
        if (itemQueue.isEmpty() && !allDataFetched) {
            fetchNextBatch();
        }

        Post item = itemQueue.poll();
        if (item != null) {
            log.debug("Reading item: {}", item.getId());
        }

        return item;
    }

    private void fetchNextBatch() {
        try {
            log.info("Fetching batch from page: {}", currentPage);
            List<Post> posts = mockApiService.fetchPostsWithPagination(currentPage, pageSize);

            if (posts.isEmpty() || currentPage >= maxPages) {
                allDataFetched = true;
                log.info("All data fetched. Total pages processed: {}", currentPage - 1);
                return;
            }

            itemQueue.addAll(posts);
            currentPage++;

            log.info("Added {} items to queue. Queue size: {}", posts.size(), itemQueue.size());

        } catch (Exception e) {
            log.error("Failed to fetch batch from page {}: {}", currentPage, e.getMessage(), e);
            allDataFetched = true;
        }
    }

    public void reset() {
        itemQueue.clear();
        currentPage = 1;
        allDataFetched = false;
        log.info("Reader reset completed");
    }
}