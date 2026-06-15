let isGameStarted = false;
let moves = ["挑衅", "防御", "左避", "右避", "上勾拳", "左勾拳", "右勾拳", "直拳", "反弹", "小猩猩", "双层防御", "冰冻", "大猩猩", "致命一击", "解雇"];

const counterNames = ["累计挑衅", "未用挑衅", "未用防御", "冰冻蓄力", "猩猩蓄力", "致命蓄力", "解雇蓄力"];

// 渲染计数器面板
function updateCountersDisplay(countersArray) {
    let htmlContent = "";
    if (countersArray && countersArray.length === 7) {
        for (let i = 0; i < countersArray.length; i++) {
            htmlContent += `<div class="counter-item">${counterNames[i]}: <strong>${countersArray[i]}</strong></div>`;
        }
    }
    document.getElementById("p1-counters").innerHTML = htmlContent;
}

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
            document.getElementById("battle-log-text").innerText = "📢 【第一回合】 双方大喊：‘波！波！’ 自动打出波波挑衅蓄力！";
            document.getElementById("bobo-action-billboard").innerText = "等候出招...";
            document.getElementById("start").disabled = true;
            isGameStarted = true;
            
            document.getElementById("p1-status").innerText = "正常";
            document.getElementById("p1-status").style.backgroundColor = "#27ae60";
            updateCountersDisplay(data.counters);
            
            disableAllButtons();
            enableButtons(data.availableMoves);
        }
    })
    .catch(err => {
        document.getElementById("battle-log-text").innerText = "💥 无法连接到后端，请确保 Spring Boot 已启动。";
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
        // 1. 看板解密：电脑出了什么波波动作
        if (data.p2moveID !== -1 && data.p2moveID != null) {
            document.getElementById("bobo-action-billboard").innerText = moves[data.p2moveID] + " 💥";
        } else {
            // data.p2moveID == -1 -> 对面被冰冻
            document.getElementById("bobo-action-billboard").innerText = "🥶 被冰冻空过";
        }

        // 2. 战报直接打在荧光大屏上
        document.getElementById("battle-log-text").innerText = data.roundResult;

        // 3. 刷新数据面板
        updateCountersDisplay(data.counters);

        // 4. 回合制状态机控制流
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
            document.getElementById("battle-log-text").innerText = `🏁 游戏结束！${data.roundResult}`;
            disableAllButtons();
            document.getElementById("start").disabled = false;
            isGameStarted = false;
        }
    })
    .catch(err => {
        console.error(err);
        document.getElementById("battle-log-text").innerText = "💥 运行时数据传输崩溃，请检查后端报错日志。";
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