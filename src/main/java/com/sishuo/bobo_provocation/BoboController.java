package com.sishuo.bobo_provocation;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BoboController {

    public BoboGame game = new BoboGame();
    private Random r = new Random();

    // handleMove(requestBody) accepts a POST json package from frontend
    // called during a game, returns a StatusDTO calculated by BoboGame.java
    // if not called during a game (for testing), returns a randInt(0, 15) in a StatusDTO and returns
    @PostMapping("/analyzeMove")
    public StatusDTO handleMove(@RequestBody Map<String, Object> requestBody) {

        System.out.println();
        // 获取前端传来的数据
        Boolean isGameStarted = Boolean.parseBoolean(requestBody.get("gameStarted").toString());
        Integer p1move = Integer.parseInt(requestBody.get("move").toString());

        if (Boolean.FALSE.equals(isGameStarted)) {
            if (p1move >= Player.MOVE_PROVOCATION && p1move <= Player.MOVE_LAYOFF) {
                System.out.println("Player1（前端）出招：" + p1move + ":" + Player.moves[p1move]);
            } else {
                System.out.println("Player1（前端）本回合没有出招");
            }

            // 测试招数对应使用的
            // 随机出招（暂时0-14随机数）
            int computerMove = r.nextInt(Player.MOVE_PROVOCATION, Player.MOVE_LAYOFF + 1);
            System.out.println("Player2（电脑）出招：" + computerMove + ":" + Player.moves[computerMove]);
            
            int[] result = Player.calculateState(p1move, computerMove);
            return new StatusDTO(computerMove, Player.analyzeState(result));

        } else {
            // isGameStarted = true: 游戏已经开始，不能乱出招
            // 返回BoboGame类里方法返回的StatusDTO
            return this.game.startOneNewRound(p1move);
        }
        
    }

    // startGame(requestBody) accepts a POST json package from frontend
    // called to start a game, includes the difficulty of the AI and the mode of the game (normal/ultra)
    // returns a StatusDTO to frontend
    @PostMapping("/startGame")
    public StatusDTO startGame(@RequestBody Map<String, Object> requestBody) {
        System.out.println();
        
        // 获取前端传来的数据
        String message = requestBody.get("startGame").toString();
        Integer aiDifficulty = Integer.parseInt(requestBody.get("aiDifficulty").toString());

        if (message.equals("normal")) {
            System.out.println("\n前端发起新一局普通模式游戏");
            System.out.println("游戏难度代码：" + aiDifficulty + "\n");
            System.out.println("----------------------------------------");
            System.out.println("Player1（前端）目前状态：");
            return this.game.runNewGame(false, aiDifficulty);
        } else if (message.equals("ultra")) {
            System.out.println("\n前端发起新一局困难模式游戏");
            System.out.println("游戏难度代码：" + aiDifficulty + "\n");
            System.out.println("----------------------------------------");
            System.out.println("Player1（前端）目前状态：");
            return this.game.runNewGame(true, aiDifficulty);
        } else {
            return null;
        }
    }
}
