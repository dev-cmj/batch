package com.project.batch.processor;

import com.project.batch.model.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * JDBC Paging 방식으로 읽어온 Person 데이터를 처리하는 Processor입니다.
 * 읽어온 데이터에 대한 비즈니스 로직을 적용합니다.
 */
@Slf4j
@Component("jdbcPagingProcessor")
public class JdbcPagingProcessor implements ItemProcessor<Person, Person> {

    @Override
    public Person process(Person item) throws Exception {
        log.debug("Processing person (Paging): {}", item);
        
        // 비즈니스 로직 예시: 이름에 접두사 추가
        String transformedFirstName = item.getFirstName() != null 
            ? "[PAGED] " + item.getFirstName().toLowerCase() 
            : null;
        String transformedLastName = item.getLastName() != null 
            ? "[PAGED] " + item.getLastName().toLowerCase() 
            : null;
        
        Person transformedPerson = new Person(
            item.getId(),
            transformedLastName,
            transformedFirstName
        );
        
        log.debug("Processed person (Paging): {}", transformedPerson);
        return transformedPerson;
    }
}