package ru.yandex.practicum.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.model.User;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping
    public String test() {
        return "test";
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            User user = new User();
            user.setId(i);
            user.setFirstName("UserName #" + i);
            user.setLastName("LastName #" + i);
            user.setAge(25);
            user.setActive(true);

            users.add(user);
        }

        return users;
    }
}
