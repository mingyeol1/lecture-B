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

    private String userId;
    private String userPw;
    private String nickname;
    private String email;
    private boolean del;

    public CustomUser(String username, String password, String email, String nickname, boolean del,
             Collection<? extends GrantedAuthority> authorities

                      ) {
        super(username, password, authorities);
        this.userId = username;
        this.userPw = password;
        this.nickname = nickname;
        this.email = email;
        this.del = del;
    }

    public String getName() {return this.userId;}
}
