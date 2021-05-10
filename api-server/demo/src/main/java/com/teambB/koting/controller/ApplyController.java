package com.teambB.koting.controller;

import com.teambB.koting.domain.Apply;
import com.teambB.koting.domain.Meeting;
import com.teambB.koting.domain.Member;
import com.teambB.koting.service.ApplyService;
import com.teambB.koting.service.MeetingService;
import com.teambB.koting.service.MemberService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ApplyController {

  @Autowired private final MeetingService meetingService;
  @Autowired private final MemberService memberService;
  @Autowired private final ApplyService applyService;

  @GetMapping("/applies")
  public JSONObject getMyMeetingInfo(@RequestParam("account_id") String accountId) {
    JSONObject retObject = new JSONObject();

    Member member = memberService.findOneByAccountId(accountId);
    Meeting myMeeting = member.getMyMeeting();

    if (myMeeting != null) {
        JSONObject myMeetingInfo = new JSONObject();
        JSONObject ownerInfo = new JSONObject();
        JSONObject myCreation = new JSONObject();

        ownerInfo.put("college", member.getCollege());
        ownerInfo.put("major", member.getMajor());
        ownerInfo.put("sex", member.getSex());
        ownerInfo.put("mbti", member.getMbti());
        ownerInfo.put("animal_idx", member.getAnimalIdx());
        ownerInfo.put("age", member.getAge());
        ownerInfo.put("height", member.getHeight());

        myMeetingInfo.put("owner", ownerInfo);
        myMeetingInfo.put("meeting_id", member.getMyMeeting().getId());
        myMeetingInfo.put("link", member.getMyMeeting().getLink());
        myMeetingInfo.put("player", member.getMyMeeting().getPlayer());

        myCreation.put("myMeeting", myMeetingInfo);

        List<Apply> participants = myMeeting.getParticipants();
        JSONArray jArray = new JSONArray();
        for (int i = 0; i < participants.size(); i++) {
          JSONObject myInfo = new JSONObject();
          myInfo.put("age", participants.get(i).getMember().getAge());
          myInfo.put("animal_idx", participants.get(i).getMember().getAnimalIdx());
          myInfo.put("height", participants.get(i).getMember().getHeight());
          myInfo.put("college", participants.get(i).getMember().getCollege());
          myInfo.put("major", participants.get(i).getMember().getMajor());
          myInfo.put("sex", participants.get(i).getMember().getSex());
          myInfo.put("mbti", participants.get(i).getMember().getMbti());
          myInfo.put("account_id", participants.get(i).getMember().getAccount_id());
          jArray.add(myInfo);
        }

        myCreation.put("participant", jArray);
        retObject.put("myCreation", myCreation);

    }

    JSONArray jArray2 = new JSONArray();

    if (member.getApplies() != null) {
      List<Apply> applies = member.getApplies();

      for (int i = 0; i < applies.size(); i++) {
        JSONObject meetingOwner = new JSONObject();
        meetingOwner.put("age", applies.get(i).getMeeting().getOwner().getAge());
        meetingOwner.put("animal_idx", applies.get(i).getMeeting().getOwner().getAnimalIdx());
        meetingOwner.put("height", applies.get(i).getMeeting().getOwner().getHeight());
        meetingOwner.put("college", applies.get(i).getMeeting().getOwner().getCollege());
        meetingOwner.put("major", applies.get(i).getMeeting().getOwner().getMajor());
        meetingOwner.put("sex", applies.get(i).getMeeting().getOwner().getSex());
        meetingOwner.put("mbti", applies.get(i).getMeeting().getOwner().getMbti());

        JSONObject meetingInfo = new JSONObject();
        meetingInfo.put("owner", meetingOwner);
        meetingInfo.put("meeting_id", applies.get(i).getMeeting().getId());
        meetingInfo.put("link", applies.get(i).getMeeting().getLink());
        meetingInfo.put("player", applies.get(i).getMeeting().getPlayer());
        jArray2.add(meetingInfo);
      }

      retObject.put("myApplies", jArray2);
    }
    return retObject;
  }

  @PostMapping("/applies")
  public JSONObject applyMeeting(@RequestBody JSONObject object) {

    JSONObject retObject = new JSONObject();
    String accountId = object.get("account_id").toString();
    Member member = memberService.findOneByAccountId(accountId);

    Long meetingId = Long.parseLong(object.get("meeting_id").toString());
    Meeting meeting = meetingService.findOne(meetingId);

    Apply apply = Apply.createApply(member, meeting);
    applyService.join(apply);
    retObject.put("result", "applyMeetingSuccess");

    return retObject;
  }

  @PostMapping("/applies/accept")
  public Boolean acceptApply(@RequestBody JSONObject object) {

    String myAccountId = object.get("my_account_id").toString();
    String yourAccountId = object.get("your_account_id").toString();

    Member me = memberService.findOneByAccountId(myAccountId);
    Member you = memberService.findOneByAccountId(yourAccountId);

    Meeting myMeeting = me.getMyMeeting();
    me.getSuccessMeeting().add(myMeeting);
    you.getSuccessMeeting().add(myMeeting);
    memberService.join(me);
    memberService.join(you);
    return true;
  }
}
