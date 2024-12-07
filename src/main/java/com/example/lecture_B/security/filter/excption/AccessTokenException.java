package com.example.lecture_B.security.filter.excption;


import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class AccessTokenException extends RuntimeException{

    TOKEN_ERROR token_error;

    public enum TOKEN_ERROR{
        UNACCEPT(401, "null or short"),
        BADTYPE(401, "토큰 타입이  Bearer 인데요?"),
        MALFORM(403, "malform 토큰인데요????"),
        BADSIGN(403, "안좋은시그니쳐 토큰인데요??"),
        EXPIRED(403, "만료된 토큰이라는데요?");

        private int status;
        private String msg;

        TOKEN_ERROR(int status, String msg){
            this.status = status;
            this.msg = msg;
        }
        public  int getStatus(){
            return  this.status;
        }

        public String getMsg()
        {return this.msg;}
    }



    public AccessTokenException(TOKEN_ERROR error){
        super(error.name());
        this.token_error = error;
    }

    public void sendResponseError(HttpServletResponse response){
        response.setStatus(token_error.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Gson gson = new Gson();

        String responseStr = gson.toJson(Map.of("msg",token_error.getMsg(), "time", new Date()));

        try{
            response.getWriter().println(responseStr);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
