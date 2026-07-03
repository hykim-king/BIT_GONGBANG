package com.pcwk.ehr.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pcwk.ehr.file.domain.FileVO;

@Mapper
public interface FileMapper {

	int doSave(FileVO param);

	FileVO doSelectOne(FileVO param);

	List<FileVO> selectByTarget(FileVO param);

	int countByTarget(FileVO param);

	int clearRepByTarget(FileVO param);

	int updateRep(FileVO param);

	int doDelete(FileVO param);

	int deleteByTarget(FileVO param);
}
