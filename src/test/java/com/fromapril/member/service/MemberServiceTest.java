package com.fromapril.member.service;

import com.fromapril.member.dto.MemberIdentifyDTO;
import com.fromapril.member.dto.MemberJoinDTO;
import com.fromapril.member.domain.member.Member;
import com.fromapril.member.domain.member.Profile;
import com.fromapril.member.repository.MemberRepository;
import com.fromapril.member.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {
    @Autowired MemberRepository memberRepository;
    @Autowired ProfileRepository profileRepository;

    @Autowired MemberService memberService;

    @Test
    public void 회원가입() {
        // given
        String memberEmail = "hello@local.com";
        String memberPassword = "hello";
        String nickname = "1";
        String thumbnailUrl = "2";
        String personalStatus = "3";
        MemberJoinDTO memberJoinDTO = new MemberJoinDTO(memberEmail, memberPassword, nickname, thumbnailUrl, personalStatus);

        // when
        memberService.join(memberJoinDTO);
        Member foundMember = memberRepository.findByEmail(memberEmail).orElseThrow();

        // then
        assertEquals(memberEmail, foundMember.getEmail());
        assertEquals(memberPassword, foundMember.getPassword());
        assertEquals(nickname, foundMember.getProfile().getNickname());
        assertEquals(thumbnailUrl, foundMember.getProfile().getThumbnailImage());
        assertEquals(personalStatus, foundMember.getProfile().getPersonalStatus());
    }

    @Test
    public void 회원가입_할_때_이메일은_유일해야_함() {
        // given
        String memberEmail = "hello@local.com";
        String memberPassword = "hello";
        String nickname = "1";
        String thumbnailUrl = "2";
        String personalStatus = "3";

        // when
        MemberJoinDTO memberJoinDTO = new MemberJoinDTO(memberEmail, memberPassword, nickname, thumbnailUrl, personalStatus);
        MemberJoinDTO memberJoinDTO2 = new MemberJoinDTO(memberEmail, memberPassword, nickname, thumbnailUrl, personalStatus);
        memberService.join(memberJoinDTO);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> memberService.join(memberJoinDTO2));
    }

    @Test
    public void 회원가입_할_때_닉네임은_유일해야_함() {
        // given
        String memberEmail = "hello@local.com";
        String memberPassword = "hello";
        String nickname = "1";
        String thumbnailUrl = "2";
        String personalStatus = "3";

        // when
        MemberJoinDTO memberJoinDTO = new MemberJoinDTO(memberEmail, memberPassword, nickname, thumbnailUrl, personalStatus);
        MemberJoinDTO memberJoinDTO2 = new MemberJoinDTO("11"+memberEmail, memberPassword, nickname, thumbnailUrl, personalStatus);
        memberService.join(memberJoinDTO);

        // then
        assertThrows(IllegalArgumentException.class, () -> memberService.join(memberJoinDTO2));
    }


    @Test
    public void 프로필업데이트() {
        String memberEmail = "hello@local.com";
        String memberPassword = "hello";
        String nickname = "hello";
        String thumbnailImage = "hello";
        String personalStatus = "whatisthisfor";

        Member member = Member.createMember(memberEmail, memberPassword);
        memberRepository.save(member);
        
        Profile profile = new Profile();
        profile.setNickname(nickname);
        profile.setThumbnailImage(thumbnailImage);
        profile.setPersonalStatus(personalStatus);
        profileRepository.save(profile);

        MemberJoinDTO memberJoinDTO = new MemberJoinDTO(
                memberEmail,
                memberPassword,
                nickname,
                thumbnailImage,
                personalStatus
        );

        memberService.update(memberJoinDTO);

        assertEquals(member.getProfile(), profile);
        assertEquals(member.getProfile().getMember(), member);
    }

    public void 닉네임_겹치면_프로필업데이트_못함() {
        String memberEmail = "hello@local.com";
        String memberPassword = "hello";
        String nickname = "hello";
        String thumbnailImage = "hello";
        String personalStatus = "whatisthisfor";

        Member member = Member.createMember(memberEmail, memberPassword);
        memberRepository.save(member);

        Profile profile = new Profile();
        profile.setNickname(nickname);
        profile.setThumbnailImage(thumbnailImage);
        profile.setPersonalStatus(personalStatus);
        profileRepository.save(profile);

        MemberJoinDTO memberJoinDTO = new MemberJoinDTO(
                memberEmail,
                memberPassword,
                nickname,
                thumbnailImage,
                personalStatus
        );

        memberService.update(memberJoinDTO);


    }
    
    @Test
    public void 회원탈퇴() {
        String memberEmail = "hello@local.com";
        String memberPassword = "hello";
        Member member = Member.createMember(memberEmail, memberPassword);
        memberRepository.save(member);

        MemberIdentifyDTO memberIdentifyDto = new MemberIdentifyDTO(memberEmail, memberPassword);
        memberService.leave(memberIdentifyDto);

        Member foundMember = memberRepository.findById(member.getId()).orElseThrow();
        assertTrue(foundMember.isLeaved());
    }

    @Test
    public void 비밀번호틀리면_탈퇴_못함() {
        String memberEmail = "hello@local.com";
        String memberPassword = "hello";
        Member member = Member.createMember(memberEmail, memberPassword);
        memberRepository.save(member);

        MemberIdentifyDTO memberIdentifyDto = new MemberIdentifyDTO(memberEmail, memberPassword + "hhhh");
        assertThrows(IllegalArgumentException.class, () -> memberService.leave(memberIdentifyDto));
    }

    @Test
    public void 이미_탈퇴하면_탈퇴_못함() {
        String memberEmail = "hello@local.com";
        String memberPassword = "hello";
        Member member = Member.createMember(memberEmail, memberPassword);
        member.setLeaved(true);
        memberRepository.save(member);

        MemberIdentifyDTO memberIdentifyDto = new MemberIdentifyDTO(memberEmail, memberPassword + "hhhh");
        assertThrows(IllegalArgumentException.class, () -> memberService.leave(memberIdentifyDto));
    }

    @Test
    public void 지만_조회_가능함() {
        String memberEmail = "hello@local.com";
        String memberPassword = "hello";
        Member member = Member.createMember(memberEmail, memberPassword);
        memberRepository.save(member);

        MemberIdentifyDTO memberIdentifyDto = new MemberIdentifyDTO(memberEmail, memberPassword);
        Member memberSelf = memberService.mine(memberIdentifyDto);

        assertEquals(member.getId(), memberSelf.getId());
        assertThrows(IllegalArgumentException.class, () -> memberService.mine(new MemberIdentifyDTO(memberEmail, memberPassword + "hhhh")));
    }
}