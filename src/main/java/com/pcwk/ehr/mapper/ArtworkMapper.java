package com.pcwk.ehr.mapper;

import java.util.List;

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
    public Integer selectCount(ArtworkVO param);
    
    //메인 화면 : 완성or공개작업 게시글 N건 조회
    public List<ArtworkVO> selectMain(ArtworkVO param);
    
    //추천 게시물 : 좋와요*likeWeight+조회수 N건 조회
    public List<ArtworkVO> selectRecommend(ArtworkVO param);
    
    //인기 게시글 : 특정 기간내 완성작 N건 조회
    public List<ArtworkVO> selectPopular(ArtworkVO param);
    
    //통합 검색 : 1제목 2내용 3작성자 4카테고리 그외 전체
    public List<ArtworkVO> search(ArtworkVO param);

    //통합 검색 건수 : search 페이징 totalCnt 산출(searchDiv/searchWord 분기, member+category JOIN)
    public Integer searchCount(ArtworkVO param);

    // 아트워크 테이블 삭제 (테스트 데이터 삭제)
    public int deleteAll();
}
	 	
	

