package com.pcwk.ehr.file.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.pcwk.ehr.cmn.FileManager;
import com.pcwk.ehr.file.domain.FileVO;
import com.pcwk.ehr.mapper.FileMapper;

// 첨부파일 비즈니스 계층 — 규칙(9장 제한·1번=대표·슬롯 정렬) 적용, DB·디스크 조합은 Mapper·FileManager에 위임
@Service
public class FileService {

	// 대표 사진 = 1번 슬롯 (sort_no=1 이고 is_rep='Y' — 두 값은 항상 같이 움직임)
	private static final int REP_SLOT = 1;

	// 작품·작업일지당 이미지 최대 장수 (DB sort_no 1~9 와 맞춤)
	private static final int MAX_FILES_PER_TARGET = 9;

	Logger log = LogManager.getLogger(getClass());

	@Autowired
	private FileMapper fileMapper;

	@Autowired
	private FileManager fileManager;

	public FileService() {
		log.debug("FileService");
	}

	// 다중 업로드 — 사전 9장 검사, 실패 시 디스크 고아 파일 정리
	@Transactional(rollbackFor = Exception.class)
	public List<FileVO> upload(MultipartFile[] files, FileVO param) throws IOException {
		requireUploadParam(param);
		int incoming = countNonEmptyFiles(files);
		if (incoming == 0) {
			throw new IllegalArgumentException("업로드할 파일이 없습니다.");
		}

		int existing = fileMapper.countByTarget(param);
		requireUploadCapacity(existing, incoming);

		List<FileVO> diskSaved = new ArrayList<>();
		try {
			List<FileVO> saved = new ArrayList<>();
			int batchIndex = 0;
			for (MultipartFile file : files) {
				if (file == null || file.isEmpty()) {
					continue;
				}
				saved.add(persistUpload(file, param, existing, batchIndex, diskSaved));
				batchIndex++;
			}
			return saved;
		} catch (RuntimeException | IOException e) {
			cleanupPhysical(diskSaved);
			throw e;
		}
	}

	// 단일 업로드 — ① 9장 검사 ② 디스크 저장 ③ DB 메타 INSERT (실패 시 디스크 롤백)
	@Transactional(rollbackFor = Exception.class)
	public FileVO uploadOne(MultipartFile file, FileVO param) throws IOException {
		requireUploadParam(param);

		int existing = fileMapper.countByTarget(param);
		requireUploadCapacity(existing, 1);

		List<FileVO> diskSaved = new ArrayList<>();
		try {
			return persistUpload(file, param, existing, 0, diskSaved);
		} catch (RuntimeException | IOException e) {
			cleanupPhysical(diskSaved);
			throw e;
		}
	}

