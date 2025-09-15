package com.project.batch.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@Slf4j
public abstract class BaseWriter<T> implements ItemWriter<T> {

    public void write(List<? extends T> items) throws Exception {
        try {
            log.debug("Writing {} items", items.size());
            doWrite(items);
            log.debug("Successfully wrote {} items", items.size());
        } catch (Exception e) {
            log.error("Error writing {} items", items.size(), e);
            throw e;
        }
    }
    
    protected abstract void doWrite(List<? extends T> items) throws Exception;
}