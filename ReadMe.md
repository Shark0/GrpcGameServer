# GRPC Server
簡單的遊戲範例，目前沒有做到thread safe的處理。

## 啟動專案
```
mvn clean package
java -jar .\target\GameServer-1.0-SNAPSHOT.jar
```

## 建置專案 Grpc服務
```
mvn -DskipTests package
```

## 前端Android專案
https://github.com/Shark0/SharkGame

## 遊戲
### 德州撲克
座位遊戲，房間負責管理玩家排隊、回合動作、以及座位輪流操作，接收Client請求設定押注內容。

透過兩個gRpc Request實作。
* 接收房間狀態的Request
* 影響房間狀態的Request