package com.example.lecture_B.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RootConfig {

    //modelMapper 라이브러리를 이용한 DTO변환.
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)  //필드 이름을 기준으로 매핑설정.
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)  //private 필드에서도 접근하여 매핑
                .setMatchingStrategy(MatchingStrategies.STRICT);
        //(MatchingStrategies.LOOSE); STRICT와 LOOSE의 차이점?
        //STRICT : 필드 이름과 타입이 정확이 일치해야 매핑이 이루어짐.
        //LOOSE : 필드 이름이 유사하거나 관련성이 있으면 매핑이 이루어짐. 의도하지 않은 매핑이 발생할 수 있음.
        return modelMapper;
    }

}
