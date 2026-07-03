package com.pcwk.ehr.file.service;

import java.io.IOException;
import java.util.ArrayList;
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

@Service
public class FileService {

	private static final int MAX_FILES_PER_TARGET = 9;

	Logger log = LogManager.getLogger(getClass());

	@Autowired
	private FileMapper fileMapper;

	@Autowired
	private FileManager fileManager;

	public FileService() {
		log.debug("FileService");
	}

	@Transactional
	public List<FileVO> upload(MultipartFile[] files, FileVO param) throws IOException {
		requireUploadParam(param);
		List<FileVO> saved = new ArrayList<>();
		if (files == null) {
			return saved;
		}
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

	@Transactional
	public FileVO uploadOne(MultipartFile file, FileVO param) throws IOException {
		requireUploadParam(param);

		int count = fileMapper.countByTarget(param);
		if (count >= MAX_FILES_PER_TARGET) {
			throw new IllegalArgumentException("이미지는 최대 9장까지 업로드할 수 있습니다.");
		}

		FileVO meta = fileManager.save(file, param.getTargetType(), param.getTargetId());
		meta.setMemberId(param.getMemberId());
		meta.setSortNo(count + 1);
		meta.setIsRep(count == 0 ? "Y" : "N");

		int cnt = fileMapper.doSave(meta);
		if (cnt != 1) {
			throw new IllegalStateException("파일 정보 저장에 실패했습니다.");
		}
		return meta;
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

		FileVO target = new FileVO();
		target.setTargetType(found.getTargetType());
		target.setTargetId(found.getTargetId());
		fileMapper.clearRepByTarget(target);

		return fileMapper.updateRep(param);
	}

	@Transactional
	public int remove(FileVO param) throws IOException {
		if (param == null || param.getFileId() <= 0 || param.getMemberId() <= 0) {
			return 0;
		}

		FileVO found = fileMapper.doSelectOne(param);
		if (found == null || found.getMemberId() != param.getMemberId()) {
			return 0;
		}

		fileManager.deletePhysical(found);
		return fileMapper.doDelete(param);
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

	private void requireUploadParam(FileVO param) {
		if (param == null || param.getTargetType() == null || param.getTargetId() <= 0) {
			throw new IllegalArgumentException("대상 정보가 올바르지 않습니다.");
		}
		if (param.getMemberId() <= 0) {
			throw new IllegalArgumentException("회원 정보가 올바르지 않습니다.");
		}
	}
}
