// package com.gtcafe.asimov.controller;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RestController;

// import com.gtcafe.asimov.model.Member;
// import com.gtcafe.asimov.repository.MemberRepository;

// @RestController
// public class MyController {

//     @Autowired
//     private MemberRepository memberRepository;

//      @Autowired
//     private PasswordEncoder passwordEncoder;

//     @PostMapping("/members")
//     public String createMember(@RequestBody Member member) {
//         var encodedPwd = passwordEncoder.encode(member.getPassword());
//         member.setPassword(encodedPwd);
//         member.setId(null);
//         memberRepository.insert(member);
//         return member.getId();
//     }

//     @GetMapping("/members")
//     public List<Member> getMembers() {
//         return memberRepository.findAll();
//     }

//     @GetMapping("/selected-courses")
//     public String selectedCourses() {
//         return "修課清單";
//     }

//     @GetMapping("/course-feedback")
//     public String courseFeedback() {
//         return "課程回饋";
//     }

//     // @GetMapping("/home")
//     // public String home() {
//     //     return "系統首頁";
//     // }
// }
