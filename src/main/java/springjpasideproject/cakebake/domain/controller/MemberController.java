package springjpasideproject.cakebake.domain.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import springjpasideproject.cakebake.domain.Member;
import springjpasideproject.cakebake.domain.controller.form.*;
import springjpasideproject.cakebake.domain.dto.MemberDto;
import springjpasideproject.cakebake.domain.service.MemberService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/member/join")
    public String createMemberForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    @PostMapping("/member/join")
    public String createMember(@Valid MemberForm memberForm,
                               BindingResult result) {

        if (result.hasErrors()) {
            return "redirect:/member/join";
        }

        memberService.join(memberForm.getUserId(), memberForm.getPassword(), memberForm.getName(), memberForm.getPhone(), memberForm.getEmail());
        return "redirect:/";
    }

    @GetMapping("/member/login")
    public String loginForm() {
        return "members/loginForm";
    }

    @GetMapping("/member/id/find")
    public String findId(Model model) {
        model.addAttribute("findIdForm", new FindIdForm());
        return "members/findIdForm";
    }

    @PostMapping("/member/id/find")
    public String findId(@Valid FindIdForm findIdForm,
                         BindingResult result,
                         RedirectAttributes redirect) {

        if (result.hasErrors()) {
            return "redirect:/member/id/find";
        }

        Member member = memberService.findId(findIdForm.getName(), findIdForm.getEmail());

        if (member == null) {
            result.reject("findIdFail", "가입 된 회원이 존재하지 않습니다.");
            redirect.addFlashAttribute("findIdFailMessage", "가입 된 회원이 존재하지 않습니다.");
            return "redirect:/member/login";
        }

        redirect.addAttribute("findId", member.getUserId());
        return "redirect:/member/id/find/show";
    }

    @GetMapping("/member/id/find/show")
    public String showId(@RequestParam String findId,
                         Model model) {

        model.addAttribute("findId", findId);
        return "members/showFindIdForm";
    }

    @GetMapping("/member/password/find")
    public String findPassword(Model model) {
        model.addAttribute("findPasswordForm", new FindPasswordForm());
        return "members/findPasswordForm";
    }

    @PostMapping("/member/password/find")
    public String changePasswordInfo(@Valid FindPasswordForm findPasswordForm,
                                     RedirectAttributes redirect,
                                     BindingResult result) {

        if (result.hasErrors()) {
            return "redirect:/member/password/find";
        }

        if (memberService.findByUserIdAndName(findPasswordForm.getUserId(), findPasswordForm.getName()) == null) {
            result.reject("findPasswordFail", "가입 된 회원이 존재하지 않습니다.");
            redirect.addFlashAttribute("findPasswordFailMessage", "가입 된 회원이 존재하지 않습니다.");
            return "redirect:/member/login";
        }

        redirect.addAttribute("userId", findPasswordForm.getUserId());
        redirect.addAttribute("name", findPasswordForm.getName());
        return "redirect:/member/password/change";
    }

    @GetMapping("/member/password/change")
    public String changePasswordForm(@RequestParam String userId,
                                     @RequestParam String name,
                                     Model model) {

        ChangePasswordForm changePasswordForm = new ChangePasswordForm();
        changePasswordForm.setUserId(userId);
        changePasswordForm.setName(name);
        model.addAttribute("changePasswordForm", changePasswordForm);
        return "members/changePasswordForm";
    }

    @PostMapping("/member/password/change")
    public String changePassword(@RequestParam String userId,
                                 @RequestParam String name,
                                 @Valid ChangePasswordForm changePasswordForm,
                                 BindingResult result,
                                 RedirectAttributes redirect) {

        if (result.hasErrors()) {
            redirect.addAttribute("userId", userId);
            redirect.addAttribute("name", name);
            return "redirect:/member/password/change";
        }

        if (!changePasswordForm.getPassword().equals(changePasswordForm.getCheckPassword())) {
            result.reject("changePasswordFailMessage", "비밀번호가 일치하지 않습니다.");
            redirect.addFlashAttribute("changePasswordFailMessage", "비밀번호가 일치하지 않습니다.");
            redirect.addAttribute("userId", userId);
            redirect.addAttribute("name", name);
            return "redirect:/member/password/change";
        }

        memberService.changePassword(userId, name, changePasswordForm.getPassword());
        redirect.addFlashAttribute("changePasswordMessage", "비밀번호를 변경하였습니다.");
        return "redirect:/member/login";
    }

    @GetMapping("/member/profile")
    public String profile(Principal principal, Model model) {
        MemberDto memberDto = memberService.findMemberInfo(principal.getName());
        model.addAttribute("member", memberDto);
        return "members/profile";
    }
}
