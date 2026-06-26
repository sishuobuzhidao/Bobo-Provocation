package com.sishuo.bobo_provocation;

import java.util.List;

// This class is for a better AI move system based on weights
// stores the frontend users's move and StatusDTO last round
// stores a weight table for the AI

public class ComputerMove {
    private int p1LastRoundMove; // Usually 0-14; -2 -> None; -1 -> Freezed
    private StatusDTO p1LastRoundStatus;

    private static int[] moveWeights = {35, 10, 3, 3, 4, 1, 1, 15, 10, 100, 5, 200, 500, 100000, 1000000};
    private int[] tempWeights; // moveWeights array for one round

    public static final int COUNTER_PROVOCATIONS_ACCU = 0;
    public static final int COUNTER_PROVOCATIONS_UNUSED = 1;
    public static final int COUNTER_DEFENSES = 2;
    public static final int COUNTER_FREEZE = 3;
    public static final int COUNTER_LARGE_GORILLA = 4;
    public static final int COUNTER_CRITICAL = 5;
    public static final int COUNTER_LAYOFF = 6;

    public ComputerMove() {
        this.p1LastRoundMove = Player.MOVE_NULL;
        this.p1LastRoundStatus = null;
        this.tempWeights = ComputerMove.moveWeights.clone();
    }

    // updateP1(p1Move, p1Status) refreshes the storage of the frontend user's last round move 
    // and StatusDTO
    public void updateP1(int p1Move, StatusDTO p1Status) {
        this.p1LastRoundMove = p1Move;
        this.p1LastRoundStatus = p1Status;
    }

    // generateCumulativeList(available, cumulative) inputs two ArrayLists
    // the first ArrayList is all availableMoves the player can make
    // the method generates a cumulative weight table in cumulative
    // returns the largest number in cumulative (aka the last number)
    public int generateCumulativeList(List<Integer> available, List<Integer> cumulative) {
        int weight = 0;

        for (int i : available) {
            weight += this.tempWeights[i];
            cumulative.add(weight);
        }

        return weight;
    }

