package com.msgshop.member;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.msgshop.kakaologin.KakaoLoginDto;
import com.msgshop.kakaologin.KakaoLoginService;
import com.msgshop.mail.EmailDTO;
import com.msgshop.mail.EmailService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@RequestMapping("/member/*")
@Slf4j
@Controller
public class MemberController {

	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;
	private final MemberService memberService;
	private final KakaoLoginService kservice;
	
	@Value("${kakao.authorize}")
	private String authorize;
	
	@Value("${kakao.client_id}")
    private String client_id;

    @Value("${kakao.redirect_uri}")
    private String redirect_uri;
    
    @Value("${kakao.client_secret}")
    private String client_secret;
	
	
	// 회원가입 폼
	@GetMapping("/join")  //  /member/join.html
	public void join() {
		
	}
	
	// 아이디 중복체크
	@GetMapping("/idCheck")
	public ResponseEntity<String> idCheck(String mbsp_id) throws Exception {
		
		ResponseEntity<String> entity = null;
		
		String isUse = "";
		
		if(memberService.idCheck(mbsp_id) != null) {
			isUse = "no"; // 아이디 사용불가능
		}else {
			isUse = "yes"; // 아이디 사용가능
		}
		
		entity = new ResponseEntity<String>(isUse, HttpStatus.OK);
		
		return entity;
	}
	
	//회원정보저장
	@PostMapping("/join")
	public String join(MemberVO vo) {
		
		//log.info("회원정보 비밀번호 암호화 전 : " + vo);
		
		// passwordEncoder.encode(vo.getU_pw()) : 비밀번호를 암호화
		vo.setMbsp_password(passwordEncoder.encode(vo.getMbsp_password()));
		
		log.info("회원정보 비밀번호 암호화 후: " + vo);
		
		
		// db에 저장.
		memberService.join(vo);
		
		return "redirect:/member/login";
	}
	
	// 로그인 폼
	/*
	@GetMapping("/login")
	public void loginForm() {
		
	}
	*/
	
	@GetMapping("/login")
	public void loginForm(Model model) {
		  String location = authorize + "?response_type=code&client_id="+client_id+"&redirect_uri="+redirect_uri + "&prompt=login";
	      model.addAttribute("location", location);
	}
	
	
	// 로그인 처리.
	@PostMapping("/login")  // loginProcess(String u_id, String u_pw, HttpSession session)
	public String loginProcess(LoginDTO dto, HttpSession session, RedirectAttributes rttr) throws Exception {
		
		// 작업? 아이디와비번이 정상적이면, 세션객체로 인증작업을 처리하고, 메인페이지로 이동시킨다.
		// 아이디 또는 비번이 틀린 경우이면, 다시 로그인페이지로 이동시킨다.
		
		// memberVO가 null인지 여부를 체크
		// null이면 아이디가 존재안한다. null 아니면 아이디가 존재한다는 의미.
		MemberVO memberVO = memberService.login(dto.getMbsp_id());
		
		
		String url = "";
		String status = "";
		if(memberVO != null) { // 아이디가 존재  matches("사용자가 입력비밀번호", "db에서 가져온 암호된비밀번호")
			// 사용자가 입력한 비밀번호가 db에서 가져온 암호화된 비밀번호를 만든것인지 확인
			if(passwordEncoder.matches(dto.getMbsp_password(), memberVO.getMbsp_password())) { // 비번이 맞는의미
				// 사용자를 인증처리하기위한 정보
				// UserInfo클래스인 userInfo객체가 Object형으로 저장된다. 꺼내올 때는 원래의 형(UserInfo클래스)으로 형변환시켜야 한다.
				memberVO.setMbsp_password("");
				session.setAttribute("login_auth", memberVO);
				
				if(session.getAttribute("targetUrl") != null) {
					url = (String) session.getAttribute("targetUrl");
					
					if(session.getAttribute("postData") != null) {
						log.info("데이타: " + session.getAttribute("postData"));
						
						url = url + "?" +  (String) session.getAttribute("postData");
					}
					
				}else {
					url = "/";
					
				}
			}else { // 비번이 틀린의미.
				status = "pwFail";
				url = "/member/login";
			}
		}else {  // 아이디가 존재 안한다.
			status = "idFail";
			url = "/member/login";
		}
		

		
		
		
		// 이동되는 주소의 타임리프페이지에서 status 이름으로 사용할수가 있다. 페이지에서 자바스크립트 문법으로 사용
		rttr.addFlashAttribute("status", status);
		
		
		return "redirect:"+ url;
	}
	
