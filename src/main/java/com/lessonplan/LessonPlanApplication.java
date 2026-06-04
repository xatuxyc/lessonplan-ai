package com.lessonplan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.lessonplan"})
@MapperScan(basePackages = {"com.lessonplan.mappers"})
@EnableAsync
public class LessonPlanApplication {
    public static void main(String[] args) {
        SpringApplication.run(LessonPlanApplication.class, args);
    }
}
