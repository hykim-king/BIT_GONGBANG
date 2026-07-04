package com.pcwk.ehr.file.service;

import java.io.IOException;
import java.util.ArrayList;
<<<<<<< Updated upstream
=======
import java.util.Comparator;
>>>>>>> Stashed changes
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.pcwk.ehr.cmn.FileManager;
import com.pcwk.ehr.file.domain.FileVO;
import com.pcwk.ehr.mapper.FileMapper;

<<<<<<< Updated upstream
@Service
public class FileService {

=======
// 첨부파일 비즈니스 계층 — 규칙(9장 제한·1번=대표·슬롯 정렬) 적용, DB·디스크 조합은 Mapper·FileManager에 위임
@Service
public class FileService {

	// 대표 사진 = 1번 슬롯 (sort_no=1 이고 is_rep='Y' — 두 값은 항상 같이 움직임)
	private static final int REP_SLOT = 1;

	// 작품·작업일지당 이미지 최대 장수 (DB sort_no 1~9 와 맞춤)
>>>>>>> Stashed changes
	private static final int MAX_FILES_PER_TARGET = 9;

	Logger log = LogManager.getLogger(getClass());

<<<<<<< Updated upstream
	@Autowired
	private FileMapper fileMapper;

=======
	// DB attach_file CRUD (MyBatis)
	@Autowired
	private FileMapper fileMapper;

	// 디스크 저장·삭제·경로 (물리 파일 전담)
>>>>>>> Stashed changes
	@Autowired
	private FileManager fileManager;

	public FileService() {
		log.debug("FileService");
	}

<<<<<<< Updated upstream
	@Transactional
	public List<FileVO> upload(MultipartFile[] files, FileVO param) throws IOException {
		requireUploadParam(param);
		List<FileVO> saved = new ArrayList<>();
		if (files == null) {
			return saved;
		}
=======
	// 다중 업로드 — 파일마다 uploadOne() 호출, 하나도 없으면 예외
	@Transactional
	public List<FileVO> upload(MultipartFile[] files, FileVO param) throws IOException {
		requireUploadParam(param);
		if (files == null) {
			throw new IllegalArgumentException("업로드할 파일이 없습니다.");
		}
		List<FileVO> saved = new ArrayList<>();
>>>>>>> Stashed changes
		for (MultipartFile file : files) {
			if (file == null || file.isEmpty()) {
				continue;
			}
			saved.add(uploadOne(file, param));
		}
		if (saved.isEmpty()) {
			throw new IllegalArgumentException("업로드할 파일이 없습니다.");
		}
		return saved;
	}

<<<<<<< Updated upstream
=======
	// 단일 업로드 — ① 9장 검사 ② 디스크 저장 ③ DB 메타 INSERT
>>>>>>> Stashed changes
	@Transactional
	public FileVO uploadOne(MultipartFile file, FileVO param) throws IOException {
		requireUploadParam(param);

<<<<<<< Updated upstream
=======
		// param: targetType, targetId, memberId (어떤 글·누가 올리는지)
>>>>>>> Stashed changes
		int count = fileMapper.countByTarget(param);
		if (count >= MAX_FILES_PER_TARGET) {
			throw new IllegalArgumentException("이미지는 최대 9장까지 업로드할 수 있습니다.");
		}

<<<<<<< Updated upstream
		FileVO meta = fileManager.save(file, param.getTargetType(), param.getTargetId());
		meta.setMemberId(param.getMemberId());
		meta.setSortNo(count + 1);
		meta.setIsRep(count == 0 ? "Y" : "N");
=======
		// FileManager: UUID 파일명으로 디스크 저장 후 org/save 경로 등 메타 반환
		FileVO meta = fileManager.save(file, param.getTargetType(), param.getTargetId());
		meta.setMemberId(param.getMemberId());
		meta.setSortNo(count + 1); // 기존 N장 → 새 슬롯 N+1
		meta.setIsRep(count == 0 ? "Y" : "N"); // 첫 장이면 1번 슬롯 = 대표
>>>>>>> Stashed changes

		int cnt = fileMapper.doSave(meta);
		if (cnt != 1) {
			throw new IllegalStateException("파일 정보 저장에 실패했습니다.");
		}
		return meta;
	}

<<<<<<< Updated upstream
	@Transactional
	public int setRep(FileVO param) {
=======
	// 대표 지정 — 선택한 사진을 1번 슬롯으로, 기존 1번 사진은 그 자리(sortNo)로 교환
	@Transactional
	public int setRep(FileVO param) {
		// param: fileId, memberId (본인 파일만)
>>>>>>> Stashed changes
		if (param == null || param.getFileId() <= 0 || param.getMemberId() <= 0) {
			return 0;
		}

		FileVO found = fileMapper.doSelectOne(param);
		if (found == null || found.getMemberId() != param.getMemberId()) {
			return 0;
		}
<<<<<<< Updated upstream

		FileVO target = new FileVO();
		target.setTargetType(found.getTargetType());
		target.setTargetId(found.getTargetId());
		fileMapper.clearRepByTarget(target);

		return fileMapper.updateRep(param);
	}

=======
		// 이미 1번 슬롯이면 is_rep만 Y 로 맞춤
		if (found.getSortNo() == REP_SLOT) {
			return fileMapper.updateRep(param);
		}

		FileVO target = toTargetKey(found);
		FileVO holderAt1 = fileMapper.selectBySortNo(slotKey(target, REP_SLOT));
		if (holderAt1 == null) {
			// 1번 슬롯 비어 있으면 선택 파일만 1번·대표로 이동
			FileVO move = new FileVO();
			move.setFileId(found.getFileId());
			move.setSortNo(REP_SLOT);
			move.setIsRep("Y");
			return fileMapper.updateSortAndRep(move);
		}

		// 슬롯 교환: 기존 1번 → 선택 파일의 sortNo, 선택 파일 → 1번·대표
		FileVO moveHolder = new FileVO();
		moveHolder.setFileId(holderAt1.getFileId());
		moveHolder.setSortNo(found.getSortNo());
		moveHolder.setIsRep("N");
		fileMapper.updateSortAndRep(moveHolder);

		FileVO moveFound = new FileVO();
		moveFound.setFileId(found.getFileId());
		moveFound.setSortNo(REP_SLOT);
		moveFound.setIsRep("Y");
		return fileMapper.updateSortAndRep(moveFound);
	}

	// 파일 1장 삭제 — 디스크+DB 제거 후 슬롯 번호 정리
>>>>>>> Stashed changes
	@Transactional
	public int remove(FileVO param) throws IOException {
		if (param == null || param.getFileId() <= 0 || param.getMemberId() <= 0) {
			return 0;
		}

		FileVO found = fileMapper.doSelectOne(param);
		if (found == null || found.getMemberId() != param.getMemberId()) {
			return 0;
		}

<<<<<<< Updated upstream
		fileManager.deletePhysical(found);
		return fileMapper.doDelete(param);
	}

=======
		boolean wasRepSlot = found.getSortNo() == REP_SLOT;
		int deletedSortNo = found.getSortNo();
		FileVO target = toTargetKey(found);

		fileManager.deletePhysical(found); // 디스크
		int cnt = fileMapper.doDelete(param); // DB
		if (cnt != 1) {
			return 0;
		}
		if (wasRepSlot) {
			// 1번(대표) 삭제 → 남은 사진 1..n 재정렬, 새 1번이 대표
			renormalizeSlotsAfterRepDelete(target);
		} else {
			// 그 외 슬롯 삭제 → 뒤 번호만 -1 (1번 대표는 유지)
			fileMapper.decrementSortNoAfter(deletedSortNoKey(target, deletedSortNo));
		}
		return cnt;
	}

