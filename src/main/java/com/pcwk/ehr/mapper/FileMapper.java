package com.pcwk.ehr.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pcwk.ehr.file.domain.FileVO;

<<<<<<< Updated upstream
@Mapper
public interface FileMapper {

	int doSave(FileVO param);

	FileVO doSelectOne(FileVO param);

	List<FileVO> selectByTarget(FileVO param);

	int countByTarget(FileVO param);

	int clearRepByTarget(FileVO param);

	int updateRep(FileVO param);

	int doDelete(FileVO param);

=======
// attach_file 테이블 MyBatis 매퍼 — SQL 은 fileMapper.xml, 호출은 FileService
@Mapper
public interface FileMapper {

	// INSERT — seq_attach_file 로 file_id 발급, FileService 가 채운 메타 저장
	int doSave(FileVO param);

	// file_id 로 단건 조회 (다운로드·삭제·setRep 전 확인)
	FileVO doSelectOne(FileVO param);

	// target_type + target_id 로 목록 (sort_no 순, 1번=대표가 맨 앞)
	List<FileVO> selectByTarget(FileVO param);

	// 대상별 첨부 개수 (9장 제한·uploadOne sortNo 계산용)
	int countByTarget(FileVO param);

	// 지정 file_id 를 대표로 — sort_no=1, is_rep=Y (본인 member_id 조건)
	int updateRep(FileVO param);

	// sort_no·is_rep 동시 변경 (슬롯 교환·1번 삭제 후 재정렬)
	int updateSortAndRep(FileVO param);

	// target + sort_no 로 1번 슬롯 등 특정 칸 파일 조회 (setRep 교환용)
	FileVO selectBySortNo(FileVO param);

	// sort_no > #{sortNo} 인 행 sort_no -1 (비대표 슬롯 삭제 후 빈 칸 메우기)
	int decrementSortNoAfter(FileVO param);

	// file_id + member_id 로 1건 DELETE (본인 파일만)
	int doDelete(FileVO param);

	// target_type + target_id 로 전건 DELETE (작품·일지 삭제 시 M3 연동)
>>>>>>> Stashed changes
	int deleteByTarget(FileVO param);
}
