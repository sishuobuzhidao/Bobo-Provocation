package com.sishuo.bobo_provocation;

public class BoboGame {
    public Player p1;
    public Player p2;
    public ComputerMove cm;
    public int aiDifficulty;
    public int moveCount;

    public BoboGame() {
        this.p1 = new Player();
        this.p2 = new Player();
        this.cm = new ComputerMove();
        this.aiDifficulty = -1;
        this.moveCount = 0;
    }

    public StatusDTO runNewGame(boolean isUltraHardModeOn, int aiDifficulty) {
        // 这里p1是玩家，p2是随机出招的电脑
        // String movesAndIndices = "0:挑衅 | 1:防御 | 2:左避 | 3:右避 | 4:上勾拳 | 5:左勾拳 | 6:右勾拳 | 7:直拳 | 8:反弹 | 9:小猩猩 | 10:双层防御 | 11:冰冻 | 12:大猩猩 | 13:致命一击 | 14:解雇";
        this.moveCount = 0;
        this.aiDifficulty = aiDifficulty;
        p1.reset();
        p2.reset();
        StatusDTO status = p1.showStatus(); // 初始化status
        cm.updateP1(-2, null);
        System.out.print("\n等待Player1（前端）选择招式...");
        return status;
    }

    public int makeP2Move() {
        if (this.aiDifficulty == 0) {
            return p2.makeRandomMove(true);
        } else if (this.aiDifficulty == 1) {
            return p2.makeWeightedMove();
        } else if (this.aiDifficulty == 2) {
            return p2.makeAdjustedWeightedMove(this.cm);
        } else {
            return p2.makeMove(); // Should not happen
        }
    }

    public StatusDTO startOneNewRound(int p1move, boolean isUltraHardModeOn) {
        System.out.println();
        this.moveCount++;

        if (p1move >= 0 && p1move <= 14) {
            // ultra hard mode is not on
            System.out.println("Player1（前端）出招：" + p1move + ":" + Player.moves[p1move]);

            if (isUltraHardModeOn && !p1.getAvailableMovesArray().contains(p1move)) {
                // 困难模式下玩家选择了不合法招式
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
        if (p2move != -1) {
            System.out.println("Player2（电脑）出招：" + p2move + ":" + Player.moves[p2move]);
        } else {
            System.out.println("Player2（电脑）本回合被冰冻，无法出招");
        }
         
        if (p1move != -1) {
            p1.analyzeMove(p1move);
        }

        if (p2move != -1) {
            p2.analyzeMove(p2move);
        }

        Player.updateState(p1, p2, p1move, p2move);

        StringBuilder sb = new StringBuilder();
        // analyzeState(p1, p2, roundResult) 会修改sb得到指针修改参数的效果
        boolean shouldContinue = Player.analyzeState(p1, p2, sb);

        if (!shouldContinue) {
            // 游戏分出胜负结束
            System.out.println("本局一共" + moveCount + "回合！");
            System.out.println("---------------------------------------");
            return new StatusDTO(null, -1, null, p1move, p2move, sb.toString(), false);
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

        if (p1.getState() == 3) {
            // 减少解雇状态一回合
            p1.minusOneLayoff();
        }

        System.out.print("\n等待Player1（前端）选择招式...");
        return newStatus;
    }
}
