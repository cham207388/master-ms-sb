package com.abcham.message;

import com.abcham.message.service.EmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class MessageApplicationTests {

    @MockitoBean
    private EmailSender emailSender;

    @Test
    void contextLoads() {
    }
}
