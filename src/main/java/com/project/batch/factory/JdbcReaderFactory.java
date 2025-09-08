package com.project.batch.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * JDBC 기반 ItemReader(Paging, Cursor)를 생성하는 팩토리 클래스입니다.
 * 제네릭을 사용하여 다양한 데이터 타입에 재사용할 수 있는 Reader를 생성합니다.
 */
@Component
@RequiredArgsConstructor
public class JdbcReaderFactory {

    private final DataSource dataSource;

    public <T> JdbcPagingItemReader<T> createPagingReader(String name, int pageSize, Class<T> type, String selectClause, String fromClause, String sortKey) throws Exception {
        return new JdbcPagingItemReaderBuilder<T>()
                .name(name)
                .dataSource(this.dataSource)
                .pageSize(pageSize)
                .fetchSize(pageSize)
                .queryProvider(createPagingQueryProvider(selectClause, fromClause, sortKey))
                .rowMapper(new BeanPropertyRowMapper<>(type))
                .build();
    }

    public <T> JdbcCursorItemReader<T> createCursorReader(String name, Class<T> type, String sql) {
        return new JdbcCursorItemReaderBuilder<T>()
                .name(name)
                .dataSource(this.dataSource)
                .sql(sql)
                .rowMapper(new BeanPropertyRowMapper<>(type))
                .build();
    }

    private PagingQueryProvider createPagingQueryProvider(String selectClause, String fromClause, String sortKey) throws Exception {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(this.dataSource);
        queryProvider.setSelectClause(selectClause);
        queryProvider.setFromClause(fromClause);

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put(sortKey, Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);

        return queryProvider.getObject();
    }
}