	// 로그아웃
	/*
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		// session.setAttribute("login_auth", memberVO); 사용자를 인증처리하기위한 정보를 아래작업에서 소멸.
		session.invalidate(); // 서버측의 세션으로 저장된 모든메모리가 소멸.
		
		return "redirect:/";
	}
	*/
	
	// 로그아웃
	@GetMapping("/logout")
	public String logout(HttpSession session) throws JsonProcessingException {
		
	
		String kakao_login_auth =  (String) session.getAttribute("kakao_login_auth");
		
		if(kakao_login_auth != null && !"".equals(kakao_login_auth)) {
			String accessToken =  (String) session.getAttribute("accessToken");
			
			// 카카오 로그아웃 API 호출
			kservice.kakaologout(accessToken);
			
			// 카카오 로그인 인증정보를 세션으로 소멸
			session.removeAttribute("kakao_login_auth");
			session.removeAttribute("accessToken");

		}
		// 현재 프로젝트에서 일반로그인상태를 로그아웃 처리.
		session.invalidate();
		
		
		return "redirect:/";  // 메인주소로 이동.
	}
	
	// 카카오 로그인 API서버에서 호출받게되는 주소.
		// 카카오 디벨로퍼 사이트에서 로그인작업을 위하여 미리 설정해둠.
	@GetMapping(value="/kakao/callback")
    public String loginKakaoRedirect(KakaoLoginDto dto, MemberVO vo, Model model, HttpSession session) throws Exception {
    	
		// code 라는 파라미터명으로 토큰요청인가코드를 보내준다.
		// cIZpq5QRFhTLQKoijLXd3Z4oKJGTveb1kyedVAccWbEb_GJohpQ9vAAAAAQKDRSjAAABmaPlSxNtZc76WqiBKA
		System.out.println("토큰요청인가코드: "+dto.getCode());
		
    	
    	
    	// 인증토큰 받기 
    	String accessToken = kservice.getAccessTokenFromKakao(client_id, dto.getCode(), redirect_uri, client_secret);
    	
    	// 카카오서버로부터 개인회원정보 받기
    	dto = kservice.getUserInfo(accessToken, dto);
    	
    	log.info("KakaoLoginDto: " + dto);
		  
    	// 회원존재확인
    		
		/*
    	if(kservice.kakaoOne(dto) != null) {
			//by pass
		}else {
			kservice.kakaoInsert(dto);
		}
		KakaoLoginDto rtId = kservice.kakaoOne(dto);
		httpSession.setAttribute("sessNameUsr", rtId.getMemberName());
		httpSession.setAttribute("sessSeqUsr", rtId.getMemberSeq());

        model.addAttribute("info", dto);
        */
    	
    	// 카카오 로그인 인증정보저장.
    	session.setAttribute("kakao_login_auth", dto.getEmail()); // 일반 로그인상태인지 카카오로그인상태인지 구분
    	session.setAttribute("accessToken", accessToken); // 인증토큰을 카카오 로그아웃에 사용하기위하여 세션으로 저장.
    	
        
        return "redirect:/";
    }
	
	//회원수정 폼.  select문 회원정보를 읽어오기.
	@GetMapping("/modify")
	public void modify(HttpSession session, Model model) throws Exception {
		
		log.info("modify 호출");
		
		// 로그인시 저장한 구문. session.setAttribute("login_auth", userInfo);
		String mbsp_id = ((MemberVO) session.getAttribute("login_auth")).getMbsp_id();
		
		MemberVO memberVO = memberService.modify(mbsp_id);
		
		//log.info("회원수정정보" + memberVO);
		
		model.addAttribute("memberVO", memberVO);
	}
	
	//회원수정하기
	@PostMapping("/modify")
	public String modify(MemberVO vo) throws Exception {
		
		memberService.modify_save(vo);
		
		return "redirect:/";
	}
	
	// 클라이언트에서 데이타를 보낼때 사용한 파라미터명으로 컨트롤러 메서드에서는 동일하게 사용해야 한다.(규칙)
	 // 메서드의 리턴타입이 void인 경우는 매핑주소가 파일명이된다. member/mypage.html
	// 리턴타입이 String 일때는 return "문자열" 이 파일명이된다.
	@GetMapping("/mypage") 
	public void mypage() throws Exception {
		
	}
	
