package com.project.batch.writer;

import com.project.batch.model.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * Person 데이터를 처리하는 공통 Writer 컴포넌트입니다.
 * 여러 Job에서 공유하여 사용할 수 있습니다.
 */
@Slf4j
@Component("personWriter")
public class PersonWriter implements ItemWriter<Person> {

    @Override
    public void write(Chunk<? extends Person> chunk) throws Exception {
        log.info("Writing {} persons to output", chunk.size());
        
        for (Person person : chunk.getItems()) {
            log.info("Writing person: ID={}, Name={} {}", 
                person.getId(), 
                person.getFirstName(), 
                person.getLastName());
        }
        
        log.info("Successfully wrote {} persons", chunk.size());
    }
}