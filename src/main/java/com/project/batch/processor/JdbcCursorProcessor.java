package com.project.batch.processor;

import com.project.batch.model.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * JDBC Cursor 방식으로 읽어온 Person 데이터를 처리하는 Processor입니다.
 * 읽어온 데이터에 대한 비즈니스 로직을 적용합니다.
 */
@Slf4j
@Component("jdbcCursorProcessor")
public class JdbcCursorProcessor implements ItemProcessor<Person, Person> {

    @Override
    public Person process(Person item) throws Exception {
        log.debug("Processing person: {}", item);
        
        // 비즈니스 로직 예시: 이름을 대문자로 변환
        String transformedFirstName = item.getFirstName() != null 
            ? item.getFirstName().toUpperCase() 
            : null;
        String transformedLastName = item.getLastName() != null 
            ? item.getLastName().toUpperCase() 
            : null;
        
        Person transformedPerson = new Person(
            item.getId(),
            transformedLastName,
            transformedFirstName
        );
        
        log.debug("Processed person: {}", transformedPerson);
        return transformedPerson;
    }
}