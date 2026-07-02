package com.pcwk.ehr.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.pcwk.ehr.artwork.domain.ArtworkVO;
public interface ArtworkMapper {
    List<ArtworkVO> selectMain(ArtworkVO vo);
    List<ArtworkVO> selectRecommend(@Param("limit") int limit);
    List<ArtworkVO> selectPopular(@Param("days") int days, @Param("limit") int limit);
    List<ArtworkVO> search(ArtworkVO vo);
    int searchCount(ArtworkVO vo);
}