	@Transactional
	public int setRep(FileVO param) {
		if (param == null || param.getFileId() <= 0 || param.getMemberId() <= 0) {
			return 0;
		}

		FileVO found = fileMapper.doSelectOne(param);
		if (found == null || found.getMemberId() != param.getMemberId()) {
			return 0;
		}
		if (found.getSortNo() == REP_SLOT) {
			return fileMapper.updateRep(param);
		}

		FileVO target = toTargetKey(found);
		FileVO holderAt1 = fileMapper.selectBySortNo(slotKey(target, REP_SLOT));
		if (holderAt1 == null) {
			FileVO move = new FileVO();
			move.setFileId(found.getFileId());
			move.setSortNo(REP_SLOT);
			move.setIsRep("Y");
			return fileMapper.updateSortAndRep(move);
		}

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

	/**
	 * 드래그앤드롭 순서 변경 — 받은 순서대로 sort_no 를 1..N 으로 다시 매기고,
	 * 맨 앞(1번 슬롯)을 대표(is_rep='Y')로 지정한다.
	 *
	 * 클라이언트가 보낸 fileIds 를 그대로 믿지 않는다.
	 * 대상에 실제로 달린 파일 전체를 DB 에서 다시 읽어와서
	 * (1) 전부 본인 파일인지 (2) 보내온 집합이 DB 집합과 정확히 일치하는지(누락·중복·외부 id 없음)
	 * 를 확인한 뒤에만 반영한다.
	 *
	 * SQL 은 새로 만들지 않고 기존 updateSortAndRep 를 반복 호출한다
	 * (renormalizeSlotsAfterRepDelete 와 같은 방식).
	 */
	@Transactional
	public int reorder(FileVO param, int[] fileIds) {
		if (param == null || param.getTargetType() == null || param.getTargetId() <= 0
				|| param.getMemberId() <= 0 || fileIds == null || fileIds.length == 0) {
			return 0;
		}

		List<FileVO> current = fileMapper.selectByTarget(toTargetKey(param));
		if (current.size() != fileIds.length) {
			return 0;
		}

		Set<Integer> owned = new LinkedHashSet<>();
		for (FileVO file : current) {
			if (file.getMemberId() != param.getMemberId()) {
				return 0;
			}
			owned.add(file.getFileId());
		}

		Set<Integer> requested = new LinkedHashSet<>();
		for (int fileId : fileIds) {
			requested.add(fileId);
		}
		if (!owned.equals(requested)) {
			return 0;
		}

		for (int i = 0; i < fileIds.length; i++) {
			FileVO update = new FileVO();
			update.setFileId(fileIds[i]);
			update.setSortNo(i + 1);
			update.setIsRep(i == 0 ? "Y" : "N");
			fileMapper.updateSortAndRep(update);
		}
		return fileIds.length;
	}

	// 파일 1장 삭제 — DB·슬롯 정리 후 디스크 삭제 (IOException 시 DB 롤백)
	@Transactional(rollbackFor = Exception.class)
	public int remove(FileVO param) throws IOException {
		if (param == null || param.getFileId() <= 0 || param.getMemberId() <= 0) {
			return 0;
		}

		FileVO found = fileMapper.doSelectOne(param);
		if (found == null || found.getMemberId() != param.getMemberId()) {
			return 0;
		}

		boolean wasRepSlot = found.getSortNo() == REP_SLOT;
		int deletedSortNo = found.getSortNo();
		FileVO target = toTargetKey(found);

		int cnt = fileMapper.doDelete(param);
		if (cnt != 1) {
			return 0;
		}
		if (wasRepSlot) {
			renormalizeSlotsAfterRepDelete(target);
		} else {
			fileMapper.decrementSortNoAfter(deletedSortNoKey(target, deletedSortNo));
		}
		fileManager.deletePhysical(found);
		return cnt;
	}

	public FileVO getFile(FileVO param) {
		if (param == null || param.getFileId() <= 0) {
			return null;
		}
		return fileMapper.doSelectOne(param);
	}

	public List<FileVO> selectByTarget(FileVO param) {
		if (param == null || param.getTargetType() == null || param.getTargetId() <= 0) {
			return new ArrayList<>();
		}
		return fileMapper.selectByTarget(param);
	}

	// 글 삭제 시 연동 — DB 삭제 후 디스크 정리 (IOException 시 DB 롤백)
	@Transactional(rollbackFor = Exception.class)
	public int deleteByTarget(FileVO param) throws IOException {
		if (param == null || param.getTargetType() == null || param.getTargetId() <= 0) {
			return 0;
		}

		List<FileVO> files = fileMapper.selectByTarget(param);
		if (files.isEmpty()) {
			return 0;
		}
		int cnt = fileMapper.deleteByTarget(param);
		for (FileVO file : files) {
			fileManager.deletePhysical(file);
		}
		return cnt;
	}

	private FileVO persistUpload(MultipartFile file, FileVO param, int existingCount, int batchIndex,
			List<FileVO> diskSaved) throws IOException {
		FileVO meta = fileManager.save(file, param.getTargetType(), param.getTargetId());
		diskSaved.add(meta);
		meta.setMemberId(param.getMemberId());
		meta.setSortNo(existingCount + batchIndex + 1);
		meta.setIsRep(existingCount == 0 && batchIndex == 0 ? "Y" : "N");

		int cnt = fileMapper.doSave(meta);
		if (cnt != 1) {
			throw new IllegalStateException("파일 정보 저장에 실패했습니다.");
		}
		return meta;
	}

	private void cleanupPhysical(List<FileVO> diskSaved) {
		for (FileVO vo : diskSaved) {
			try {
				fileManager.deletePhysical(vo);
			} catch (IOException e) {
				log.warn("failed to cleanup orphan file: {}", vo.getSaveFileNm(), e);
			}
		}
	}

	private int countNonEmptyFiles(MultipartFile[] files) {
		if (files == null) {
			return 0;
		}
		int count = 0;
		for (MultipartFile file : files) {
			if (file != null && !file.isEmpty()) {
				count++;
			}
		}
		return count;
	}

	private void requireUploadCapacity(int existing, int incoming) {
		if (existing + incoming > MAX_FILES_PER_TARGET) {
			throw new IllegalArgumentException("이미지는 최대 9장까지 업로드할 수 있습니다.");
		}
	}

	private void requireUploadParam(FileVO param) {
		if (param == null || param.getTargetType() == null || param.getTargetId() <= 0) {
			throw new IllegalArgumentException("대상 정보가 올바르지 않습니다.");
		}
		if (param.getMemberId() <= 0) {
			throw new IllegalArgumentException("회원 정보가 올바르지 않습니다.");
		}
	}

	private FileVO toTargetKey(FileVO found) {
		FileVO target = new FileVO();
		target.setTargetType(found.getTargetType());
		target.setTargetId(found.getTargetId());
		return target;
	}

	private FileVO slotKey(FileVO target, int sortNo) {
		FileVO key = toTargetKey(target);
		key.setSortNo(sortNo);
		return key;
	}

	private FileVO deletedSortNoKey(FileVO target, int deletedSortNo) {
		FileVO key = toTargetKey(target);
		key.setSortNo(deletedSortNo);
		return key;
	}

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
}
