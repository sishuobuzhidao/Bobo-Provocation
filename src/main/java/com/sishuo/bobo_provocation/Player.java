package com.sishuo.bobo_provocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Scanner;

public class Player {
    public static Scanner sc = new Scanner(System.in);

    private int provocations_accu;
    private int provocations_unused;
    private int defenses;

    private int freeze_counter;
    private int lgorilla_counter;
    private int critical_counter;
    private int layoff_counter;

    private int layoff_remaining_moves;

    private int state;
    private ArrayList<Integer> available;

    private static int[][][] results;
    public static String[] moves = {"挑衅", "防御", "左避", "右避", "上勾拳", "左勾拳", "右勾拳", "直拳", "反弹", "小猩猩", "双层防御", "冰冻", "大猩猩", "致命一击", "解雇"};
    private static int[] moveWeights = {10, 9, 4, 4, 5, 1, 1, 12, 10, 25, 20, 50, 100, 200, 1000};

    private static String[] failMessages = {"电脑获胜！你输了！再来一局？", "电脑赢了！祝您下次好运！", "你输了！游戏结束！", "很抱歉，你输了！再来一局？"};
    private static String[] normalMessages = {"无事发生，游戏继续。", "游戏仍在继续...", "游戏仍胜负未分!", "游戏继续，请选择你的招法。"};
    private static String[] winMessages = {"游戏结束！恭喜你，你赢了！", "恭喜你在这局游戏中取得胜利！", "恭喜，你赢了！再来一局？", "你成功打败了电脑！恭喜！"};

