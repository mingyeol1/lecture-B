package com.example.lecture_B.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
@Setter
@ToString
public class CustomUser extends User {

    private Long id;
    private String userId;
    private String userPw;
    private String nickname;
    private String email;
    private boolean del;

    public CustomUser(Long id,String username, String password, String email, String nickname, boolean del,
             Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;   //식별자로 나중에 추가했음.
        this.userId = username; //유저의 로그인 아이디.
        this.userPw = password; // 패스워드
        this.nickname = nickname;
        this.email = email;
        this.del = del;
    }

    public String getName() {return this.userId;}
}
