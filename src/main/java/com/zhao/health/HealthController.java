package com.zhao.health;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @RequestMapping("/health")
    public String health(){
        return "hello, I'm healthy! " + System.currentTimeMillis();
    }
}
