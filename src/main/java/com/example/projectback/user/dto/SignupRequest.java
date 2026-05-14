package com.example.projectback.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 4, max = 30, message = "아이디는 4자 이상 30자 이하로 입력해주세요.")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하로 입력해주세요.")
    private String password;

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
    private String name;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(max = 50, message = "닉네임은 50자 이하로 입력해주세요.")
    private String nickname;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 255, message = "이메일은 255자 이하로 입력해주세요.")
    private String email;

    @Pattern(regexp = "^$|^[0-9]{2,3}-?[0-9]{3,4}-?[0-9]{4}$", message = "전화번호는 숫자 또는 하이픈 형식으로 입력해주세요.")
    private String phoneNumber;

    @NotBlank(message = "성별을 입력해주세요.")
    @Pattern(regexp = "^[MF]$", message = "성별은 M 또는 F만 입력할 수 있습니다.")
    private String gender;

    @NotBlank(message = "회차를 입력해주세요.")
    @Size(max = 50, message = "회차는 50자 이하로 입력해주세요.")
    private String cohort;
}
