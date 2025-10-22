package com.msgshop.admin.partner.stats;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PartnerStatsMapper {

    List<PartnerStatsVO> findAll();
    PartnerStatsVO findById(int statId);
    PartnerStatsVO findByProductId(@Param("proNum") Integer proNum);
    void insert(PartnerStatsVO vo);
    void update(PartnerStatsVO vo);
    void delete(int statId);

    Integer sumViewCount();
    Integer sumPhoneInquiry();

    Integer findProNumByName(@Param("proName") String proName);

    List<PartnerStatsVO> selectByProductAndDate(
        @Param("proNum") Integer proNum,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
