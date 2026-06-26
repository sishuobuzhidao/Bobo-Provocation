package com.sishuo.bobo_provocation;

// This class describes a running BoboGame
// contains two Player p1 and p2, and a ComputerMove cm

public class BoboGame {
    public Player p1; // p1 is always the frontend user
    public Player p2; // p2 is always the backend computer
    public ComputerMove cm;
    public boolean isUltraHardModeOn;
    public int aiDifficulty;
    public int moveCount;

    public static final int EASY_DIFFICULTY = 0;
    public static final int NORMAL_DIFFICULTY = 1;
    public static final int HARD_DIFFICULTY = 2;

    public BoboGame() {
        this.p1 = new Player();
        this.p2 = new Player();
        this.cm = new ComputerMove();
        this.isUltraHardModeOn = false;
        this.aiDifficulty = -1;
        this.moveCount = 0;
    }

    // runNewGame(isUltraHardModeOn, aiDifficulty) runs a new requested Bobo Provocation game
    // updates this.isUltraHardModeOn and this.aiDifficulty
    public StatusDTO runNewGame(boolean isUltraHardModeOn, int aiDifficulty) {
        // 这里p1是玩家，p2是随机出招的电脑
        this.moveCount = 0;
        this.isUltraHardModeOn = isUltraHardModeOn;
        this.aiDifficulty = aiDifficulty;
        p1.reset();
        p2.reset();
        StatusDTO status = p1.showStatus(); // 初始化status
        cm.updateP1(Player.MOVE_NULL, null);
        System.out.print("\n等待Player1（前端）选择招式...");
        return status;
    }

    // makeP2Move() returns a moveID depending on aiDifficulty
    public int makeP2Move() {
        if (this.aiDifficulty == BoboGame.EASY_DIFFICULTY) {
            return p2.makeRandomMove(true);
        } else if (this.aiDifficulty == BoboGame.NORMAL_DIFFICULTY) {
            return p2.makeWeightedMove();
        } else if (this.aiDifficulty == BoboGame.HARD_DIFFICULTY) {
            return p2.makeAdjustedWeightedMove(this.cm);
        } else {
            return p2.makeMove(); // Should not happen
        }
    }

    // startOneNewRound(p1move, isUltraHardModeOn) first prints the frontend user's choice to console
    // then make the computer's move and calculates the round result
    // finally, prints the current information of the frontend user after this round
    // and returns to frontend a StatusDTO object
    public StatusDTO startOneNewRound(int p1move) {
        System.out.println();
        this.moveCount++;

        if (p1move >= Player.MOVE_PROVOCATION && p1move <= Player.MOVE_LAYOFF) {
            // 0 <= p1move <= 14
            System.out.println("Player1（前端）出招：" + p1move + ":" + Player.moves[p1move]);

            if (isUltraHardModeOn && !p1.getAvailableMovesArray().contains(p1move)) {
                // frontend chose an illegal move (in ultra hard mode)
                p1move = p1.makeRandomMove(false);
                System.out.println("检测到选择招式不合法，已使用随机合法招式替换。");
                System.out.println("招式被替换为：" + p1move + ":" + Player.moves[p1move]);
            }
        } else {
            System.out.println("Player1（前端）本回合没有出招");
        }

        System.out.println();
        System.out.println("Player2（电脑）目前状态：");
        int p2move = makeP2Move();
        if (p2move != Player.MOVE_FROZEN_NULL) {
            System.out.println("Player2（电脑）出招：" + p2move + ":" + Player.moves[p2move]);
        } else {
            System.out.println("Player2（电脑）本回合被冰冻，无法出招");
        }
         
        if (p1move >= Player.MOVE_PROVOCATION && p1move <= Player.MOVE_LAYOFF) {
            p1.analyzeMove(p1move);
        }

        if (p2move >= Player.MOVE_PROVOCATION && p2move <= Player.MOVE_LAYOFF) {
            p2.analyzeMove(p2move);
        }

        Player.updateState(p1, p2, p1move, p2move);

        StringBuilder sb = new StringBuilder();
        // analyzeState(p1, p2, roundResult) 会修改sb得到指针修改参数的效果
        boolean shouldContinue = Player.analyzeState(p1, p2, sb);

        if (!shouldContinue) {
            // 游戏分出胜负结束
            String moveCountReveal = ("本局一共" + moveCount + "回合！");
            sb.append("\n" + moveCountReveal);
            System.out.println(moveCountReveal);
            System.out.println("---------------------------------------");
            return new StatusDTO(null, Player.LOST, null, p1move, p2move, sb.toString(), false);
        }

        // 判断完成后，为下一回合做准备
        System.out.println("---------------------------------------");
        System.out.println("Player1（前端）目前状态：");
        StatusDTO newStatus = p1.showStatus();
        newStatus.setP1moveID(p1move);
        newStatus.setP2moveID(p2move);
        newStatus.setRoundResult(sb.toString());
        newStatus.setShouldContinue(shouldContinue);

        cm.updateP1(p1move, newStatus);

        if (p1.getState() == Player.LAYOFFED) {
            // 减少解雇状态一回合
            p1.minusOneLayoff();
        }

        System.out.print("\n等待Player1（前端）选择招式...");
        return newStatus;
    }
}
