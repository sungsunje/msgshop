package com.msgshop.admin.review;

import java.util.List;
import org.springframework.stereotype.Service;

import com.msgshop.common.utils.SearchCriteria;
import com.msgshop.review.ReviewReply;
import com.msgshop.review.ReviewVO;

import lombok.RequiredArgsConstructor;


// 비지니스 로직작업 목적
@RequiredArgsConstructor
@Service
public class AdReviewService {

	private final AdReviewMapper adReviewMapper;
	
	public List<ReviewVO> review_list(SearchCriteria cri, String rev_rate, String rev_content) {
	    return adReviewMapper.review_listWithProduct(cri, rev_rate, rev_content);
	}
	
	public int review_count(SearchCriteria cri, String rev_rate, String rev_content) {
		return adReviewMapper.review_count(cri, rev_rate, rev_content);
	}
	
	public ReviewReply reply_info(Long reply_id) {
		return adReviewMapper.reply_info(reply_id);
	}
	
	public void reply_insert(ReviewReply vo) throws Exception {
	    adReviewMapper.reply_insert(vo);
	}
	
	public void reply_modify(Long reply_id, String reply_text) {
		adReviewMapper.reply_modify(reply_id, reply_text);
	}
	
	public void reply_delete(Long reply_id) {
		adReviewMapper.reply_delete(reply_id);
	}
}
