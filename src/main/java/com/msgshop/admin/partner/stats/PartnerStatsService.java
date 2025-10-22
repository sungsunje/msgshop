package com.msgshop.admin.partner.stats;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PartnerStatsService {

    private final PartnerStatsMapper mapper;

    public PartnerStatsService(PartnerStatsMapper mapper) {
        this.mapper = mapper;
    }

    // =================== CRUD ===================
    public List<PartnerStatsVO> findAll() {
        return mapper.findAll();
    }

    public PartnerStatsVO findById(int statId) {
        return mapper.findById(statId);
    }

    public void insert(PartnerStatsVO vo) {
        mapper.insert(vo);
    }

    public void update(PartnerStatsVO vo) {
        mapper.update(vo);
    }

    public void delete(int statId) {
        mapper.delete(statId);
    }

    // --------------------------
    /**
     * 상품 상세 조회 시 조회수 기록
     * @param proNum 상품 번호
     */
    public void addViewCount(Integer proNum) {
        PartnerStatsVO vo = mapper.findByProductId(proNum);

        if (vo == null) {
            PartnerStatsVO newVo = new PartnerStatsVO();
            newVo.setProNum(proNum);
            newVo.setViewCount(1);
            newVo.setPhoneInquiryCount(0);
            mapper.insert(newVo);
        } else {
            vo.setViewCount(vo.getViewCount() + 1);
            mapper.update(vo);
        }
    }

    // =================== 통계 관련 ===================

    /**
     * 전체 통계: 웹사이트 전체 상품 조회수와 전화 문의 수 합계
     */
    public Map<String, Integer> getTotalStats() {
        Map<String, Integer> total = new HashMap<>();
        Integer totalView = mapper.sumViewCount();
        Integer totalPhone = mapper.sumPhoneInquiry();
        total.put("view", totalView != null ? totalView : 0);
        total.put("phone", totalPhone != null ? totalPhone : 0);
        return total;
    }

    /**
     * 상품명 또는 상품번호 + 날짜 범위 통계 조회
     * @param proName 상품명 (null이면 전체)
     * @param startDate 시작일
     * @param endDate 종료일
     */
    public List<PartnerStatsVO> findStatsByProductAndDate(String proName, LocalDate startDate, LocalDate endDate) {
        Integer proNum = null;
        if (proName != null && !proName.isEmpty()) {
            proNum = mapper.findProNumByName(proName);
        }
        return mapper.selectByProductAndDate(proNum, startDate, endDate);
    }

    /**
     * 전체 statsList 조회 (초기 테이블/그래프용)
     */
    public List<PartnerStatsVO> getAllStats() {
        return mapper.selectByProductAndDate(null, LocalDate.of(2000, 1, 1), LocalDate.now());
    }
}
