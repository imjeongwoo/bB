package com.teambB.koting.service;

import com.teambB.koting.domain.Apply;
import com.teambB.koting.domain.Meeting;
import com.teambB.koting.domain.MeetingStatus;
import com.teambB.koting.domain.Member;
import com.teambB.koting.repository.ApplyRepository;
import com.teambB.koting.repository.MeetingRepository;
import com.teambB.koting.repository.MemberRepository;
import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

  @Autowired
  private JavaMailSender javaMailSender;
  private final MemberRepository memberRepository;
  private final MeetingRepository meetingRepository;
  private final ApplyService applyService;
  private final ApplyRepository applyRepository;

  public Long join(Member member) {
    if (member.getId() == null) {
      validateMember(member);
    }
    memberRepository.save(member);
    return member.getId();
  }

  private void validateMember(Member member) {
    List<Member> findByNumber = memberRepository.findByNumberList(member.getNumber());
    List<Member> findByEmail = memberRepository.findByEmailList(member.getEmail());
    if (!findByNumber.isEmpty() || !findByEmail.isEmpty()) {
        throw new IllegalStateException("이미 존재하는 회원입니다.");
    }
  }

  public void deleteMember(String accountId) {
    Member member = memberRepository.findByAccountId(accountId);
    memberRepository.delete(member);

    List<Meeting> meetingList = meetingRepository.findByMemberId(member.getId());
    for (Meeting meeting : meetingList) {
      meetingRepository.delete(meeting);
    }
  }

  public Member findOne(Long id) {
    return memberRepository.findOne(id);
  }

  public Member findOneByEmail(String email) {
    return memberRepository.findByEmail(email);
  }

  public List<Member> findOneByNumber(String number) {
    return memberRepository.findByNumberList(number);
  }

  public Member findOneByAccountId(String accountId) {
    return memberRepository.findByAccountId(accountId);
  }

  public void clearMyMeetingId(String accountId) {
    Member member = memberRepository.findByAccountId(accountId);

    // 삭제 말고 전체 거절처리
    Meeting myMeeting = meetingRepository.findById(member.getMyMeetingId());
    for (Apply apply_ : myMeeting.getParticipants()) {
      Apply one = applyService.findOne(apply_.getId());
      one.rejectAccept();
    }
    myMeeting.setMeetingStatus(MeetingStatus.CLOSE);
    member.setMyMeetingId(null);
  }

  public JSONObject setMemberInfo(Member member) {
    JSONObject retObject = new JSONObject();

    retObject.put("age", member.getAge());
    retObject.put("animal_idx", member.getAnimalIdx());
    retObject.put("height", member.getHeight());
    retObject.put("college", member.getCollege());
    retObject.put("nickname", member.getNickname());
    retObject.put("major", member.getMajor());
    retObject.put("sex", member.getSex());
    retObject.put("mbti", member.getMbti());

    return retObject;
  }

  public void sendMail(String email, String authKey) throws MessagingException, UnsupportedEncodingException {

    String to = email;
    String from = "noreply@koting.kr";
    String subject = "[코팅] 회원가입 인증메일입니다. ";
    String url = "https://koting.kr/auth/email?email=" + email + "&authKey=" + authKey;

    StringBuilder body = new StringBuilder();
    body.append("<html> <body>");
    body.append("<div> 동국대학교 학우님 반갑습니다! </div>");
    body.append("<div> 하단의 링크를 클릭해주세요! </div>");
    body.append("<a href=\"" + url + "\">인증하기</a>");
    body.append("</body> </html>");

    MimeMessage message = javaMailSender.createMimeMessage();
    MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true, "UTF-8");

    mimeMessageHelper.setFrom(from,"noreply@koting.kr");
    mimeMessageHelper.setTo(to);
    mimeMessageHelper.setSubject(subject);
    mimeMessageHelper.setText(body.toString(), true);
    javaMailSender.send(message);
  }
}
