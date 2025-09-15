package com.project.batch.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public abstract class BaseProcessor<I, O> implements ItemProcessor<I, O> {
    
    @Override
    public O process(I item) throws Exception {
        try {
            return doProcess(item);
        } catch (Exception e) {
            log.error("Error processing item: {}", item, e);
            throw e;
        }
    }
    
    protected abstract O doProcess(I item) throws Exception;
}