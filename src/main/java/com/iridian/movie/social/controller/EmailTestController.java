package com.iridian.movie.social.controller;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iridian.movie.social.service.EmailTestService;

@RestController
@RequestMapping("/api/test-email")
public class EmailTestController {

    private final EmailTestService emailTestService;

    public EmailTestController(EmailTestService emailTestService) {
        this.emailTestService = emailTestService;
    }

    @GetMapping
    public String sendEmail(@RequestParam String to) {
        emailTestService.sendTestEmail(to);
        return "✅ Test email sent to " + to;
    }
      @PostMapping
    public String sendEmailPost(@RequestBody Map<String, String> body) {
        String to = body.get("to");
        emailTestService.sendTestEmail(to);
        return "✅ Test email sent to " + to;
    }
}
