package org.example.android.service;

import org.example.android.common.R;
import org.example.android.entity.User;
import org.example.android.mapper.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public R<String> register(User user) {
        // 检查用户名是否已存在
        if (userRepository.findByName(user.getName()).isPresent()) {
            return R.error("用户名已存在");
        }

        // 对密码进行加密处理
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 保存新用户
        userRepository.save(user);

        return R.success("注册成功");
    }

    public R<String> login(String name, String password) {
        // 尝试通过用户名查找用户
        Optional<User> userOptional = userRepository.findByName(name);
        if (!userOptional.isPresent()) {
            return R.error("用户名不存在");
        }

        User user = userOptional.get();
        // 验证密码
        if (passwordEncoder.matches(password, user.getPassword())) {
            return R.success("登录成功");
        } else {
            return R.error("密码错误");
        }
    }
}
