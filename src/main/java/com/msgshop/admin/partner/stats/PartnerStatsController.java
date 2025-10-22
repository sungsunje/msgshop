package com.msgshop.admin.partner.stats;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin/partner/stats")
public class PartnerStatsController {

    private final PartnerStatsService service;

    public PartnerStatsController(PartnerStatsService service) {
        this.service = service;
    }

    /**
     * 1) 기본 페이지: 전체 통계
     */
    @GetMapping
    public String getStatsPage(Model model) {
        // 1. 전체 조회수/전화문의 가져오기
        Map<String, Integer> totalStats = service.getTotalStats();
        if (totalStats == null) totalStats = new HashMap<>();
        totalStats.putIfAbsent("view", 0);
        totalStats.putIfAbsent("phone", 0);
        model.addAttribute("totalStats", totalStats);

        // 2. 전체 statsList 가져오기 (초기 테이블 및 그래프용)
        List<PartnerStatsVO> statsList = service.getAllStats(); // 서비스에 getAllStats() 구현 필요
        if (statsList == null) statsList = new ArrayList<>();
        model.addAttribute("statsList", statsList);

        // 3. 그래프용 리스트 미리 생성
        List<String> statDates = new ArrayList<>();
        List<Integer> viewCounts = new ArrayList<>();
        List<Integer> phoneCounts = new ArrayList<>();
        for (PartnerStatsVO stat : statsList) {
            statDates.add(stat.getStatDate().toString());
            viewCounts.add(stat.getViewCount());
            phoneCounts.add(stat.getPhoneInquiryCount());
        }
        model.addAttribute("statDates", statDates);
        model.addAttribute("viewCounts", viewCounts);
        model.addAttribute("phoneCounts", phoneCounts);

        return "admin/partnerStats/partner_stats";
    }

    /**
     * 2) 상세 검색: Ajax
     */
    @GetMapping("/search")
    @ResponseBody
    public Map<String, Object> searchStats(
            @RequestParam(required = false, defaultValue = "") String partnerName,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 이후일 수 없습니다.");
        }

        List<PartnerStatsVO> statsList = service.findStatsByProductAndDate(partnerName, startDate, endDate);
        if (statsList == null) statsList = new ArrayList<>();

        List<String> dates = new ArrayList<>();
        List<Integer> viewCounts = new ArrayList<>();
        List<Integer> phoneCounts = new ArrayList<>();
        for (PartnerStatsVO stat : statsList) {
            dates.add(stat.getStatDate().toString());
            viewCounts.add(stat.getViewCount());
            phoneCounts.add(stat.getPhoneInquiryCount());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("statsList", statsList);
        result.put("dates", dates);
        result.put("viewCounts", viewCounts);
        result.put("phoneCounts", phoneCounts);

        return result;
    }
}
