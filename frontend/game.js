let isGameStarted = false;
let moves = ["挑衅", "防御", "左避", "右避", "上勾拳", "左勾拳", "右勾拳", "直拳", "反弹", "小猩猩", "双层防御", "冰冻", "大猩猩", "致命一击", "解雇"];

// 困难模式:去除了计数器面板

// 开始游戏
function startGame() {
    fetch('http://localhost:8080/api/startGame', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ startGame : "1" }) 
    })
    .then(response => response.json())
    .then(data => {
        if (data != null) {
            document.getElementById("battle-log-text").innerText = "请选择你第一回合的招式。";
            document.getElementById("p1-action-billboard").innerText = "等候选择...";
            document.getElementById("p2-action-billboard").innerText = "等候出招...";
            document.getElementById("start").disabled = true;
            isGameStarted = true;
            
            document.getElementById("p1-status").innerText = "对局中";
            document.getElementById("p2-status").innerText = "对局中";            
            disableAllButtons();
            enableButtons(data.availableMoves);
        }
    })
    .catch(err => {
        document.getElementById("battle-log-text").innerText = "无法连接到后端，请确保 Spring Boot 已启动。";
    });
}

// 玩家选择招式，传输到后端，后端java内部权重随机数揭开招式返回，并渲染到前端
function makeMove(moveID) {
    let requestBody = {
        gameStarted: isGameStarted,
        move: moveID
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
        let actualP1Move = (data.p1moveID !== undefined && data.p1moveID !== null) ? data.p1moveID : moveID;
        
        if (actualP1Move >= 0 && actualP1Move <= 14) {
            document.getElementById("p1-action-billboard").innerText = moves[actualP1Move];
        } else {
            document.getElementById("p1-action-billboard").innerText = "🥶 被冰冻空过";
        }


        // 分析电脑出了招法
        if (data.p2moveID !== -1 && data.p2moveID != null) {
            document.getElementById("p2-action-billboard").innerText = moves[data.p2moveID];
        } else {
            // data.p2moveID == -1 -> 对面被冰冻
            document.getElementById("p2-action-billboard").innerText = "🥶 被冰冻空过";
        }

        // 更新战报
        document.getElementById("battle-log-text").innerText = data.roundResult;

        // 查看游戏是否需要继续
        if (data.shouldContinue === true) {
            if (data.statusCode === 2) {
                // 我方下回合被冰冻状态
                document.getElementById("p1-status").innerText = "🥶 被冰冻";
                document.getElementById("p1-status").style.backgroundColor = "#2980b9";
                document.getElementById("battle-log-text").innerText += " (你被冰冻了！2秒后自动空过此轮...)";
                
                // 留出1.5秒空档给玩家看清战报，然后用代码发送 -1 空过
                setTimeout(() => { makeMove(-1); }, 2000);
            } else if (data.statusCode === 3) {
                // 我方下回合被解雇状态
                document.getElementById("p1-status").innerText = "📝 被解雇";
                document.getElementById("p1-status").style.backgroundColor = "#e74c3c";
                enableButtons(data.availableMoves); // 此时后端只会发 [1]，逼玩家点防御
            } else {
                // 正常状态
                document.getElementById("p1-status").innerText = "对局中";
                document.getElementById("p1-status").style.backgroundColor = "#27ae60";
                enableButtons(data.availableMoves);
            }
        } else {
            // 胜负已分
            document.getElementById("battle-log-text").innerText = `${data.roundResult}`;
            disableAllButtons();
            document.getElementById("start").disabled = false;
            document.getElementById("p1-status").innerText = "准备就绪";
            document.getElementById("p2-status").innerText = "准备就绪";            
            isGameStarted = false;
        }
    })
    .catch(err => {
        console.error(err);
        document.getElementById("battle-log-text").innerText = "运行时数据传输崩溃，请检查后端报错日志。";
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

// 使所有按钮无法点击
function disableAllButtons() {
    for (let index = 0; index < 15; index++) {
        document.getElementById("Button" + index).disabled = true;                
    }
}

// enableButtons([0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]); // 测试前端颜色用