	//비밀번호 변경하기 폼
	@GetMapping("/pwchange") //  /member/pwchange.html
	public void pwchange() throws Exception {
		
	}
	
	//비밀번호 변경하기   <form><button type="submit" class="btn btn-primary">비밀번호 변경하기</button></form>
	@PostMapping("/pwchange")
	@ResponseBody
	public Map<String, String> pwchangeAjax(
	        @RequestParam("cur_pw") String curPw,
	        @RequestParam("new_pw") String newPw,
	        HttpSession session) throws Exception {

	    Map<String, String> res = new HashMap<>();
	    MemberVO loginUser = (MemberVO) session.getAttribute("login_auth");
	    String mbspId = loginUser.getMbsp_id();
	    String mbspEmail = loginUser.getMbsp_email();

	    // DB에서 최신 비밀번호 조회
	    MemberVO dbUser = memberService.modify(mbspId);
	    String dbPassword = dbUser.getMbsp_password();

	    if(passwordEncoder.matches(curPw, dbPassword)) {
	        String encodeNewPw = passwordEncoder.encode(newPw);
	        memberService.pwchange(mbspId, encodeNewPw);

	        // 비밀번호 변경 알림 메일
	        EmailDTO dto = new EmailDTO();
	        dto.setReceiverMail(mbspEmail);
	        dto.setSubject("Ezen Mall 비밀번호 변경 알림");
	        emailService.sendMail("mail/pwchange", dto, newPw);

	        res.put("status", "success");
	    } else {
	        res.put("status", "fail");
	    }

	    return res;
	}
	
	// 아이디및비밀번호 찾기 폼
	@GetMapping("/lostpass")
	public String lostpass() throws Exception {
		
		return "member/lostpass";
	}
	
	// 아이디찾기 - 메일발송
	// @ResponseBody
	@GetMapping("/idsearch")
	public ResponseEntity<String> idsearch(String mbsp_name, String mbsp_email) throws Exception {
		
		ResponseEntity<String> entity = null;
		
		String result = "";
		
		String mbsp_id = memberService.idsearch(mbsp_name, mbsp_email);
		
		if(mbsp_id != null) {
		
			// 아이디 메일발송
			String type = "mail/idsearch";
			
			EmailDTO dto = new EmailDTO();
			dto.setReceiverMail(mbsp_email); // 받는사람 메일주소
			dto.setSubject("Ezen Mall 아이디 찾기결과를 보냅니다.");
			
			result = "success";
			emailService.sendMail(type, dto, mbsp_id);
		}else {
			result = "fail";
		}
		
		entity = new ResponseEntity<String>(result, HttpStatus.OK);
		
		return entity;
	}
	
	// 임시비밀번호 발급 - 메일발송
	@GetMapping("/pwtemp")
	public ResponseEntity<String> pwtemp(String mbsp_id, String mbsp_email) throws Exception {
		
		ResponseEntity<String> entity = null;
		
		String result = "";
		
		// 아이디와 전자우편이 존재하는 지 DB에서 체크
		String d_u_email = memberService.pwtemp_confirm(mbsp_id, mbsp_email);
		
		if(d_u_email != null) {
			result = "success";
			
			// createAuthCode()메서드가 emailService인터페이스의 추상메서드로 만든것이 아니라
			// EmailServiceImpl클래의 메서드로 존재하기 때문에, EmailServiceImpl클래스로 형변화해서
			// 호출해야 한다.(자바문법)
			// 임시비밀번호 암호화하여, DB에 저장.
			String imsi_pw = emailService.createAuthCode();
			
			// u_id, imsi_pw 암호화
			memberService.pwchange(mbsp_id,  passwordEncoder.encode(imsi_pw));
			
			
			// 아이디 메일발송
			String type = "mail/pwtemp";
			
			EmailDTO dto = new EmailDTO();
			dto.setReceiverMail(d_u_email); // 받는사람 메일주소
			dto.setSubject("Ezen Mall 임시비밀번호를 보냅니다.");

			emailService.sendMail(type, dto, imsi_pw);
			
		}else {
			result = "fail";
		}
		
		entity = new ResponseEntity<String>(result, HttpStatus.OK);
		
		return entity;
	}
	
	   @Controller
	   @RequestMapping("/member")
	   public class MemberProfileController {

	       @GetMapping("/profile")
	       public String profile() {
	           // templates/member/profile.html 로 렌더링
	           return "member/profile";
	       }
	   }
}