	// 다운로드용 — fileId 로 DB 메타 조회 (실제 스트림은 Controller + FileManager 경로)
>>>>>>> Stashed changes
	public FileVO getFile(FileVO param) {
		if (param == null || param.getFileId() <= 0) {
			return null;
		}
		return fileMapper.doSelectOne(param);
	}

<<<<<<< Updated upstream
=======
	// 작품·작업일지별 첨부 목록 (sort_no 순, 1번=대표가 맨 앞)
>>>>>>> Stashed changes
	public List<FileVO> selectByTarget(FileVO param) {
		if (param == null || param.getTargetType() == null || param.getTargetId() <= 0) {
			return new ArrayList<>();
		}
		return fileMapper.selectByTarget(param);
	}

<<<<<<< Updated upstream
=======
	// 글 삭제 시 연동 — 해당 target 의 파일 전부 디스크·DB 일괄 삭제 (M3에서 호출 예정)
>>>>>>> Stashed changes
	@Transactional
	public int deleteByTarget(FileVO param) throws IOException {
		if (param == null || param.getTargetType() == null || param.getTargetId() <= 0) {
			return 0;
		}

		List<FileVO> files = fileMapper.selectByTarget(param);
		for (FileVO file : files) {
			fileManager.deletePhysical(file);
		}
		return fileMapper.deleteByTarget(param);
	}

<<<<<<< Updated upstream
=======
	// 업로드 전 param 검증 — 대상(ARTWORK/ARTWORK_ENTRY + id) + memberId 필수
>>>>>>> Stashed changes
	private void requireUploadParam(FileVO param) {
		if (param == null || param.getTargetType() == null || param.getTargetId() <= 0) {
			throw new IllegalArgumentException("대상 정보가 올바르지 않습니다.");
		}
		if (param.getMemberId() <= 0) {
			throw new IllegalArgumentException("회원 정보가 올바르지 않습니다.");
		}
	}
<<<<<<< Updated upstream
=======

	// Mapper 조회용 — targetType + targetId 만 담은 키 VO
	private FileVO toTargetKey(FileVO found) {
		FileVO target = new FileVO();
		target.setTargetType(found.getTargetType());
		target.setTargetId(found.getTargetId());
		return target;
	}

	// 특정 슬롯(sortNo) 파일 조회용 키
	private FileVO slotKey(FileVO target, int sortNo) {
		FileVO key = toTargetKey(target);
		key.setSortNo(sortNo);
		return key;
	}

	// decrementSortNoAfter SQL — sort_no > 삭제된 번호 인 행들 -1
	private FileVO deletedSortNoKey(FileVO target, int deletedSortNo) {
		FileVO key = toTargetKey(target);
		key.setSortNo(deletedSortNo);
		return key;
	}

	// 1번(대표) 삭제 후: 남은 목록을 sortNo 오름차순 → 1,2,3… 재부여, index 0 이 새 대표
	private void renormalizeSlotsAfterRepDelete(FileVO target) {
		List<FileVO> remaining = new ArrayList<>(fileMapper.selectByTarget(target));
		if (remaining.isEmpty()) {
			return;
		}
		remaining.sort(Comparator.comparingInt(FileVO::getSortNo));

		for (int i = 0; i < remaining.size(); i++) {
			FileVO update = new FileVO();
			update.setFileId(remaining.get(i).getFileId());
			update.setSortNo(i + 1);
			update.setIsRep(i == 0 ? "Y" : "N");
			fileMapper.updateSortAndRep(update);
		}
	}
>>>>>>> Stashed changes
}