    static {
        // 前两个索引对应 1号玩家 和 2号玩家 的招法 (0-14)
        // 返回的数组：index 0 是1号玩家状态码，index 1 是2号玩家状态码
        // 状态码：-1=输, 0=继续(抵消/无事发生), 1=赢, 2=被冰冻, 3=被解雇
        int[][][] results0 = {
            // 0: 挑衅
            {
                {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {-1, 1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 1: 防御
            {
                {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 2: 左避
            {
                {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 3: 右避
            {
                {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 4: 上勾拳
            {
                {0, 0}, {1, -1}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {-1, 1}, {-1, 1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 5: 左勾拳
            {
                {0, 0}, {0, 0}, {1, -1}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {-1, 1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 6: 右勾拳
            {
                {0, 0}, {0, 0}, {0, 0}, {1, -1}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {-1, 1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 7: 直拳
            {
                {1, -1}, {0, 0}, {0, 0}, {0, 0}, {1, -1}, {1, -1}, {1, -1}, {0, 0}, {-1, 1}, {-1, 1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 8: 反弹
            {
                {0, 0}, {0, 0}, {0, 0}, {0, 0}, {1, -1}, {0, 0}, {0, 0}, {1, -1}, {0, 0}, {1, -1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 9: 小猩猩
            {
                {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {-1, 1}, {0, 0}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 10: 双层防御
            {
                {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {3, 0}
            },
            // 11: 冰冻
            {
                {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 0}, {0, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 12: 大猩猩
            {
                {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {0, 0}, {1, -1}, {0, 0}, {-1, 1}, {3, 0}
            },
            // 13: 致命一击
            {
                {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {0, 0}, {3, 0}
            },
            // 14: 解雇
            {
                {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 0}
            }
        };
        Player.results = results0;
    }

    public Player() {
        provocations_accu = 0;
        provocations_unused = 0;
        defenses = 0;

        freeze_counter = 0;
        lgorilla_counter = 0;
        critical_counter = 0;
        layoff_counter = 0;

        layoff_remaining_moves = 0;

        state = 0;
        available = new ArrayList<>();
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        if (state != -1 && state != 0 && state != 1 && state != 2 && state != 3) {
            throw new RuntimeException("Invalid state number!");
            // return;
        }
        this.state = state;
    }

    public void setLayoff() {
        this.state = 3;
        this.layoff_remaining_moves = 5;
    }

    public boolean minusOneLayoff() {
        // Returns true if the layoff status ends
        this.layoff_remaining_moves--;
        this.state = 3;
        if (this.layoff_remaining_moves == 0) {
            this.state = 0;
            return true;
        }
        return false;
    }

    public int getLayoffRemaining() {
        return this.layoff_remaining_moves;
    }

    public void reset() {
        provocations_accu = 0;
        provocations_unused = 0;
        defenses = 0;

        freeze_counter = 0;
        lgorilla_counter = 0;
        critical_counter = 0;
        layoff_counter = 0;

        state = 0;
        available.clear();

        this.layoff_remaining_moves = 0;
    }

    public static int[] calculateState(int firstPlayer, int secondPlayer) {
        // -1 Lost; 0 Continue play; 1 Won; 2 Freezed; 3 Layoffed
        return results[firstPlayer][secondPlayer];
    }

    public static String updateState(Player p1, Player p2, int firstPlayer, int secondPlayer) {
        System.out.println();
        int[] res = null;
        String result;
        if (p1.getState() == 2) {
            res =  results[0][secondPlayer];
            p1.setState(res[0]);
            p2.setState(res[1]);
            result = "Player1本回合被冰冻，无法出招\n";
            result += ("Player2出招: " + secondPlayer + ":" + moves[secondPlayer]);
            return result;
        } else if (p2.getState() == 2) {
            res =  results[firstPlayer][0];
            p1.setState(res[0]);
            p2.setState(res[1]);
            result = ("Player1出招: " + firstPlayer + ":" + moves[firstPlayer]);
            result += ("\nPlayer2本回合被冰冻，无法出招");
        } else if (p1.getState() == 3) {
            res = results[1][secondPlayer];
            p1.setState(res[0] == -1 ? -1 : 3);
            p2.setState(res[1]);
            result = ("Player1本回合被解雇，只能出 1:防御\n");
            result += ("Player2出招: " + secondPlayer + ":" + moves[secondPlayer]);
        } else if (p2.getState() == 3) {
            res = results[firstPlayer][1];
            p2.setState(res[1] == -1 ? -1 : 3);
            p1.setState(res[0]);
            result = ("Player1出招: " + firstPlayer + ":" + moves[firstPlayer]);
            result += ("\nPlayer2本回合被解雇，只能出 1:防御");
        } else {
            res =  results[firstPlayer][secondPlayer];
            if (secondPlayer == 14 && firstPlayer != 14) {
                p1.setLayoff();
            } else if (firstPlayer == 14 && secondPlayer != 14) {
                p2.setLayoff();
            }
            p1.setState(res[0]);
            p2.setState(res[1]);
            result = ("Player1出招: " + firstPlayer + ":" + moves[firstPlayer]);
            result += ("\nPlayer2出招: " + secondPlayer + ":" + moves[secondPlayer]);
        }
        System.out.println(result);
        System.out.println();
        return result;
    }

    public static String analyzeState(int[] states) {
        // -1 Lost; 0 Continue play; 1 Won; 2 Freezed; 3 Layoffed
        String result;
        if (states[0] == -1) {
            result = ("Player2 获胜");
        } else if (states[0] == 1) {
            result = ("Player1 获胜");
        } else if (states[0] == 2) {
            result = ("Player1 被冰冻一回合，游戏继续");
        } else if (states[1] == 2) {
            result = ("Player2 被冰冻一回合，游戏继续");            
        } else if (states[0] == 3) {
            result = ("Player1 被解雇，五回合内只能出防御，游戏继续");            
        } else if (states[1] == 3) {
            result = ("Player2 被解雇，五回合内只能出防御，游戏继续");            
        } else {
            result = ("游戏继续");
        }
        System.out.println(result);
        return result;
    }

    public static boolean analyzeState(Player p1, Player p2) {
        // -1 Lost; 0 Continue play; 1 Won; 2 Freezed; 3 Layoffed
        if (p1.getState() == -1 || p2.getState() == 1) {
            System.out.println("Player2 获胜");
            return false;
        } else if (p1.getState() == 1 || p2.getState() == -1) {
            System.out.println("Player1 获胜");
            return false;
        } else if (p1.getState() == 2) {
            System.out.println("Player1 被冰冻一回合，下一回合无法出招，游戏继续");
            return true;
        } else if (p2.getState() == 2) {
            System.out.println("Player2 被冰冻一回合，下一回合无法出招，游戏继续");  
            return true;          
        } else if (p1.getState() == 3) {
            System.out.println("Player1 被解雇，还剩" + p1.getLayoffRemaining() + "回合（从下一回合算起），游戏继续");   
            return true;         
        } else if (p2.getState() == 3) {
            System.out.println("Player2 被解雇，还剩" + p2.getLayoffRemaining() + "回合（从下一回合算起），游戏继续");    
            return true;        
        } else {
            System.out.println("游戏继续");
            return true;
        }
    }

    public static boolean analyzeState(Player p1, Player p2, StringBuilder sb) {
        // -1 Lost; 0 Continue play; 1 Won; 2 Freezed; 3 Layoffed
        if (p1.getState() == -1 || p2.getState() == 1) {
            sb.append(getRandomMessage(-1));
            System.out.println("Player2 获胜");
            return false;
        } else if (p1.getState() == 1 || p2.getState() == -1) {
            sb.append(getRandomMessage(1));
            System.out.println("Player1 获胜");
            return false;
        } else if (p1.getState() == 2) {
            sb.append("你被电脑冰冻了一回合！下一回合将无法出招。");
            System.out.println("Player1 被冰冻一回合，下一回合无法出招，游戏继续");
            return true;
        } else if (p2.getState() == 2) {
            sb.append("电脑被你冰冻了一回合！电脑下一回合将无法出招。");  
            System.out.println("Player2 被冰冻一回合，下一回合无法出招，游戏继续");
            return true;          
        } else if (p1.getState() == 3) {
            sb.append("你被电脑解雇了！从下一回合算起，你只能出防御" + p1.getLayoffRemaining() + "回合了。");   
            System.out.println("Player1 被解雇，还剩" + p1.getLayoffRemaining() + "回合（从下一回合算起），游戏继续");
            return true;         
        } else if (p2.getState() == 3) {
            sb.append("电脑被你解雇了！从下一回合算起，电脑只能出防御" + p2.getLayoffRemaining() + "回合！");    
            System.out.println("Player2 被解雇，还剩" + p2.getLayoffRemaining() + "回合（从下一回合算起），游戏继续");
            return true;        
        } else {
            sb.append(getRandomMessage(0));
            System.out.println("游戏继续");
            return true;
        }
    }


    public int showAvailableMoves(){
        available.clear();
        System.out.println("现在状态：累计挑衅: " + provocations_accu);
        System.out.println("现在状态：未兑换挑衅: " + provocations_unused);
        System.out.println("现在状态：未兑换防御: " + defenses);
        System.out.print("冰冻计数器：" + freeze_counter);
        System.out.print("; 大猩猩计数器：" + lgorilla_counter);
        System.out.print("; 致命一击计数器：" + critical_counter);
        System.out.println("; 解雇计数器：" + layoff_counter);

        System.out.println("当前玩家状态码：" + state);

        if (state == 2) {
            System.out.println("当前被冰冻，无法出招");
            // sc.nextInt();
            return 2;
        } else if (state == 3) {
            System.out.println("当前被解雇，只能出 1:防御");
            // sc.nextInt();
            return 3;
        }

        /* 0:挑衅 | 1:防御 | 2:左避 | 3:右避 | 4:上勾拳 | 5:左勾拳 | 6:右勾拳 | 7:直拳 |
             8:反弹 | 9:小猩猩 | 10:双层防御 | 11:冰冻 | 12:大猩猩 | 13:致命一击 | 14:解雇";
        */

        // ArrayList<Integer> available = new ArrayList<>();
        available.add(0);
        available.add(1);
        available.add(2);
        available.add(3);
        available.add(4);
        available.add(5);
        available.add(6);
        
        if (defenses >= 2) {
            available.add(10);
        }
        if (defenses >= 1) {
            available.add(8);
        }

        if (provocations_unused >= 3) {
            available.add(9);
        } 
        if (provocations_unused >= 1) {
            available.add(7);
        }

        if (freeze_counter >= 5) {
            available.add(11);
        }

        if (lgorilla_counter >= 6) {
            available.add(12);
        }

        if (critical_counter >= 7) {
            available.add(13);
        }

        if (layoff_counter >= 10) {
            available.add(14);
        }
        
        available.sort(new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
            
        });

        System.out.println("本回合可使用的招法：");
        for (int i : available) {
            System.out.print(i + ":" + moves[i] + "; ");
        }
        System.out.println();

        return 0;
    }

    // The returning Object[] has one 7-sized array in index 0, one int in index 1 and one ArrayList<Integer> in index 2
    public StatusDTO showStatus(){
        available.clear();

        int[] counters = {provocations_accu, provocations_unused, defenses, freeze_counter, lgorilla_counter, critical_counter, layoff_counter};
        System.out.println("现在状态：累计挑衅: " + provocations_accu);
        System.out.println("现在状态：未兑换挑衅: " + provocations_unused);
        System.out.println("现在状态：未兑换防御: " + defenses);
        System.out.print("冰冻计数器：" + freeze_counter);
        System.out.print("; 大猩猩计数器：" + lgorilla_counter);
        System.out.print("; 致命一击计数器：" + critical_counter);
        System.out.println("; 解雇计数器：" + layoff_counter);

        System.out.println("当前玩家状态码：" + state);

        if (state == 2) {
            System.out.println("当前被冰冻，无法出招");
            System.out.println();
            return new StatusDTO(counters, state, available);
        } else if (state == 3) {
            System.out.println("当前被解雇，只能出 1:防御");
            System.out.println();
            available.add(1);
            return new StatusDTO(counters, state, available);
        }

        /* 0:挑衅 | 1:防御 | 2:左避 | 3:右避 | 4:上勾拳 | 5:左勾拳 | 6:右勾拳 | 7:直拳 |
             8:反弹 | 9:小猩猩 | 10:双层防御 | 11:冰冻 | 12:大猩猩 | 13:致命一击 | 14:解雇";
        */

        // ArrayList<Integer> available = new ArrayList<>();
        available.add(0);
        available.add(1);
        available.add(2);
        available.add(3);
        available.add(4);
        available.add(5);
        available.add(6);
        
        if (defenses >= 2) {
            available.add(10);
        }
        if (defenses >= 1) {
            available.add(8);
        }

        if (provocations_unused >= 3) {
            available.add(9);
        } 
        if (provocations_unused >= 1) {
            available.add(7);
        }

        if (freeze_counter >= 5) {
            available.add(11);
        }

        if (lgorilla_counter >= 6) {
            available.add(12);
        }

        if (critical_counter >= 7) {
            available.add(13);
        }

        if (layoff_counter >= 10) {
            available.add(14);
        }
        
        available.sort(new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
            
        });

        System.out.println("本回合可使用的招法：");
        for (int i : available) {
            System.out.print(i + ":" + moves[i] + "; ");
        }

        return new StatusDTO(counters, 1, available);
    }

    public void updateProvocationUnused(int cost) {
        // 优先扣除已兑换的挑衅数量，再扣除未兑换的挑衅数量
        if (cost <= provocations_accu - provocations_unused) {
            // 总共的已兑换挑衅数量大于等于总花费
            return;
        } else {
            this.provocations_unused -= cost - (this.provocations_accu - this.provocations_unused);
        }
    }

    public void analyzeMove(int move) {
        /* 0:挑衅 | 1:防御 | 2:左避 | 3:右避 | 4:上勾拳 | 5:左勾拳 | 6:右勾拳 | 7:直拳 |
             8:反弹 | 9:小猩猩 | 10:双层防御 | 11:冰冻 | 12:大猩猩 | 13:致命一击 | 14:解雇";
        */
        switch (move) {
            case 0: //挑衅
                provocations_accu++;
                provocations_unused++;
                freeze_counter++;
                lgorilla_counter++;
                critical_counter++;
                layoff_counter++;
                break;
            case 1: //防御
                defenses++;
                break;
            case 7: //直拳
                provocations_unused--;
                break;
            case 8: //反弹
                defenses--;
                break;
            case 9: //小猩猩
                provocations_unused -= 3;
                break;
            case 10: //双层防御
                defenses -= 2;
                break;
            case 11: //冰冻
                updateProvocationUnused(5);
                freeze_counter = 0;
                break;
            case 12: //大猩猩
                updateProvocationUnused(6);
                freeze_counter = 0;
                lgorilla_counter = 0;
                break;
            case 13: //致命一击
                updateProvocationUnused(7);
                freeze_counter = 0;
                lgorilla_counter = 0;
                critical_counter = 0;
                break;
            case 14: //解雇
                updateProvocationUnused(10);
                freeze_counter = 0;
                lgorilla_counter = 0;
                critical_counter = 0;
                layoff_counter = 0;
                break;
            // default:
            //     break;
        }
    }
    
    public int makeMove() {
        int moving = this.showAvailableMoves();
        int move;
        // moving == 2 被冰冻； moving == 3 被解雇
        if (moving == 2) {
            sc.nextInt();
            return -1;
        } else if (moving == 3) {
            sc.nextInt();
            minusOneLayoff();
            return 1;
        } else {
            while (true) {
                move = sc.nextInt();
                if (!available.contains(move)) {
                    System.out.println("当前回合该招式不合法，请重新输入");
                } else {
                    System.out.println("你选择的招式：" + move + ":" + moves[move]);
                    break;
                }
            }
            return move;
        }
    }

    public int makeRandomMove() {
        int moving = this.showAvailableMoves();
        // moving == 2 被冰冻； moving == 3 被解雇        
        if (moving == 2) {
            return -1;
        } else if (moving == 3) {
            minusOneLayoff();
            return 1;
        } else {
            Collections.shuffle(available);
            return available.get(0);
        }
    }

    public int makeWeightedMove() {
        int moving = this.showAvailableMoves();
        // moving == 2 被冰冻； moving == 3 被解雇
        if (moving == 2) {
            return -1;
        } else if (moving == 3) {
            minusOneLayoff();
            return 1;
        } else {
            ArrayList<Integer> cumulatedAvailableMoveWeights = new ArrayList<>();
            int weight = 0;

            for (int i : this.available) {
                weight += Player.moveWeights[i];
                cumulatedAvailableMoveWeights.add(weight);
            }

            Random r = new Random();
            int randInt = r.nextInt(1, weight + 1);

            int index = Collections.binarySearch(cumulatedAvailableMoveWeights, randInt);
            if (index < 0) {
                // index = - insertionPoint - 1
                index = (index + 1) * -1;
            }

            return available.get(index);
        }
    }

    public static String getRandomMessage(int type) {
        Random r = new Random();
        int index = r.nextInt(4);
        switch (type){
            case -1: {
                return failMessages[index];
            } case 0: {
                return normalMessages[index];
            } case 1: {
                return winMessages[index];
            } default: {
                return "";
            }
        }
    }
}