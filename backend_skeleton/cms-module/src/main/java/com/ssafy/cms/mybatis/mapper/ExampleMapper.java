package com.ssafy.cms.mybatis.mapper;

import com.ssafy.cms.constant.DataMap;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface ExampleMapper {

    /**
     * 메서드 생성
     * 단, 메서드 명은 example_sql.xml 파일의 sql id 와 동일해야함.
     */
    int countExample(DataMap param);
}
