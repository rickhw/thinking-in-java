package com.gtcafe.asimov.controller.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.model.Member;
import com.gtcafe.asimov.repository.MemberRepository;

@RestController
@RequestMapping("/api")
public class MemberController {

    @Autowired
    private MemberRepository memberRepository;

     @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/members")
    public String createMember(@RequestBody Member member) {
        var encodedPwd = passwordEncoder.encode(member.getPassword());
        member.setPassword(encodedPwd);
        member.setId(null);
        memberRepository.insert(member);
        return member.getId();
    }

    @GetMapping("/members")
    public List<Member> getMembers() {
        return memberRepository.findAll();
    }

}