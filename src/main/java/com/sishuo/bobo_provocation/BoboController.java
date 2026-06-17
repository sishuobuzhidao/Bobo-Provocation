package com.sishuo.bobo_provocation;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // 🌟 跨域金牌：允许前端网页无阻碍地连上来
public class BoboController {

    public BoboGame game;

    @PostMapping("/analyzeMove")
    public StatusDTO handleMove(@RequestBody Map<String, Object> requestBody) {

        System.out.println();
        // System.out.println("----------------------------------------");
        // 获取前端传来的数据
        Boolean isGameStarted = Boolean.parseBoolean(requestBody.get("gameStarted").toString());
        Integer p1move = Integer.parseInt(requestBody.get("move").toString());
        
        // 在后端控制台打印
        if (p1move >= 0 && p1move <= 14) {
            System.out.println("Player1（前端）出招：" + p1move + ":" + Player.moves[p1move]);
        } else {
            System.out.println("Player1（前端）本回合没有出招");
        }

        if (!isGameStarted) {
            // 测试招数对应使用的
            // 随机出招（暂时0-14随机数）
            Random r = new Random();
            int computerMove = r.nextInt(0, 15);
            System.out.println("Player2（电脑）出招：" + computerMove + ":" + Player.moves[computerMove]);
            
            int[] result = Player.calculateState(p1move, computerMove);
            return new StatusDTO(computerMove, Player.analyzeState(result));

        } else {
            // isGameStarted = true: 游戏已经开始，不能乱出招
            // 返回BoboGame类里方法返回的StatusDTO
            return this.game.startOneNewRound(p1move);
        }
        
    }

    @PostMapping("/startGame")
    public StatusDTO startGame(@RequestBody Map<String, String> requestBody) {
        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println("Player1（前端）目前状态：");
        // 获取前端传来的数据
        String message = requestBody.get("startGame");

        if (message.equals("1")) {
            this.game = new BoboGame();
            StatusDTO status = this.game.runNewGame();
            return status;
        } else {
            return null;
        }
    }
}
