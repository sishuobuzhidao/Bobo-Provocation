package com.sishuo.bobo_provocation;

// import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Player p1 = new Player();
        Player p2 = new Player();

        while(true) {
            System.out.println("请选择游玩模式：");
            System.out.println("1: 人机互打（只观战）");
            System.out.println("2：人机对战（电脑为Player1）");
            System.out.println("3：人人对战（同时操控两人）");
            System.out.println("输入其他内容退出");
            int choice = Player.sc.nextInt();

            if (choice < 1 || choice > 3) {
                break;
            } else {
                runGame(choice, p1, p2);
            }
        }

        
        Player.sc.close();
    }

    public static void runGame(int choice, Player p1, Player p2) {
        String movesAndIndices = "0:挑衅 | 1:防御 | 2:左避 | 3:右避 | 4:上勾拳 | 5:左勾拳 | 6:右勾拳 | 7:直拳 | 8:反弹 | 9:小猩猩 | 10:双层防御 | 11:冰冻 | 12:大猩猩 | 13:致命一击 | 14:解雇";
        int moveCount = 0;

        p1.reset();
        p2.reset();

        while(true) {
            System.out.println("---------------------------------------");
            moveCount++;
            System.out.println(movesAndIndices);
            System.out.println();

            int p1move;
            int p2move;

            if (choice == 1 || choice == 2) {
                System.out.println("Player1（电脑）已出招完毕");
                // p1move = p1.makeRandomMove();
                p1move = p1.makeWeightedMove();
            } else {
                System.out.println("Player1（你）出招: 请输入在可选列表中的 0-14 数字");
                p1move = p1.makeMove();
            }
            
            System.out.println();

            if (choice == 1) {
                System.out.println("Player2（电脑）已出招完毕");
                // p2move = p2.makeRandomMove();
                p2move = p2.makeWeightedMove();
            } else {
                System.out.println("Player2（你）出招: 请输入在可选列表中的 0-14 数字");
                p2move = p2.makeMove();
            }

            
            if (p1move != -1) {
                p1.analyzeMove(p1move);
            }

            if (p2move != -1) {
                p2.analyzeMove(p2move);
            }


            Player.updateState(p1, p2, p1move, p2move);
            if (Player.analyzeState(p1, p2)) {
                System.out.println("输入任意继续：");
                Player.sc.next();
                continue;
            } else {
                System.out.println("本局一共" + moveCount + "回合！");
                System.out.println("---------------------------------------");
                break;
            }
        }
    }
}