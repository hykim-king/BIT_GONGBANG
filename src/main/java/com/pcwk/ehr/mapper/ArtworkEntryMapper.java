package com.pcwk.ehr.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.pcwk.ehr.artworkentry.domain.ArtworkEntryVO;
import com.pcwk.ehr.cmn.WorkDiv;

@Mapper
public interface ArtworkEntryMapper extends WorkDiv<ArtworkEntryVO> {

	// 전체 삭제 (테스트 데이터 삭제 전용) 
	public int deleteAll();
}