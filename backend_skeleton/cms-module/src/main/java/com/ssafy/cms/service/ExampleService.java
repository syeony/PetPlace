package com.ssafy.cms.service;

import com.ssafy.cms.constant.DataMap;
import com.ssafy.cms.mybatis.mapper.ExampleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service 예제
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExampleService {

    private final ExampleMapper exampleMapper;

    /**
     * 총 개수
     * @param param
     * @return
     */
    public int countExample(DataMap param) {
        // exampleMapper를 사용하여 총 개수 return
        return 0;
    }

    /**
     * 목록
     * @param param
     * @return
     */
    public List listExample(DataMap param) {
        // exampleMapper를 사용하여 목록 return
        return null;
    }

    /**
     * 등록
     * @param param
     */
    @Transactional
    public void insertExample(DataMap param) {
        // exampleMapper를 사용하여 데이터 저장

    }

    /**
     * 상세 정보
     * @param param
     * @return
     */
    public DataMap detailExample(DataMap param) {
        // exampleMapper를 사용하여 상세 정보 return
        return null;
    }
}
