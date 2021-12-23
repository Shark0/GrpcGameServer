# Game Server
簡單的遊戲範例。

## 建置專案 Grpc服務
```
mvn -DskipTests package
```

## 啟動專案
```
mvn clean package
java -jar .\target\GameServer-1.0-SNAPSHOT.jar
```

## 前端Android專案
https://github.com/Shark0/SharkGame

## 遊戲
### 德州撲克
座位遊戲，房間負責管理玩家排隊、回合動作、以及座位輪流操作，接收Client請求設定押注內容。
透過兩個gRpc Request實作。
* 接收房間狀態的Request
* 影響房間狀態的Request
#### 牌
由0 - 51數字組成，每個數字有自己的牌的號碼跟牌的花色，公式如下
* 牌號 = 數字%13，0是2、1是3、2是4、3是5、4是6、5是7、6是8、7是9、8是10、9是J、10是Q、11是K、12是A
* 牌色 = 數字/13，0是梅花、1是方塊、2是愛心、3是黑桃

## gRPC
### 登入 
LoginService.proto
#### 登入請求 
* playerName 玩家姓名，因為是範例，所以當作帳號用

#### 登入回傳
* status 登入狀態，可以不管，都會登入成功
* token 使用者token，
* name 玩家名稱
* money 錢

### 德州鋪克 - 房間狀態 
TexasHoldemGameService.proto

#### 狀態請求
* token 登入時獲得的token

#### 狀態回應
負責回傳房間目前的狀態，status是回傳狀態類型，message是回傳資料內容。
* status 0: 回傳入座座號
```
//message
"0" //座位號碼，從0到11
```
* status 1: 回傳從座位站起來，有機會沒錢時被請起
* status 2: 目前沒有座位
* status 3: 座位資訊
```
//message
{
    "name": "Shark", //玩家名稱
    "id: 0, //座位編號
    "status": 0, //座位狀態 0 = 等待遊戲開始、1 = 遊戲中、2 = 離開遊戲、1 = 站起
    "action": 0, //玩家操作 -1 = 沒有操作、0 = 離開、1 = 跟注、2 = 加注、3 = 全下、4 = 棄牌、5 = 站起、6 = 坐下
    "money": 0, //玩家剩餘金額
    "roundBet": 0 // 玩家這回合下注金額
}
```
* status 4: 進房資訊
```
//message
{
    "roomBet": 0 //目前遊戲總下注金額,
    "publicCardList: [0,1,2,3,4], //公牌，由0-51，梅花二到黑桃A
    "seatIdSeatMap": {
        0: { //座位編號
            "name": "Shark", //玩家名稱
            "id: 0, //座位編號
            "status": 0, //座位狀態 0 = 等待遊戲開始、1 = 遊戲中、2 = 離開遊戲、1 = 站起
            "action": 0, //玩家操作 -1 = 沒有操作、0 = 離開、1 = 跟注、2 = 加注、3 = 全下、4 = 棄牌、5 = 站起、6 = 坐下
            "money": 0, //玩家剩餘金額
            "roundBet": 0 // 玩家這回合下注金額
        }
    } //座位狀態 0 = 等待遊戲開始、1 = 遊戲中、2 = 離開遊戲、1 = 站起
    "bigBlindBet": 0, //大盲注下注金額
    "smallBlindSeatId": 0, //小盲注座位號碼
    "bigBlindSeatId": 0 //大盲注座位號碼
    "currentOperationSeatId": 0 //目前要動作的座位號碼
    "sceneStatus": 0 //場景狀態，0 = 等待遊戲開始、1 = 發牌、2 = 翻牌、3 = 轉牌、4 = 河牌、5 = 開牌、6 = 分配賭池
}
```
* status 5: 房間資訊，同4
* status 6: 檢查前端是否存活
* status 7: 場景狀態改變
```
//message
"0" //場景狀態，0 = 等待遊戲開始、1 = 發牌、2 = 翻牌、3 = 轉牌、4 = 河牌、5 = 開牌、6 = 分配賭池
```
* status 8: 座位手牌，只顯示有回傳的
```
//message
{
    0: { //座位編號
        "cardType": 0, //牌型，0 = 高牌、1 = 一對、2 = 二對、3 = 三條、4 = 順子、5 = 同花、6 = 葫蘆、7 = 鐵支、8 = 同花順、9 = 皇家同花順
        "cardList": [0,51] //玩家手牌，由0-51，梅花二到黑桃A
    }
}
```
* status 9: 開始行動
```
//message
{
    "seatId": 0, //座位Id,
    "maxRaiseBet": 0, //可加注最大值
    "minRaiseBet": 0, //可加注最小值
    "isCanRaise": true, //是否可加注
    "callBet": 0, //跟注值
    "lastOperationTime": 150000 //可思考操作毫秒
}
```
* status 10: 等待位置行動
```
//message
{
    "seatId": 0, //座位Id,
    "lastOperationTime": 150000 //可思考操作毫秒
}
```
* status 11: 位置行動內容
```
//message
{
    "seatId": 0, //座位Id,
    "lastOperationTime": 150000 //可思考操作毫秒
}
```
* status 12: 公牌  
```
//message
[0, 1, 2, 3, 4, 5] //Card Array 
```

* status 13: 賭池獎勵
```
//message
[
    {
        "winnerSeatIdList": [0, 1], //贏池座位Id Array
        "winPot": 50 //賭池金額
    },
    {
        "winnerSeatIdList": [0, 1],
        "winPot": 50
    }
]
```

### 德州鋪克 - 操作
#### 操作請求
* token 登入時獲得的token
* operation 玩家操作 0 = 離開、1 = 跟注、2 = 加注、4 = 棄牌、5 = 站起、6 = 坐下
* bet 跟注或加注金額
#### 操作回傳
* status 是否操作成功，成功回傳1不成功回傳-1
* message 訊息