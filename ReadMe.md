# GRPC Server
簡單的遊戲範例，目前沒有做到thread safe的處理。

## 建置專案
```
mvn -DskipTests package
```

## 啟動專案
直接啟動Main Class

## 德州撲克Android專案
https://github.com/Shark0/SharkGame

## 遊戲
### 剪刀石頭布
單機遊戲範例，遊戲內容就是跟電腦玩剪刀石頭布。
房間負責收到Client請求隨機產生結果，可以修改成水果機、連線遊戲。
透過一個gRpc Request實作，一個Request就回傳一個Response。

### 紅黑大戰
狀態機遊戲，遊戲內容是電腦有紅方跟黑方，每一局都會抽一張卡，玩家就押注紅贏或黑贏。
房間負責狀態改變，接收Client請求設定押注內容，可以修改成百家樂、魚蝦蟹、色蝶，捕魚。

狀態可以簡單區分成下列狀態。
* 發牌
* 下注
* 開獎
* 發獎

透過兩個gRpc Request實作。
* 接收房間狀態的Request
* 影響房間狀態的Request

### 德州撲克
座位遊戲，房間負責管理玩家排隊、回合動作、以及座位輪流操作，接收Client請求設定押注內容。

透過兩個gRpc Request實作。
* 接收房間狀態的Request
* 影響房間狀態的Request