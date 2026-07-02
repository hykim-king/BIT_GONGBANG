package com.pcwk.ehr.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.cmn.WorkDiv;

/**
 * ArtworkMapper - 아트워크(게시판) DAO
 * 매핑 XML : artworkMapper.xml (namespace = 이 인터페이스 경로명)
 *
 * WorkDiv<ArtworkVO> 상속 기본 CRUD 5종 :
 *   int          doSave(ArtworkVO vo)        - 등록
 *   int          doUpdate(ArtworkVO vo)      - 수정
 *   int          doDelete(ArtworkVO vo)      - 삭제
 *   ArtworkVO    doSelectOne(ArtworkVO vo)   - 상세 1건
 *   List<ArtworkVO> doRetrieve(ArtworkVO vo) - 목록 (is_status 분기)
 *
 */
@Mapper
public interface ArtworkMapper extends WorkDiv<ArtworkVO> {
 
    // 조회수 1 증가 
    public int updateViewCount(ArtworkVO param);
 
    // 완성 전환 : is_status='Y', comp_dt=SYSDATE 
    public int updateStatus(ArtworkVO param);
     
    // 목록 건수 카운트
    public int selectCount(ArtworkVO param);
    
    // 아트워크 테이블 삭제
    public int deleteAll();
}
	 	
	

