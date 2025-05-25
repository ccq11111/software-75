package financialAI.ysll;

import org.springframework.ai.chat.ChatClient; // 正确路径import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class Controller {

    private final ChatClient chatClient;

    // 使用 @Autowired 注解自动注入 ChatClient
    @Autowired
    public Controller(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        // 1.0.0+ 更简化的调用
        return chatClient.call(message);
    }
}