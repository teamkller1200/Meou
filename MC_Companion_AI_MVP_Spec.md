# Minecraft LLM Companion AI - MVP (Minimum Viable Product) 仕様書

## 1. 概要 & 目的
本プロジェクトは、LLM（大規模言語モデル）を活用して「状況を解釈し、プレイヤーに寄り添って行動する相棒」をMinecraft内で実現するためのMVP（最小限の実証モデル）開発仕様書である。

毎Tickのリアルタイム物理制御（移動・攻撃など）はJava側の標準機能（Pathfinder/Goal）に任せ、LLMは**「状況判断・意思決定・対話（思考層）」**に特化させる階層型アーキテクチャを採用する。

---
## 2. MVPのスコープ（実装機能）

### ⭕ MVPに含める機能
1. **プレイヤー追従 & 基本動作**
   * プレイヤーの後方を一定距離（2〜3ブロック）保って追従する。
   * プレイヤーが遠く離れた場合はテレポート処理で追従漏れを防止。
2. **コンテキスト認識型チャット機能**
   * プレイヤーのステータス（HP、満腹度、所持メインアイテム）および周囲環境（時間帯、バイオーム、周囲の敵モブ数）を把握。
   * プレイヤーからのチャット発言に対して、現在の文脈を踏まえた応答を返す。
3. **簡易な状況リアクション（自律トリガー）**
   * 危険検知（例: 夜間 + プレイヤーHP低下 + 敵モブ接近）時に、頼まれなくても自律的に警告セリフを発話し、逃走または警戒モードへ移行。
4. **非同期バックグラウンド思考**
   * LLMの推論待ち（1〜3秒）の間もゲーム処理を止めることなく、MOD側で「あたりを見回す」などの待機モーションを実行。

### ❌ MVPでは見送る機能（フェーズ2以降）
* 複雑な建築手伝い（Schematicの配置など）
* 高度なアイテム管理・チェスト自動整理
* RAG / Vector DBを用いた長期記憶の保存
* 音声入力（Whisper）および音声合成（VOICEVOX等）との連携

---

## 3. システム構成 & アーキテクチャ

```text
+------------------------------------+
|  Minecraft Client / Server (Java)  |
|  - EntityCompanion (Custom Mob)    |
|  - State Collector (HP, Env)       |
|  - Goal / Pathfinder System        |
+-----------------+------------------+
                  | (WebSocket / HTTP JSON)
                  v
+------------------------------------+
|  Python Local Bridge Server        |
|  - FastAPI / async Communication   |
|  - Prompt Formatter                |
|  - Schema Validation (Pydantic)    |
+-----------------+------------------+
                  | (API Call)
                  v
+------------------------------------+
|  LLM Provider                      |
|  - Local: Ollama (Llama 3 / Gemma) |
|  - Cloud: Gemini API / OpenAI API  |
+------------------------------------+
```

---

## 4. データ連携仕様（JSON Schema）

### 4.1. Java -> Python (Context State Payload)
定期更新（5秒周期）またはプレイヤーチャット発言時にJava MODからPython Bridgeへ送信されるコンテキストデータ。

```json
{
  "timestamp": 1713182400,
  "trigger": "CHAT_MESSAGE", // "PERIODIC_TICK" または "CHAT_MESSAGE"
  "player": {
    "name": "Steve",
    "hp": 8,
    "max_hp": 20,
    "food_level": 12,
    "main_hand_item": "minecraft:iron_sword"
  },
  "environment": {
    "dimension": "minecraft:overworld",
    "biome": "minecraft:plains",
    "time_of_day": "night",
    "light_level": 4,
    "nearby_mobs": [
      {"type": "minecraft:zombie", "distance": 6.5},
      {"type": "minecraft:skeleton", "distance": 12.0}
    ]
  },
  "chat_message": "ちょっとお腹減ったな..."
}
```

### 4.2. Python -> Java (Action Command Response)
LLMの推論結果をPython Bridgeでパースし、Java MODへ送信する行動命令。

```json
{
  "dialogue": "無理しないで！ゾンビも近くにいるから、一旦松明を置いて身を守ろう！",
  "action": {
    "type": "DEFEND_PLAYER", // FOLLOW, FLEE, ATTACK, DEFEND_PLAYER, IDLE
    "target_position": null,
    "urgency": "HIGH"
  }
}
```

---

## 5. 技術スタック

| コンポーネント | 採用技術 / ライブラリ | 備考 |
| :--- | :--- | :--- |
| **Minecraft Mod Core** | Fabric / Forge (Java 17/21) | バージョン 1.20.x 推奨 |
| **Bridge Server** | Python 3.11+, FastAPI, uvicorn | 非同期HTTP通信の確保 |
| **LLM Inference** | Ollama (ローカル) または Gemini API | 低遅延応答のための軽量モデル |
| **通信プロトコル** | WebSocket または HTTP REST | 双方向リアルタイム性の確保 |

---

## 6. 実装ロードマップ (MVP Milestone)

1. **Step 1: Java MOD側 カスタムEntity & 追従Goalの作成**
   * 独自モブ（コンパニオン）を出現させ、プレイヤーを追従する標準Goalを実装。
2. **Step 2: Python Bridge 簡易サーバー構築**
   * JSONを受け取り、固定のテストダミー応答を打ち返すエンドポイントの作成。
3. **Step 3: Java <-> Python 通信基盤の実装**
   * 5秒周期でワールド情報をJSONでPython側へPOST送信するTickイベントの追加。
4. **Step 4: LLM API / Ollama 連携 & プロンプト調整**
   * システムプロンプトでペルソナ（相棒の性格）を定義し、JSONフォーマットでの出力（Function Calling / Structured Outputs）を安定化。
5. **Step 5: 結合テスト & 調整**
   * チャット応答と危険検出時のリアクション動作の検証。
