package com.sishuo.bobo_provocation;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // 🌟 跨域金牌：允许前端网页无阻碍地连上来
public class BoboController {

    // 1. 这是一个标准的 POST 接口，专门监听 http://localhost:8080/api/bobo
    @PostMapping("/bobo")
    public Map<String, String> handleProvocation(@RequestBody Map<String, String> requestBody) {
        
        // 2. 从前端发来的 JSON 里面，把名叫 "provocation" 的那句话捞出来
        String userMessage = requestBody.get("provocation");
        
        // 3. 在后端控制台打印一下，假装我们收到了导弹信号
        System.out.println("\n--- [雷达警报] 接收到前端挑衅弹头！ ---");
        System.out.println("内容是: " + userMessage);
        System.out.println("----------------------------------------\n");

        /* 4. 💡 核心联动枢纽：
           你现在可以在这里直接调用你 App 里的游戏核心逻辑了！
           比如：String boboResponse = App.getBoboReply(userMessage);
           目前我们先写死一个随机嘴臭，用来跟前端调通联调：
        */
        String boboReply = "波波冷笑了一声说：['" + userMessage + "']？就这？你还是先回去把 STAT 230 学明白吧！";

        // 5. 把波波的回击打包成 JSON，啪的一声甩回给前端
        return Map.of("reply", boboReply);
    }

    @PostMapping("/analyzeMove")
    public Map<String, String> handleMove(@RequestBody Map<String, String> requestBody) {

        System.out.println();
        System.out.println("----------------------------------------");
        // 获取前端传来的数据
        String message = requestBody.get("move");
        Integer moveIndex = Integer.parseInt(message);
        
        // 在后端控制台打印
        System.out.println("Player1（前端）出招：" + message + ":" + Player.moves[moveIndex]);

        // 随机出招（暂时0-14随机数）
        Random r = new Random();
        int computerMove = r.nextInt(0, 15);
        System.out.println("Player2（电脑）出招：" + computerMove + ":" + Player.moves[computerMove]);
        
        int[] result = Player.calculateState(moveIndex, computerMove);

        // 回给前端
        return Map.of(
            "computerMoveID", Integer.valueOf(computerMove).toString(), 
            "roundResult", Player.analyzeState(result)
        );
    }
}
