package com.sishuo.bobo_provocation;

import java.util.ArrayList;

public class ComputerMove {
    // 本类是为了更强的AI算法创建的，需要存储玩家前面出的招式和可选招式，以及电脑自己可选招式的权重

    private int p1LastRoundMove; // Usually 0-14; -2 -> None; -1 -> Freezed
    private StatusDTO p1LastRoundStatus;

    // private static final int[] MOVEWEIGHTS_OLD = {10, 9, 4, 4, 5, 1, 1, 12, 10, 25, 20, 50, 100, 200, 1000};
    private static int[] moveWeights = {35, 10, 3, 3, 4, 1, 1, 15, 10, 100, 5, 200, 500, 100000, 1000000};
    private int[] tempWeights;

    public ComputerMove() {
        this.p1LastRoundMove = -2;
        this.p1LastRoundStatus = null;
        this.tempWeights = ComputerMove.moveWeights.clone();
    }

    public void updateP1(int p1move, StatusDTO p1Status) {
        this.p1LastRoundMove = p1move;
        this.p1LastRoundStatus = p1Status;
    }

    public int generateCumulativeList(ArrayList<Integer> available, ArrayList<Integer> cumulative) {
        int weight = 0;

        for (int i : available) {
            weight += this.tempWeights[i];
            cumulative.add(weight);
        }

        return weight;
    }

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
        this.tempWeights = ComputerMove.moveWeights.clone(); // iniatilize

        int[] counters = p1LastRoundStatus.getCounters();
        /*  Note: 
            counters: [0] provocations_accu; [1] provocations_unused; [2] defenses
            [3] freeze_counter; [4] lgorilla_counter; [5] critical_counter; [6] layoff_counter
        */

        int p1StatusCode = p1LastRoundStatus.getStatusCode();

        if (p1StatusCode == 2) {
            // p1被冰冻
            adjust(0, 1000);
            adjust(7, 1000000);
            adjust(9, 1000000);
            return;
        } else if (p1StatusCode == 3) {
            // p1被解雇
            adjust(4, 1000000);
            adjust(0, 1000);
            return;
        }

        if (p2AvailableMoves.contains(9)) {
            if (counters[0] < 5 && counters[1] <= 3 && counters[2] == 0) {
                // 有小猩猩对面没有防御，就出小猩猩
                adjust(9, 100000);
                return;
            }
        }
        if (p2AvailableMoves.contains(11)) {
            if (counters[0] <= 5 && counters[2] <= 2) {
                adjust(11, 100000);
            }
        }
        if (p2AvailableMoves.contains(12)) {
            if (counters[0] <= 6 && counters[2] <= 2) {
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

        if (counters[1] == 0) {
            // 对面空手，安全出挑衅，不出防御/左右避
            adjust(0, 100);
            adjust(7, 10);
            adjust(1, -8);
            adjust(2, -5);
            adjust(3, -5);
        } else if (counters[1] >= 1 && counters[1] < 3) {
            adjust(1, 15);
            adjust(2, 1);
            adjust(3, 1);
            adjust(7, 5);
            adjust(8, 10);
        } else if (counters[1] >= 3) {
            adjust(0, -30);
            adjust(8, 200);
            adjust(9, 500);
            adjust(10, 20);
        }

        if (counters[2] == 0) {
            // 对面没有防御：打直拳或者上勾拳
            adjust(4, 10);
            adjust(7, 25);
        }
        if (counters[2] == 1) {
            adjust(7, -3);
            adjust(9, -5);
        } else if (counters[2] >= 2) {
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

        if (counters[3] >= 5 && counters[4] < 6 && counters[5] < 7 && counters[6] < 10) {
            // 冰冻唯一可能
            adjust(1, 10);
            adjust(10, 1000);
            adjust(11, 10000); // 有冰冻也跟着出冰冻
        } else if (counters[4] >= 6 && counters[5] < 7 && counters[6] < 10) {
            // 大猩猩可能出现，准备双层防御
            adjust(10, 1000);
            adjust(12, 10000); // 有再跟着出
        } else if (counters[5] >= 7 && counters[6] < 10) {
            // 可能出现致命
            adjust(7, 50); // 直拳偷袭
            adjust(4, 10);
            adjust(13, 10000); // 跟着出致命
        } else if (counters[0] >= 7 && counters[5] < 7 && counters[6] < 10) {
            // 致命与解雇之间的窗口期，可以尝试偷袭
            adjust(0, 10);
            adjust(7, 50); // 直拳偷袭，如果有
            adjust(4, 4);
            adjust(1, 5);
            adjust(9, 20);
        } else if (counters[6] >= 10) {
            // 对面能出解雇了，有解雇跟着出，否则就自暴自弃直拳偷一下
            adjust(7, 30);
            adjust(14, 10000);
        }
    }

    public void adjust(int moveIndex, int amount) {
        this.tempWeights[moveIndex] += amount;
        if (this.tempWeights[moveIndex] <= 0) {
            this.tempWeights[moveIndex] = 1;
        }
    }

    public static void leftRightPunchUpdate() {
        ComputerMove.moveWeights[5] += ComputerMove.moveWeights[5] >= 4 ? 0 : 1;
        ComputerMove.moveWeights[6] += ComputerMove.moveWeights[6] >= 4 ? 0 : 1;
    }

}
