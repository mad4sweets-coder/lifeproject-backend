package com.example.demo.qa;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

// C20 인생 Q&A (쿼리는 resources/mappers/qa-mapper.xml)
@Mapper
public interface QaDao {
	List<QaDTO> selectQaList(int lifeId);	// 이 인생의 질문·답변 (오래된 순)
	int countQa(int lifeId);				// 질문 수 (인생당 제한)
	int insertQa(QaDTO qa);					// 새 질문·답변 저장 (qaId 채워짐)
	QaDTO selectQa(int qaId);				// 저장한 1개 다시 읽기
}
