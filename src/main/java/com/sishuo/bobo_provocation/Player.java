package com.sishuo.bobo_provocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Scanner;

// This class describes a Player playing Bobo Provocation
// contains the state and counters of the Player during a game

public class Player {
    public static Scanner sc;

    private int provocationsAccu;
    private int provocationsUnused;
    private int defenses;

    private int freezeCounter;
    private int largeGorillaCounter;
    private int criticalCounter;
    private int layoffCounter;

    private int layoffRemainingMoves;

    private int state;
    public static final int LOST = -1;
    public static final int NORMAL = 0;
    public static final int WON = 1;
    public static final int FROZEN = 2;
    public static final int LAYOFFED = 3;

    private ArrayList<Integer> available;

    public static final int MOVE_NULL = -2;
    public static final int MOVE_FROZEN_NULL = -1;
    public static final int MOVE_PROVOCATION = 0;
    public static final int MOVE_DEFENSE = 1;
    public static final int MOVE_LEFT_DODGE = 2;
    public static final int MOVE_RIGHT_DODGE = 3;
    public static final int MOVE_UPPERCUT = 4;
    public static final int MOVE_LEFT_HOOK = 5;
    public static final int MOVE_RIGHT_HOOK = 6;
    public static final int MOVE_STRAIGHT_PUNCH = 7;
    public static final int MOVE_REBOUND = 8;
    public static final int MOVE_SMALL_GORILLA = 9;
    public static final int MOVE_DOUBLE_DEFENSE = 10;
    public static final int MOVE_FREEZE = 11;
    public static final int MOVE_LARGE_GORILLA = 12;
    public static final int MOVE_CRITICAL = 13;
    public static final int MOVE_LAYOFF = 14;


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
            // 0: 挑衅 Provocation
            {
                {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {-1, 1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 1: 防御 Defense
            {
                {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 2: 左避 Left Dodge
            {
                {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 3: 右避 Right Dodge
            {
                {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 4: 上勾拳 Upper Hook
            {
                {0, 0}, {1, -1}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {-1, 1}, {-1, 1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 5: 左勾拳 Left Hook
            {
                {0, 0}, {0, 0}, {1, -1}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {-1, 1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 6: 右勾拳 Right Hook
            {
                {0, 0}, {0, 0}, {0, 0}, {1, -1}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {0, 0}, {-1, 1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 7: 直拳 Stright Punch
            {
                {1, -1}, {0, 0}, {0, 0}, {0, 0}, {1, -1}, {1, -1}, {1, -1}, {0, 0}, {-1, 1}, {-1, 1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 8: 反弹 Rebound
            {
                {0, 0}, {0, 0}, {0, 0}, {0, 0}, {1, -1}, {0, 0}, {0, 0}, {1, -1}, {0, 0}, {1, -1}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 9: 小猩猩 Small Gorilla
            {
                {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {-1, 1}, {0, 0}, {0, 0}, {2, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 10: 双层防御 Double Defense
            {
                {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {-1, 1}, {3, 0}
            },
            // 11: 冰冻 Freeze
            {
                {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 0}, {0, 0}, {-1, 1}, {-1, 1}, {3, 0}
            },
            // 12: 大猩猩 Large Gorilla
            {
                {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {0, 0}, {1, -1}, {0, 0}, {-1, 1}, {3, 0}
            },
            // 13: 致命一击 Critical Strike
            {
                {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {1, -1}, {0, 0}, {3, 0}
            },
            // 14: 解雇 Layoff
            {
                {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 0}
            }
        };
        Player.results = results0;
    }

    public Player() {
        provocationsAccu = 0;
        provocationsUnused = 0;
        defenses = 0;

        freezeCounter = 0;
        largeGorillaCounter = 0;
        criticalCounter = 0;
        layoffCounter = 0;

        layoffRemainingMoves = 0;

        state = Player.NORMAL;
        available = new ArrayList<>();
    }

    // getState() returns this.state
    public int getState() {
        return this.state;
    }

    // setState() sets a Player's state (state should be in -1, 0, 1, 2, 3)
    public void setState(int state) {
        if (state < Player.LOST || state > Player.LAYOFFED) {
            throw new RuntimeException("Invalid state number!");
            // return;
        }
        this.state = state;
    }

    // getAvailableMovesArray() returns this.available
    public ArrayList<Integer> getAvailableMovesArray() {
        return this.available;
    }

    // setLayoff() Force the Player to have 5 rounds of only defenses
    public void setLayoff() {
        this.state = Player.LAYOFFED;
        this.layoffRemainingMoves = 5;
    }

    // minusOneLayoff() removes one round of LAYOFF state. 
    // If the LAYOFF state hits zero, the state is changed
    // back to NORMAL and returns true
    public boolean minusOneLayoff() {
        this.layoffRemainingMoves--;
        this.state = Player.LAYOFFED;
        if (this.layoffRemainingMoves == 0) {
            this.state = Player.NORMAL;
            return true;
        }
        return false;
    }

    // getLayoffRemaining() returns this.layoffRemainingMoves
    public int getLayoffRemaining() {
        return this.layoffRemainingMoves;
    }

    // reset() is called when a new game starts
    // the method resets/initializes the player's counters and state
    public void reset() {
        provocationsAccu = 0;
        provocationsUnused = 0;
        defenses = 0;

        freezeCounter = 0;
        largeGorillaCounter = 0;
        criticalCounter = 0;
        layoffCounter = 0;

        state = Player.NORMAL;
        available.clear();

        this.layoffRemainingMoves = 0;
    }

    // calculateState(firstPlayer, secondPlayer)
    // Reads Player.results and returns an int[2] representing the state of players after the round
    // firstPlayer and secondPlayer are ints representing the moveID of Player1 and Player2, respectively
    public static int[] calculateState(int firstPlayer, int secondPlayer) {
        return Player.results[firstPlayer][secondPlayer];
    }

    // updateState(p1, p2, firstPlayer, secondPlayer)
    // reads Player.results for the states of Players; updates p1.state and p2.state
    // returns a String describing the round result
    public static String updateState(Player p1, Player p2, int firstPlayer, int secondPlayer) {
        System.out.println();
        int[] res = null;
        String result;
        if (p1.getState() == Player.FROZEN) {
            res =  results[0][secondPlayer];
            if (secondPlayer == MOVE_LAYOFF && firstPlayer != MOVE_LAYOFF) {
                p1.setLayoff();
            } else if (firstPlayer == MOVE_LAYOFF && secondPlayer != MOVE_LAYOFF) {
                p2.setLayoff();
            }
            p1.setState(res[0]);
            p2.setState(res[1]);
            result = "Player1本回合被冰冻，无法出招\n";
            result += ("Player2出招: " + secondPlayer + ":" + moves[secondPlayer]);
            return result;
        } else if (p2.getState() == Player.FROZEN) {
            res =  results[firstPlayer][MOVE_PROVOCATION]; // Use MOVE_PROVOCATION as a placeholder
            if (secondPlayer == MOVE_LAYOFF && firstPlayer != MOVE_LAYOFF) {
                p1.setLayoff();
            } else if (firstPlayer == MOVE_LAYOFF && secondPlayer != MOVE_LAYOFF) {
                p2.setLayoff();
            }
            p1.setState(res[0]);
            p2.setState(res[1]);
            result = ("Player1出招: " + firstPlayer + ":" + moves[firstPlayer]);
            result += ("\nPlayer2本回合被冰冻，无法出招");
        } else if (p1.getState() == Player.LAYOFFED) {
            res = results[1][secondPlayer];
            p1.setState(res[0] == Player.LOST ? Player.LOST: Player.LAYOFFED);
            p2.setState(res[1]);
            result = ("Player1本回合被解雇，只能出 1:防御\n");
            result += ("Player2出招: " + secondPlayer + ":" + moves[secondPlayer]);
        } else if (p2.getState() == Player.LAYOFFED) {
            res = results[firstPlayer][MOVE_DEFENSE];
            p2.setState(res[1] == Player.LOST ? Player.LOST: Player.LAYOFFED);
            p1.setState(res[0]);
            result = ("Player1出招: " + firstPlayer + ":" + moves[firstPlayer]);
            result += ("\nPlayer2本回合被解雇，只能出 1:防御");
        } else {
            res =  results[firstPlayer][secondPlayer];
            if (secondPlayer == MOVE_LAYOFF && firstPlayer != MOVE_LAYOFF) {
                p1.setLayoff();
            } else if (firstPlayer == MOVE_LAYOFF && secondPlayer != MOVE_LAYOFF) {
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

    // analyzeState(states) accepts a int[2], which has the state ID for the two players
    // returns a String that describes the round result
    public static String analyzeState(int[] states) {
        // -1 Lost; 0 Continue play; 1 Won; 2 Freezed; 3 Layoffed
        String result;
        if (states[0] == Player.LOST) {
            result = ("Player2 获胜");
        } else if (states[0] == Player.WON) {
            result = ("Player1 获胜");
        } else if (states[0] == Player.FROZEN) {
            result = ("Player1 被冰冻一回合，游戏继续");
        } else if (states[1] == Player.FROZEN) {
            result = ("Player2 被冰冻一回合，游戏继续");            
        } else if (states[0] == Player.LAYOFFED) {
            result = ("Player1 被解雇，五回合内只能出防御，游戏继续");            
        } else if (states[1] == Player.LAYOFFED) {
            result = ("Player2 被解雇，五回合内只能出防御，游戏继续");            
        } else {
            result = ("游戏继续");
        }
        System.out.println(result);
        return result;
    }

    // analyzeState(p1, p2) inputs two Players whose states have been updated and analyzes the states
    // returns true if the game should continue, returns false otherwise
    public static boolean analyzeState(Player p1, Player p2) {
        // -1 Lost; 0 Continue play; 1 Won; 2 Freezed; 3 Layoffed
        if (p1.getState() == Player.LOST || p2.getState() == Player.WON) {
            System.out.println("Player2 获胜");
            return false;
        } else if (p1.getState() == Player.WON || p2.getState() == Player.LOST) {
            System.out.println("Player1 获胜");
            return false;
        } else if (p1.getState() == Player.FROZEN) {
            System.out.println("Player1 被冰冻一回合，下一回合无法出招，游戏继续");
            return true;
        } else if (p2.getState() == Player.FROZEN) {
            System.out.println("Player2 被冰冻一回合，下一回合无法出招，游戏继续");  
            return true;          
        } else if (p1.getState() == Player.LAYOFFED) {
            System.out.println("Player1 被解雇，还剩" + p1.getLayoffRemaining() + "回合（从下一回合算起），游戏继续");   
            return true;         
        } else if (p2.getState() == Player.LAYOFFED) {
            System.out.println("Player2 被解雇，还剩" + p2.getLayoffRemaining() + "回合（从下一回合算起），游戏继续");    
            return true;        
        } else {
            System.out.println("游戏继续");
            return true;
        }
    }

    // analyzeState(p1, p2, sb) inputs two Players whose states have been updated and analyzes the states
    // the method also inputs a StringBuilder sb, where the String message describing the round result is updated
    // returns true if the game should continue, returns false otherwise
    public static boolean analyzeState(Player p1, Player p2, StringBuilder sb) {
        // -1 Lost; 0 Continue play; 1 Won; 2 Freezed; 3 Layoffed
        if (p1.getState() == Player.LOST || p2.getState() == Player.WON) {
            sb.append(getRandomMessage(Player.LOST));
            System.out.println("Player2 获胜");
            return false;
        } else if (p1.getState() == Player.WON || p2.getState() == Player.LOST) {
            sb.append(getRandomMessage(Player.WON));
            System.out.println("Player1 获胜");
            return false;
        } else if (p1.getState() == Player.FROZEN) {
            sb.append("你被电脑冰冻了一回合！下一回合将无法出招。");
            System.out.println("Player1 被冰冻一回合，下一回合无法出招，游戏继续");
            return true;
        } else if (p2.getState() == Player.FROZEN) {
            sb.append("电脑被你冰冻了一回合！电脑下一回合将无法出招。");  
            System.out.println("Player2 被冰冻一回合，下一回合无法出招，游戏继续");
            return true;          
        } else if (p1.getState() == Player.LAYOFFED) {
            sb.append("你被电脑解雇了！从下一回合算起，你只能出防御" + p1.getLayoffRemaining() + "回合了。");   
            System.out.println("Player1 被解雇，还剩" + p1.getLayoffRemaining() + "回合（从下一回合算起），游戏继续");
            return true;         
        } else if (p2.getState() == Player.LAYOFFED) {
            sb.append("电脑被你解雇了！从下一回合算起，电脑只能出防御" + p2.getLayoffRemaining() + "回合！");    
            System.out.println("Player2 被解雇，还剩" + p2.getLayoffRemaining() + "回合（从下一回合算起），游戏继续");
            return true;        
        } else {
            sb.append(getRandomMessage(Player.NORMAL));
            System.out.println("游戏继续");
            return true;
        }
    }

    // updateAvailableMovesArray() updates this.available and sorts the ArrayList (ascending)
    // does not print anything to console and does not check this.state (assumes state is NORMAL)
    public void updateAvailableMovesArray() {
        this.available.clear();
        /* 0:挑衅 | 1:防御 | 2:左避 | 3:右避 | 4:上勾拳 | 5:左勾拳 | 6:右勾拳 | 7:直拳 |
             8:反弹 | 9:小猩猩 | 10:双层防御 | 11:冰冻 | 12:大猩猩 | 13:致命一击 | 14:解雇";
        */
        this.available.add(MOVE_PROVOCATION);
        this.available.add(MOVE_DEFENSE);
        this.available.add(MOVE_LEFT_DODGE);
        this.available.add(MOVE_RIGHT_DODGE);
        this.available.add(MOVE_UPPERCUT);
        this.available.add(MOVE_LEFT_HOOK);
        this.available.add(MOVE_RIGHT_HOOK);
        
        if (defenses >= 2) {
            this.available.add(MOVE_DOUBLE_DEFENSE);
        }
        if (defenses >= 1) {
            this.available.add(MOVE_REBOUND);
        }
        if (provocationsUnused >= 3) {
            this.available.add(MOVE_SMALL_GORILLA);
        } 
        if (provocationsUnused >= 1) {
            this.available.add(MOVE_STRAIGHT_PUNCH);
        }
        if (freezeCounter >= 5) {
            this.available.add(MOVE_FREEZE);
        }
        if (largeGorillaCounter >= 6) {
            this.available.add(MOVE_LARGE_GORILLA);
        }
        if (criticalCounter >= 7) {
            this.available.add(MOVE_CRITICAL);
        }
        if (layoffCounter >= 10) {
            this.available.add(MOVE_LAYOFF);
        }
        
        this.available.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }    
        });
    }


    // showAvailableMoves() updates this.available by calling updateAvailableMovesArray() 
    // prints the counters and all available moves to console
    public int showAvailableMoves() {
        this.available.clear();
        System.out.println("现在状态：累计挑衅: " + provocationsAccu);
        System.out.println("现在状态：未兑换挑衅: " + provocationsUnused);
        System.out.println("现在状态：未兑换防御: " + defenses);
        System.out.print("冰冻计数器：" + freezeCounter);
        System.out.print("; 大猩猩计数器：" + largeGorillaCounter);
        System.out.print("; 致命一击计数器：" + criticalCounter);
        System.out.println("; 解雇计数器：" + layoffCounter);

        System.out.println("当前玩家状态码：" + state);

        if (state == Player.FROZEN) {
            System.out.println("当前被冰冻，无法出招");
            return Player.FROZEN;
        } else if (state == Player.LAYOFFED) {
            System.out.println("当前被解雇，只能出 1:防御");
            return Player.LAYOFFED;
        }

        updateAvailableMovesArray();
        System.out.println("本回合可使用的招法：");
        for (int i : available) {
            System.out.print(i + ":" + moves[i] + "; ");
        }
        System.out.println();
        return Player.NORMAL;
    }

    // showStatus() returns a StatusDTO object containing information (counters, states) about the Player
    // updates this.available by calling updateAvailableMovesArray()
    // prints relevant information to console
    public StatusDTO showStatus(){
        available.clear();

        int[] counters = {provocationsAccu, provocationsUnused, defenses, freezeCounter, largeGorillaCounter, criticalCounter, layoffCounter};
        System.out.println("现在状态：累计挑衅: " + provocationsAccu);
        System.out.println("现在状态：未兑换挑衅: " + provocationsUnused);
        System.out.println("现在状态：未兑换防御: " + defenses);
        System.out.print("冰冻计数器：" + freezeCounter);
        System.out.print("; 大猩猩计数器：" + largeGorillaCounter);
        System.out.print("; 致命一击计数器：" + criticalCounter);
        System.out.println("; 解雇计数器：" + layoffCounter);

        System.out.println("当前玩家状态码：" + state);

        if (state == Player.FROZEN) {
            System.out.println("当前被冰冻，无法出招");
            System.out.println();
            return new StatusDTO(counters, state, available);
        } else if (state == Player.LAYOFFED) {
            System.out.println("当前被解雇，只能出 1:防御");
            System.out.println();
            available.add(MOVE_DEFENSE);
            return new StatusDTO(counters, state, available);
        }

        updateAvailableMovesArray();

        System.out.println("本回合可使用的招法：");
        for (int i : available) {
            System.out.print(i + ":" + moves[i] + "; ");
        }

        return new StatusDTO(counters, Player.NORMAL, available);
    }

    // updateProvocationUnused(cost) updates this.provocationUnused
    // called when freeze/large gorilla/critical/layoff is used by a player
    public void updateProvocationUnused(int cost) {
        // 优先扣除已兑换的挑衅数量，再扣除未兑换的挑衅数量
        if (cost <= provocationsAccu - provocationsUnused) {
            // 总共的已兑换挑衅数量大于等于总花费
            return;
        } else {
            this.provocationsUnused -= cost - (this.provocationsAccu - this.provocationsUnused);
        }
    }

    // analyzeMove(move) inputs a moveID (0 <= move <= 14) and updates the Player's counters
    public void analyzeMove(int move) {
        /* 0:挑衅 | 1:防御 | 2:左避 | 3:右避 | 4:上勾拳 | 5:左勾拳 | 6:右勾拳 | 7:直拳 |
             8:反弹 | 9:小猩猩 | 10:双层防御 | 11:冰冻 | 12:大猩猩 | 13:致命一击 | 14:解雇";
        */
        switch (move) {
            case MOVE_PROVOCATION: //挑衅
                provocationsAccu++;
                provocationsUnused++;
                freezeCounter++;
                largeGorillaCounter++;
                criticalCounter++;
                layoffCounter++;
                break;
            case MOVE_DEFENSE: //防御
                defenses++;
                break;
            case MOVE_STRAIGHT_PUNCH: //直拳
                provocationsUnused--;
                break;
            case MOVE_REBOUND: //反弹
                defenses--;
                break;
            case MOVE_SMALL_GORILLA: //小猩猩
                provocationsUnused -= 3;
                break;
            case MOVE_DOUBLE_DEFENSE: //双层防御
                defenses -= 2;
                break;
            case MOVE_FREEZE: //冰冻
                updateProvocationUnused(5);
                freezeCounter = 0;
                break;
            case MOVE_LARGE_GORILLA: //大猩猩
                updateProvocationUnused(6);
                freezeCounter = 0;
                largeGorillaCounter = 0;
                break;
            case MOVE_CRITICAL: //致命一击
                updateProvocationUnused(7);
                freezeCounter = 0;
                largeGorillaCounter = 0;
                criticalCounter = 0;
                break;
            case MOVE_LAYOFF: //解雇
                updateProvocationUnused(10);
                freezeCounter = 0;
                largeGorillaCounter = 0;
                criticalCounter = 0;
                layoffCounter = 0;
                break;
            default:
                break;
        }
    }
    
    // makeMove() allows a Player to make a move by typing in a moveID (0 <= moveID <= 14) to System.in
    // returns the number inputed
    public int makeMove() {
        if (Player.sc == null) {
            Player.sc = new Scanner(System.in);
        }
        int moving = this.showAvailableMoves();
        int move;
        // moving == 2 被冰冻； moving == 3 被解雇
        if (moving == Player.FROZEN) {
            Player.sc.nextInt();
            return MOVE_FROZEN_NULL;
        } else if (moving == Player.LAYOFFED) {
            Player.sc.nextInt();
            minusOneLayoff();
            return MOVE_DEFENSE;
        } else {
            while (true) {
                move = Player.sc.nextInt();
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

    // makeRandomMove(show) returns a random moveID in this.available
    // each moveID is equally likely to be returned
    // If needs updating this.available and printing the Player's information to console, 
    // call makeRandomMove(true)
    public int makeRandomMove(boolean show) {
        if (show) {
            this.showAvailableMoves();
        }
        // state == 2 被冰冻； state == 3 被解雇        
        if (state == Player.FROZEN) {
            return MOVE_FROZEN_NULL;
        } else if (state == Player.LAYOFFED) {
            minusOneLayoff();
            return MOVE_DEFENSE;
        } else {
            Collections.shuffle(available);
            return available.get(0);
        }
    }

    // makeWeightedMove() returns a moveID in available
    // the probability of an ID chosen depends on the weight in Player.moveWeights
    public int makeWeightedMove() {
        int moving = this.showAvailableMoves();
        // moving == 2 被冰冻； moving == 3 被解雇
        if (moving == Player.FROZEN) {
            return MOVE_FROZEN_NULL;
        } else if (moving == Player.LAYOFFED) {
            minusOneLayoff();
            return MOVE_DEFENSE;
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

    // makeAdjustedWeightedMove(cm) returns a moveID in available
    // the probability of a moveID chosen is altered in cm.updatedWeights(), which is related to
    // the previous move, state, and counters of the frontend user
    public int makeAdjustedWeightedMove(ComputerMove cm) {
        int moving = this.showAvailableMoves();
        // moving == 2 被冰冻； moving == 3 被解雇
        if (moving == Player.FROZEN) {
            return MOVE_FROZEN_NULL;
        } else if (moving == Player.LAYOFFED) {
            minusOneLayoff();
            return MOVE_DEFENSE;
        } else {
            cm.updateWeights(this.available); // 在cm内更新权重
            ArrayList<Integer> cumulatedAvailableMoveWeights = new ArrayList<>();
            int weight = cm.generateCumulativeList(this.available, cumulatedAvailableMoveWeights);

            System.out.println("Player2权重表:" + cumulatedAvailableMoveWeights.toString());
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

    // getRandomMessage(int type) returns a random message String 
    // stored in Player.failMessages, Player.normalMessages and Player.winMessages
    // type can only be -1, 0, 1 for correct usage
    public static String getRandomMessage(int type) {
        Random r = new Random();
        int index = r.nextInt(4);
        switch (type){
            case Player.LOST: {
                return failMessages[index];
            } case Player.NORMAL: {
                return normalMessages[index];
            } case Player.WON: {
                return winMessages[index];
            } default: {
                return "";
            }
        }
    }
}