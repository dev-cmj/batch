package com.project.batch.reader;

import com.project.batch.model.Post;
import com.project.batch.service.MockApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParallelMockApiItemReader implements ItemStreamReader<Post> {

    private final MockApiService mockApiService;
    private final ConcurrentLinkedQueue<Post> itemQueue = new ConcurrentLinkedQueue<>();

    private final AtomicInteger currentPage = new AtomicInteger(1);
    private final int pageSize = 10;
    private volatile boolean allDataFetched = false;
    private final int maxPages = 20; // 병렬처리 테스트를 위해 더 많은 데이터

    // 스레드 안전성을 위한 동기화
    private final Object lock = new Object();

    @Override
    public Post read() throws Exception {
        if (itemQueue.isEmpty() && !allDataFetched) {
            synchronized (lock) {
                // double-check locking
                if (itemQueue.isEmpty() && !allDataFetched) {
                    fetchNextBatch();
                }
            }
        }

        Post item = itemQueue.poll();
        if (item != null) {
            log.debug("Reading item: {} on thread: {}", item.getId(), Thread.currentThread().getName());
        }

        return item;
    }

    private void fetchNextBatch() {
        try {
            int page = currentPage.get();
            if (page > maxPages) {
                allDataFetched = true;
                log.info("All data fetched. Total pages processed: {}", page - 1);
                return;
            }

            log.info("Fetching batch from page: {} on thread: {}", page, Thread.currentThread().getName());
            List<Post> posts = mockApiService.fetchPostsWithPagination(page, pageSize);

            if (posts.isEmpty()) {
                allDataFetched = true;
                log.info("No more data available. Total pages processed: {}", page - 1);
                return;
            }

            // 페이지 번호를 원자적으로 증가
            currentPage.incrementAndGet();

            // 각 Post에 어느 페이지에서 왔는지 정보 추가 (디버깅용)
            posts.forEach(post -> {
                post.setBody(post.getBody() + " [Page: " + page + "]");
            });

            itemQueue.addAll(posts);
            log.info("Added {} items to queue from page {}. Queue size: {}, Thread: {}",
                    posts.size(), page, itemQueue.size(), Thread.currentThread().getName());

        } catch (Exception e) {
            log.error("Failed to fetch batch from page {}: {}", currentPage.get(), e.getMessage(), e);
            allDataFetched = true;
        }
    }

    /**
     * 병렬처리를 위한 동기화된 ItemReader 래퍼를 생성합니다.
     */
    public SynchronizedItemStreamReader<Post> createSynchronizedReader() {
        SynchronizedItemStreamReader<Post> reader = new SynchronizedItemStreamReader<>();
        reader.setDelegate(this);
        return reader;
    }

    public void reset() {
        synchronized (lock) {
            itemQueue.clear();
            currentPage.set(1);
            allDataFetched = false;
            log.info("Parallel reader reset completed on thread: {}", Thread.currentThread().getName());
        }
    }

    public int getCurrentQueueSize() {
        return itemQueue.size();
    }

    public int getCurrentPage() {
        return currentPage.get();
    }

    public boolean isAllDataFetched() {
        return allDataFetched;
    }

    @Override
    public void open(org.springframework.batch.item.ExecutionContext executionContext) {
        // ItemStreamReader 인터페이스 구현
    }

    @Override
    public void update(org.springframework.batch.item.ExecutionContext executionContext) {
        // ItemStreamReader 인터페이스 구현
    }

    @Override
    public void close() {
        // ItemStreamReader 인터페이스 구현
        reset();
    }
}