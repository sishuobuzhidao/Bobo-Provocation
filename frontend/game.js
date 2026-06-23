let isGameStarted = false;
let moves = ["挑衅", "防御", "左避", "右避", "上勾拳", "左勾拳", "右勾拳", "直拳", "反弹", "小猩猩", "双层防御", "冰冻", "大猩猩", "致命一击", "解雇"];
let isUltraHardModeOn = false;
let aiDifficulty = 0;

// 困难模式:去除了计数器面板
// 超困难模式:将所有招数的按钮都设为可选，若选择到非法招式，后台自动使用随机数挑选可选中的一个
function toggleMode() {
    isUltraHardModeOn = !isUltraHardModeOn;
    document.getElementById("mode-toggle").innerText = isUltraHardModeOn ? "困难模式" : "普通模式";
}

// 简单难度：AI纯随机出招
// 中等难度：AI固定权重出招
// 困难难度：AI反应性变化权重出招
function toggleAIDifficulty() {
    aiDifficulty = (aiDifficulty + 1) % 3;
    if (aiDifficulty === 0) {
        document.getElementById("ai-difficulty-toggle").innerText = "简单难度";
    } else if (aiDifficulty === 1) {
        document.getElementById("ai-difficulty-toggle").innerText = "中等难度";
    } else if (aiDifficulty === 2){
        document.getElementById("ai-difficulty-toggle").innerText = "困难难度";
    } else {
        document.getElementById("ai-difficulty-toggle").innerText = "参数错误";
    }
}

// 开始游戏
function startGame() {
    document.getElementById("start").disabled = true;
    document.getElementById("mode-toggle").disabled = true;
    document.getElementById("ai-difficulty-toggle").disabled = true;

    fetch('http://localhost:8080/api/startGame', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
            startGame : isUltraHardModeOn ? "ultra" : "normal", 
            aiDifficulty : aiDifficulty
        }) 
    })
    .then(response => response.json())
    .then(data => {
        if (data != null) {
            document.getElementById("battle-log-text").innerText = isUltraHardModeOn ? "请选择你第一回合的招式。如果招式非法，后台会随机从可选招式中选择一个！" : "请选择你第一回合的招式。";
            document.getElementById("p1-action-billboard").innerText = "等候选择...";
            document.getElementById("p2-action-billboard").innerText = "等候出招...";
            isGameStarted = true;
            
            document.getElementById("p1-status").innerText = "对局中";
            document.getElementById("p2-status").innerText = "对局中";            
            disableAllButtons();
            
            if (!isUltraHardModeOn) {
                enableButtons(data.availableMoves);
            } else {
                enableAllButtons();
            }
        }
    })
    .catch(err => {
        document.getElementById("battle-log-text").innerText = "无法连接到后端，请确保 Spring Boot 已启动。";
        document.getElementById("start").disabled = false;
        document.getElementById("mode-toggle").disabled = false;
        document.getElementById("ai-difficulty-toggle").disabled = false;
    });
}

// 玩家选择招式，传输到后端，后端java内部权重随机数揭开招式返回，并渲染到前端
function makeMove(moveID) {
    let requestBody = {
        gameStarted: isGameStarted,
        move: moveID, 
        isUltraHardModeOn: isUltraHardModeOn
    };

    disableAllButtons(); // 出招瞬间立刻封锁所有动作，防止连击鬼畜

    fetch('http://localhost:8080/api/analyzeMove', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody)
    })
    .then(response => response.json())
    .then(data => {
        // 增加盲点操作
        let actualP1Move = data.p1moveID;
        document.getElementById("battle-log-text").innerText = "";

        if (actualP1Move !== null && actualP1Move !== undefined && actualP1Move !== moveID) {
            // 后端出了随机不合法招数
            document.getElementById("battle-log-text").innerText += "你选择的招式不合法。作为惩罚，你的选择被替换成了随机目前局面下的合法招式。" + data.roundResult;
        } else {
            // 直接更新战况
            document.getElementById("battle-log-text").innerText += data.roundResult;
        }
        
        if (actualP1Move >= 0 && actualP1Move <= 14) {
            document.getElementById("p1-action-billboard").innerText = moves[actualP1Move];
        } else {
            document.getElementById("p1-action-billboard").innerText = "!被冰冻空过!";
        }

        // 分析电脑出的招法
        if (data.p2moveID !== -1 && data.p2moveID != null) {
            document.getElementById("p2-action-billboard").innerText = moves[data.p2moveID];
        } else {
            // data.p2moveID == -1 -> 对面被冰冻
            document.getElementById("p2-action-billboard").innerText = "!被冰冻空过!";
        }

        // 查看游戏是否需要继续
        if (data.shouldContinue === true) {
            if (data.statusCode === 2) {
                // 我方下回合被冰冻状态
                document.getElementById("p1-status").innerText = "被冰冻";
                document.getElementById("battle-log-text").innerText += " (你被冰冻了！2秒后自动空过此轮...)";
                
                // 留出1.5秒空档给玩家看清战报，然后用代码发送 -1 空过
                setTimeout(() => { makeMove(-1); }, 2000);
            } else if (data.statusCode === 3) {
                // 我方下回合被解雇状态
                document.getElementById("p1-status").innerText = "被解雇";
                enableButtons(data.availableMoves); // 此时后端只会发 [1]，逼玩家点防御
            } else {
                // 正常状态
                document.getElementById("p1-status").innerText = "对局中";
                if (!isUltraHardModeOn) {
                    enableButtons(data.availableMoves); 
                } else {
                    enableAllButtons();
                }
            }
        } else {
            // 胜负已分
            isGameStarted = false;
            disableAllButtons();
            document.getElementById("start").disabled = false;
            document.getElementById("mode-toggle").disabled = false;
            document.getElementById("ai-difficulty-toggle").disabled = false;
            document.getElementById("p1-status").innerText = "准备就绪";
            document.getElementById("p2-status").innerText = "准备就绪";          
        }
    })
    .catch(err => {
        console.error(err);
        document.getElementById("battle-log-text").innerText = "运行时数据传输崩溃，请检查后端报错日志。";
        document.getElementById("start").disabled = false;
        document.getElementById("mode-toggle").disabled = false;
        document.getElementById("ai-difficulty-toggle").disabled = false;
    });
}

// 通过传入参数availableMoves（ArrayList<Integer>）把能出招的按钮能被点击
function enableButtons(availableMoves) {
    if (availableMoves == null) return;
    for (let id of availableMoves) {
        if (id >= 0 && id <= 14) {
            document.getElementById("Button" + id).disabled = false;
        }
    }
}

// 使所有按钮可点击
function enableAllButtons() {
    for (let index = 0; index < 15; index++) {
        document.getElementById("Button" + index).disabled = false;                
    }
}

// 使所有按钮无法点击
function disableAllButtons() {
    for (let index = 0; index < 15; index++) {
        document.getElementById("Button" + index).disabled = true;                
    }
}

// enableButtons([0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]); // 测试前端颜色用