package com.msgshop.kakaologin;

public interface KakaoLoginDao {
//	 로그인 id 확인
	public KakaoLoginDto kakaoOne(KakaoLoginDto dto);
	
	// 회원등록
	public int kakaoInsert(KakaoLoginDto dto);
}
