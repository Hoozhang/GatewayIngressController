package com.zhao.health;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class HealthController {

    @RequestMapping("/health")
    public String health(){
        return "hello, I'm healthy! " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
