package com.example.canteen.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "学号不能为空")
    @Size(min = 4, max = 32, message = "学号长度应在 4-32 之间")
    @Pattern(regexp = "^[0-9A-Za-z]+$", message = "学号仅支持数字和字母")
    private String studentNo;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度应在 6-64 之间")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过 50")
    private String nickname;

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
