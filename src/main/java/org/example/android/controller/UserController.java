package org.example.android.controller;

import org.example.android.common.R;
import org.example.android.entity.User;
import org.example.android.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public R<String> register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public R<String> login(@RequestParam("name") String name, @RequestParam("password") String password) {
        return userService.login(name, password);
    }
}
