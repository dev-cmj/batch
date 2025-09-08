package com.project.batch.mapper;

import com.project.batch.model.Person;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * Person 데이터에 접근하는 MyBatis Mapper 인터페이스입니다.
 * 이 인터페이스의 구현은 PersonMapper.xml 파일에 정의됩니다.
 */
@Mapper
public interface PersonMapper {

    /**
     * MyBatisPagingItemReader가 사용할 페이징 쿼리입니다.
     * Spring Batch가 페이징 처리를 위해 필요한 파라미터(_page, _pagesize, _skiprows)를 자동으로 넘겨줍니다.
     *
     * @param params 페이징 파라미터
     * @return 지정된 페이지에 해당하는 Person 리스트
     */
    List<Person> findByPaging(Map<String, Object> params);
}