    // updateWeights(p2AvailableMoves) updates this.tempWeights
    // by examining information from this.p1LastRoundMove, this.p1LastRoundStatus and p2AvailableMoves
    // time: O(1)
    public void updateWeights(List<Integer> p2AvailableMoves) {
        if (p1LastRoundStatus == null) {
            // game just started -> No statusDTO
            adjust(Player.MOVE_PROVOCATION, 1000);
            // 挑衅权重增加
            return;
        }

        this.tempWeights = ComputerMove.moveWeights.clone(); // initialize the array

        int[] counters = p1LastRoundStatus.getCounters();
        /*  Note: 
            counters: [0] provocationsAccu; [1] provocationsUnused; [2] defenses
            [3] freezeCounter; [4] largeGorillaCounter; [5] criticalCounter; [6] layoffCounter
        */

        int p1StatusCode = p1LastRoundStatus.getStatusCode();

        if (p1StatusCode == Player.FROZEN) {
            // p1被冰冻
            adjust(Player.MOVE_PROVOCATION, 1000);
            adjust(Player.MOVE_STRAIGHT_PUNCH, 1000000);
            adjust(Player.MOVE_SMALL_GORILLA, 1000000);
            return;
        } else if (p1StatusCode == Player.LAYOFFED) {
            // p1被解雇
            adjust(Player.MOVE_UPPERCUT, 1000000);
            adjust(Player.MOVE_PROVOCATION, 1000);
            return;
        }

        if (p2AvailableMoves.contains(Player.MOVE_SMALL_GORILLA) 
            && counters[COUNTER_PROVOCATIONS_ACCU] < 5 
            && counters[COUNTER_PROVOCATIONS_UNUSED] <= 3 
            && counters[COUNTER_DEFENSES] == 0) {
                // 有小猩猩对面没有防御，就出小猩猩
                adjust(Player.MOVE_SMALL_GORILLA, 100000);
                return;
            }
        
        if (p2AvailableMoves.contains(Player.MOVE_FREEZE) 
            && counters[COUNTER_PROVOCATIONS_ACCU] <= 5 
            && counters[COUNTER_DEFENSES] <= 2) {
                adjust(Player.MOVE_FREEZE, 100000);
            }
        
        if (p2AvailableMoves.contains(Player.MOVE_LARGE_GORILLA) 
            && counters[COUNTER_PROVOCATIONS_ACCU] <= 6 
            && counters[COUNTER_DEFENSES] <= 2) {
                adjust(Player.MOVE_LARGE_GORILLA, 100000);
            }
        
        if (!p2AvailableMoves.contains(Player.MOVE_STRAIGHT_PUNCH) 
            && !p2AvailableMoves.contains(Player.MOVE_FREEZE) 
            && !p2AvailableMoves.contains(Player.MOVE_LARGE_GORILLA)) {
            // 自己不能出直拳（且不能使用冰冻/大猩猩，更高的暂时不管），减少勾拳，尽量出挑衅
            adjust(Player.MOVE_UPPERCUT, -5);
            adjust(Player.MOVE_LEFT_HOOK, -5);
            adjust(Player.MOVE_RIGHT_HOOK, -5);
            adjust(Player.MOVE_PROVOCATION, 5);
        }

        if (counters[COUNTER_PROVOCATIONS_UNUSED] == 0) {
            // 对面空手，安全出挑衅，不出防御/左右避
            adjust(Player.MOVE_PROVOCATION, 100);
            adjust(Player.MOVE_STRAIGHT_PUNCH, 10);
            adjust(Player.MOVE_DEFENSE, -8);
            adjust(Player.MOVE_LEFT_DODGE, -5);
            adjust(Player.MOVE_RIGHT_DODGE, -5);
        } else if (counters[COUNTER_PROVOCATIONS_UNUSED] >= 1 
                    && counters[COUNTER_PROVOCATIONS_UNUSED] < 3) {
            adjust(Player.MOVE_DEFENSE, 15);
            adjust(Player.MOVE_LEFT_DODGE, 1);
            adjust(Player.MOVE_RIGHT_DODGE, 1);
            adjust(Player.MOVE_STRAIGHT_PUNCH, 5);
            adjust(Player.MOVE_REBOUND, 10);
        } else if (counters[COUNTER_PROVOCATIONS_UNUSED] >= 3) {
            adjust(Player.MOVE_PROVOCATION, -30);
            adjust(Player.MOVE_REBOUND, 200);
            adjust(Player.MOVE_SMALL_GORILLA, 500);
            adjust(Player.MOVE_DOUBLE_DEFENSE, 20);
        }

        if (counters[COUNTER_DEFENSES] == 0) {
            // 对面没有防御：打直拳或者上勾拳
            adjust(Player.MOVE_UPPERCUT, 5);
            adjust(Player.MOVE_STRAIGHT_PUNCH, 25);
        }
        if (counters[COUNTER_DEFENSES] == 1) {
            adjust(Player.MOVE_STRAIGHT_PUNCH, -3);
            adjust(Player.MOVE_SMALL_GORILLA, -5);
        } else if (counters[COUNTER_DEFENSES] >= 2) {
            adjust(Player.MOVE_STRAIGHT_PUNCH, -3);
            adjust(Player.MOVE_SMALL_GORILLA, -5);
            adjust(Player.MOVE_FREEZE, -10);
            adjust(Player.MOVE_LARGE_GORILLA, -10);
        }

        switch (this.p1LastRoundMove) {
            case Player.MOVE_PROVOCATION: 
                adjust(Player.MOVE_PROVOCATION, -10);
                adjust(Player.MOVE_DEFENSE, 10);
                adjust(Player.MOVE_UPPERCUT, 5);
                adjust(Player.MOVE_STRAIGHT_PUNCH, 15);
                adjust(Player.MOVE_REBOUND, 10);
                break;
            case Player.MOVE_DEFENSE:
                adjust(Player.MOVE_UPPERCUT, 5);
                adjust(Player.MOVE_STRAIGHT_PUNCH, -3);
                adjust(Player.MOVE_SMALL_GORILLA, -5);
                break;
            case Player.MOVE_LEFT_DODGE, Player.MOVE_RIGHT_DODGE:
                leftRightPunchUpdate();
                break;
            case Player.MOVE_STRAIGHT_PUNCH:
                adjust(Player.MOVE_UPPERCUT, 4);
                adjust(Player.MOVE_STRAIGHT_PUNCH, 6);
                break;
            case Player.MOVE_SMALL_GORILLA: // 刚出小猩猩空窗
                adjust(Player.MOVE_PROVOCATION, 15);
                adjust(Player.MOVE_UPPERCUT, 3);
                adjust(Player.MOVE_STRAIGHT_PUNCH, 10);
                break;
            case Player.MOVE_DOUBLE_DEFENSE:
                adjust(Player.MOVE_UPPERCUT, 7);
                adjust(Player.MOVE_STRAIGHT_PUNCH, 8);
                break;
            case Player.MOVE_FREEZE, Player.MOVE_LARGE_GORILLA:
                adjust(Player.MOVE_PROVOCATION, 10);  // 趁机挑衅
                adjust(Player.MOVE_STRAIGHT_PUNCH, 15);  // 直拳压制
                adjust(Player.MOVE_SMALL_GORILLA, 20);  // 小猩猩压制
                break;
            case Player.MOVE_CRITICAL:
                adjust(Player.MOVE_PROVOCATION, 20);
                adjust(Player.MOVE_STRAIGHT_PUNCH, 15);
                adjust(Player.MOVE_SMALL_GORILLA, 10);
                break;
            case Player.MOVE_LAYOFF:
                // 电脑也出了解雇对撞才活着，否则只能出防御
                adjust(Player.MOVE_PROVOCATION, 20);
                adjust(Player.MOVE_STRAIGHT_PUNCH, 15);
                break;
            default:
                break;
        }

        if (counters[COUNTER_FREEZE] >= 5 
            && counters[COUNTER_LARGE_GORILLA] < 6 
            && counters[COUNTER_CRITICAL] < 7 
            && counters[COUNTER_LAYOFF] < 10) {
            // 冰冻唯一可能
            adjust(Player.MOVE_DEFENSE, 10);
            adjust(Player.MOVE_DOUBLE_DEFENSE, 1000);
            adjust(Player.MOVE_FREEZE, 10000); // 有冰冻也跟着出冰冻
        } else if (counters[COUNTER_LARGE_GORILLA] >= 6 
                    && counters[COUNTER_CRITICAL] < 7 && counters[COUNTER_LAYOFF] < 10) {
            // 大猩猩可能出现，准备双层防御
            adjust(Player.MOVE_DOUBLE_DEFENSE, 1000);
            adjust(Player.MOVE_LARGE_GORILLA, 10000); // 有再跟着出
        } else if (counters[COUNTER_CRITICAL] >= 7 && counters[COUNTER_LAYOFF] < 10) {
            // 可能出现致命
            adjust(Player.MOVE_STRAIGHT_PUNCH, 50); // 直拳偷袭
            adjust(Player.MOVE_UPPERCUT, 10);
            adjust(Player.MOVE_CRITICAL, 10000); // 跟着出致命
        } else if (counters[COUNTER_PROVOCATIONS_ACCU] >= 7 
                    && counters[COUNTER_CRITICAL] < 7 && counters[COUNTER_LAYOFF] < 10) {
            // 致命与解雇之间的窗口期，可以尝试偷袭
            adjust(Player.MOVE_PROVOCATION, 10);
            adjust(Player.MOVE_STRAIGHT_PUNCH, 50); // 直拳偷袭，如果有
            adjust(Player.MOVE_UPPERCUT, 4);
            adjust(Player.MOVE_DEFENSE, 5);
            adjust(Player.MOVE_SMALL_GORILLA, 20);
        } else if (counters[COUNTER_LAYOFF] >= 10) {
            // 对面能出解雇了，有解雇跟着出，否则就自暴自弃直拳偷一下
            adjust(Player.MOVE_STRAIGHT_PUNCH, 30);
            adjust(Player.MOVE_LAYOFF, 10000);
        }
    }

    // adjust(moveIndex, amount) accepts a moveIndex (0 to 14 inclusive)
    // updates this.tempWeights[moveIndex] to max(1, this.tempWeights[moveIndex] + amount)
    public void adjust(int moveIndex, int amount) {
        if (moveIndex < Player.MOVE_PROVOCATION || moveIndex > Player.MOVE_LAYOFF) {
            // moveIndex not in 0-14
            return;
        }
        this.tempWeights[moveIndex] += amount;
        if (this.tempWeights[moveIndex] <= 0) {
            this.tempWeights[moveIndex] = 1;
        }
    }

    // updates ComputerMove.moveWeights[5 and 6] (Left, right punch)
    // increments by 1 with a limit of 4
    // this method is for a learning AI (usually left and right dodges are rarely used)
    public static void leftRightPunchUpdate() {
        ComputerMove.moveWeights[Player.MOVE_LEFT_HOOK] += ComputerMove.moveWeights[Player.MOVE_LEFT_HOOK] >= 4 ? 0 : 1;
        ComputerMove.moveWeights[Player.MOVE_RIGHT_HOOK] += ComputerMove.moveWeights[Player.MOVE_RIGHT_HOOK] >= 4 ? 0 : 1;
    }

}
