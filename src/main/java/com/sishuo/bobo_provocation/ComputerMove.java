package com.sishuo.bobo_provocation;

import java.util.ArrayList;

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
        this.p1LastRoundMove = -2;
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
    public int generateCumulativeList(ArrayList<Integer> available, ArrayList<Integer> cumulative) {
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
    public void updateWeights(ArrayList<Integer> p2AvailableMoves) {
        if (p1LastRoundStatus == null) {
            // game just started -> No statusDTO
            adjust(0, 1000);
            // 挑衅权重增加
            return;
        }

        /* 0:挑衅 | 1:防御 | 2:左避 | 3:右避 | 4:上勾拳 | 5:左勾拳 | 6:右勾拳 | 7:直拳 |
             8:反弹 | 9:小猩猩 | 10:双层防御 | 11:冰冻 | 12:大猩猩 | 13:致命一击 | 14:解雇";
        */
        this.tempWeights = ComputerMove.moveWeights.clone(); // initialize the array

        int[] counters = p1LastRoundStatus.getCounters();
        /*  Note: 
            counters: [0] provocationsAccu; [1] provocationsUnused; [2] defenses
            [3] freezeCounter; [4] largeGorillaCounter; [5] criticalCounter; [6] layoffCounter
        */

        int p1StatusCode = p1LastRoundStatus.getStatusCode();

        if (p1StatusCode == Player.FROZEN) {
            // p1被冰冻
            adjust(0, 1000);
            adjust(7, 1000000);
            adjust(9, 1000000);
            return;
        } else if (p1StatusCode == Player.LAYOFFED) {
            // p1被解雇
            adjust(4, 1000000);
            adjust(0, 1000);
            return;
        }

        if (p2AvailableMoves.contains(9)) {
            if (counters[COUNTER_PROVOCATIONS_ACCU] < 5 && counters[COUNTER_PROVOCATIONS_UNUSED] <= 3 && counters[COUNTER_DEFENSES] == 0) {
                // 有小猩猩对面没有防御，就出小猩猩
                adjust(9, 100000);
                return;
            }
        }
        if (p2AvailableMoves.contains(11)) {
            if (counters[COUNTER_PROVOCATIONS_ACCU] <= 5 && counters[COUNTER_DEFENSES] <= 2) {
                adjust(11, 100000);
            }
        }
        if (p2AvailableMoves.contains(12)) {
            if (counters[COUNTER_PROVOCATIONS_ACCU] <= 6 && counters[COUNTER_DEFENSES] <= 2) {
                adjust(12, 100000);
            }
        }
        if (!p2AvailableMoves.contains(7) && !p2AvailableMoves.contains(11) && !p2AvailableMoves.contains(12)) {
            // 自己不能出直拳（且不能使用冰冻/大猩猩，更高的暂时不管），减少勾拳，尽量出挑衅
            adjust(4, -5);
            adjust(5, -5);
            adjust(6, -5);
            adjust(0, 5);
        }

        if (counters[COUNTER_PROVOCATIONS_UNUSED] == 0) {
            // 对面空手，安全出挑衅，不出防御/左右避
            adjust(0, 100);
            adjust(7, 10);
            adjust(1, -8);
            adjust(2, -5);
            adjust(3, -5);
        } else if (counters[COUNTER_PROVOCATIONS_UNUSED] >= 1 
                    && counters[COUNTER_PROVOCATIONS_UNUSED] < 3) {
            adjust(1, 15);
            adjust(2, 1);
            adjust(3, 1);
            adjust(7, 5);
            adjust(8, 10);
        } else if (counters[COUNTER_PROVOCATIONS_UNUSED] >= 3) {
            adjust(0, -30);
            adjust(8, 200);
            adjust(9, 500);
            adjust(10, 20);
        }

        if (counters[COUNTER_DEFENSES] == 0) {
            // 对面没有防御：打直拳或者上勾拳
            adjust(4, 10);
            adjust(7, 25);
        }
        if (counters[COUNTER_DEFENSES] == 1) {
            adjust(7, -3);
            adjust(9, -5);
        } else if (counters[COUNTER_DEFENSES] >= 2) {
            adjust(7, -3);
            adjust(9, -5);
            adjust(11, -10);
            adjust(12, -10);
        }

        switch (this.p1LastRoundMove) {
            case 0: 
                adjust(0, -10);
                adjust(1, 10);
                adjust(4, 5);
                adjust(7, 10);
                adjust(8, 10);
                break;
            case 1:
                adjust(4, 5);
                adjust(7, -3);
                adjust(9, -5);
                break;
            case 2:
            case 3:
                leftRightPunchUpdate();
                break;
            case 7:
                adjust(4, 4);
                adjust(7, 6);
                break;
            case 9: // 刚出小猩猩空窗
                adjust(0, 15);
                adjust(4, 3);
                adjust(7, 10);
                break;
            case 10:
                adjust(4, 7);
                adjust(7, 8);
                break;
            case 11:
                adjust(0, 10);  // 趁机挑衅
                adjust(7, 15);  // 直拳压制
                adjust(9, 20);  // 小猩猩压制
                break;
            case 12:
                adjust(0, 10);
                adjust(7, 15); // 对面出大猩猩己方防住，如果有直拳，就出了偷刀
                adjust(9, 20);
                break;
            case 13:
                adjust(0, 20);
                adjust(7, 15);
                adjust(9, 10);
                break;
            case 14:
                // 电脑也出了解雇对撞才活着，否则只能出防御
                adjust(0, 20);
                adjust(7, 15);
                break;
        }

        if (counters[COUNTER_FREEZE] >= 5 
            && counters[COUNTER_LARGE_GORILLA] < 6 
            && counters[COUNTER_CRITICAL] < 7 
            && counters[COUNTER_LAYOFF] < 10) {
            // 冰冻唯一可能
            adjust(1, 10);
            adjust(10, 1000);
            adjust(11, 10000); // 有冰冻也跟着出冰冻
        } else if (counters[COUNTER_LARGE_GORILLA] >= 6 
                    && counters[COUNTER_CRITICAL] < 7 && counters[COUNTER_LAYOFF] < 10) {
            // 大猩猩可能出现，准备双层防御
            adjust(10, 1000);
            adjust(12, 10000); // 有再跟着出
        } else if (counters[COUNTER_CRITICAL] >= 7 && counters[COUNTER_LAYOFF] < 10) {
            // 可能出现致命
            adjust(7, 50); // 直拳偷袭
            adjust(4, 10);
            adjust(13, 10000); // 跟着出致命
        } else if (counters[COUNTER_PROVOCATIONS_ACCU] >= 7 
                    && counters[COUNTER_CRITICAL] < 7 && counters[COUNTER_LAYOFF] < 10) {
            // 致命与解雇之间的窗口期，可以尝试偷袭
            adjust(0, 10);
            adjust(7, 50); // 直拳偷袭，如果有
            adjust(4, 4);
            adjust(1, 5);
            adjust(9, 20);
        } else if (counters[COUNTER_LAYOFF] >= 10) {
            // 对面能出解雇了，有解雇跟着出，否则就自暴自弃直拳偷一下
            adjust(7, 30);
            adjust(14, 10000);
        }
    }

    // adjust(moveIndex, amount) accepts a moveIndex (0 to 14 inclusive)
    // updates this.tempWeights[moveIndex] to max(1, this.tempWeights[moveIndex] + amount)
    public void adjust(int moveIndex, int amount) {
        if (moveIndex < 0 || moveIndex > 14) {
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
        ComputerMove.moveWeights[5] += ComputerMove.moveWeights[5] >= 4 ? 0 : 1;
        ComputerMove.moveWeights[6] += ComputerMove.moveWeights[6] >= 4 ? 0 : 1;
    }

}
