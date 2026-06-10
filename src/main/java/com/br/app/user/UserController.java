package com.br.app.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
public class UserController {

    @RequestMapping("/created")
    public String requestMethodName(@RequestParam String param) {
        return new String();
    }

    @RequestMapping("/get{userId}")
    public String getUser(@RequestParam String param) {
        return new String();
    }

    @RequestMapping("/getAll")
    public String getAllUsers(@RequestParam String param) {
        return new String();
    }

    @RequestMapping("/update")
    public String updateUser(@RequestParam String param) {
        return new String();
    }
    
    

}
