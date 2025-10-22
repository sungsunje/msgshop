package com.msgshop.kakaologin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KakaoLoginDto {
	
	
	private String code;  // 카카오 로그인 API서버에서 받게되는 파라미터명. 용도는 토큰요청인가코드
	
	
	private Long   id;
	private String name;
	private String email;
	private String phone;
	
	private String memberEmail;
	private String memberName;
	private String memberSeq;

	

}
