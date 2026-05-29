package ru.nuclearius.finam.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ClientForwardController {

    @GetMapping(value = {"/{path:^(?!api).*}/**/{path:[^\\.]*}", "/{path:^(?!api)[^\\.]*}"})
    public String forward(@PathVariable String path) {
        return "forward:/";
    }
}
