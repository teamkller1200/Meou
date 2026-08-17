# Minecraft Companion Ailuu - MVP 仕様書

## 1. 概要 & 目的

本プロジェクトは、Minecraft内で**アイルー風コンパニオン**を追加する Fabric MOD である。『モンスターハンター』シリーズのアイルーをインスパイア元とし、プレイヤーに追従し、設定したスキルを状況に応じて発動してプレイヤーを助ける。

LLM（大規模言語モデル）などの外部AIは一切使用しない。すべての動作はバニラMinecraftのEntity/AIシステムで実現する。

### コンセプトイメージ

- コンパニオン名: **Ailuu**（アイルー）
- 猫型の二足歩行キャラクター（白/クリーム色ベース）
- プレイヤーを「オトモ」として慕い、常に後方を追従する
- プレイヤーが設定したスキルを状況に応じて自動発動する
- 鳴き声は「ニャ」「ニャッ」などの猫語
- 時々あくびをしたり、首をかしげたりするアイドルアニメーションを持つ

---

## 2. MVP スコープ

### 前提機能（基本動作、常時有効）

1. **プレイヤー追従 & テレポート**
   - プレイヤーの後方を 2〜3 ブロック保って追従する
   - プレイヤーが遠く離れた場合はテレポートで追従漏れ防止
   - 既存の `FollowCompanionGoal` を流用（実装済み）

### ⭕ 含める機能

1. **アイテム手持ち・装備**
   - Ailuu がツール（剣、ツルハシなど）を手持ちスロットに装備可能
   - プレイヤーが右クリックでアイテムを渡すと装備する
   - 装備したアイテムは見た目に反映（モデルに手持ち表示）

2. **スキルシステム（1種選択・自動発動）**
   - プレイヤーがAiluuを右クリック→GUIで**1つのスキル**を設定
   - 設定後はAiluuが状況を監視し、条件を満たしたら**自律的にスキル発動**
   - クールダウン制（連続発動防止）
   - 発動中は専用モーションとエフェクト

   | スキル | 効果 | 自動発動トリガー |
   |:---|:---|:---|
   | **回復** | プレイヤーに再生効果IIを3秒付与 | HP ≤ 6 かつクールダウン終了 |
   | **応援** | プレイヤーに移動速度上昇を10秒付与 | ダメージを受けた or 敵接近、かつクールダウン終了 |
   | **採集** | 周囲8ブロックのドロップアイテムを収集 | ドロップアイテムあり、かつアイドル状態 |
   | **警報** | 敵モブを光るエフェクトでマーキング | 敵mobが8ブロック以内に接近 |
   | **照明** | 暗所（light ≤ 7）に松明を設置 | 暗所かつプレイヤーインベントリに松明あり |

3. **複数体の同時管理**
   - 1人のプレイヤーが複数の Ailuu を従えられる
   - 各個体に個別の名前（名札対応）
   - Ailuuごとに異なるスキルを設定可能

### ❌ MVP では見送る機能（フェーズ2以降）

- チェスト整理などの高度なギミック
- プレイヤーをかばう（戦闘参加）
- おすわり機能
- アイドルアニメーション
- 鳴き声（サウンド）
- パーティクル演出
- スポーンエッグ
- カスタムモデル & テクスチャ

---

## 3. システム構成

```
Minecraft Client / Server (Fabric 1.21.1)
├── AiluuEntity (PathfinderMob)
│   ├── FollowCompanionGoal (追従)
│   ├── SkillAutoTriggerGoal (スキル自動発動)
│   │   ├── 回復 / 応援 / 採集 / 警報 / 照明
│   │   └── クールダウン管理
│   └── スキル発動モーション
├── SkillScreen (クライアントGUI)
│   └── ScreenHandler (サーバー連携)
├── CustomPayload (スキル選択ネットワーク)
├── AiluuModel (カスタムモデル)
│   ├── 頭・耳・胴体・尾・4本脚
│   └── アニメーション対応
├── AiluuRenderer (テクスチャ描画)
└── ModSoundEvents (鳴き声定義)
```

**AI/LLM コンポーネントは一切含まない。** Python Bridge サーバーも不要。

---

## 4. 実装ロードマップ（MVP Milestone）

1. **Step 1: エンティティリネーム & クリーンアップ**
   - TestEntity → AiluuEntity, モデル・Renderer も同様に改名
   - bridge/ ディレクトリ削除
   - Aibots.java からの橋渡しコード削除

2. **Step 2: アイテム手持ち・装備**
   - AiluuEntity に手持ちスロット追加
   - プレイヤーからアイテムを受け取るインタラクション
   - モデルへの手持ち表示

3. **Step 3: スキルシステム実装**
   - AiluuSkill インターフェース / enum 定義（5種）
   - 各スキルの発動ロジック（回復 / 応援 / 採集 / 警報 / 照明）
   - SkillAutoTriggerGoal: 状況監視＋自動発動
   - クールダウン管理（NBT保存対応）

4. **Step 4: スキル選択GUI**
   - SkillScreenHandler（サーバー側コンテナ、0スロット）
   - SkillScreen（クライアント側、5ボタン）
   - CustomPayload でのスキル選択通信
   - interactMob() でGUIを開く

5. **Step 5: 複数体管理 & 結合テスト**
   - 複数 Ailuu 管理の動作確認
   - テクスチャ・モデル調整
   - 結合テスト

---

## 5. 技術スタック

| コンポーネント | 採用技術 | 備考 |
| :--- | :--- | :--- |
| **Minecraft Mod Core** | Fabric (Java 21) | 1.21.1 |
| **Entity** | `PathfinderMob` 継承 | バニラAI重用 |
| **モデル** | `HierarchicalModel` + `ModelPart` | CubeListBuilder で構築 |
| **サウンド** | `SoundEvent` レジストリ | `.json` サウンド定義 |
| **GUI** | `ScreenHandler` + `HandledScreen` | Fabric Networking (CustomPayload) |
| **スキル** | `enum` + `interface` | クールダウンは NBT 保存 |

---

## 6. ファイル構成（最終形）

```
src/
├── main/java/com/aibots/
│   ├── Aibots.java                  # Mod エントリポイント
│   ├── entity/
│   │   ├── ModEntityTypes.java      # エンティティ登録
│   │   ├── AiluuEntity.java         # Ailuu エンティティ
│   │   ├── skill/
│   │   │   ├── AiluuSkill.java      # スキル enum (5種)
│   │   │   ├── SkillAutoTriggerGoal.java  # 自動発動Goal
│   │   │   └── SkillRegistry.java   # スキル効果定義
│   │   └── ai/
│   │       └── FollowCompanionGoal.java
│   ├── screen/
│   │   ├── AiluuScreenHandler.java  # インベントリGUI (実装済み)
│   │   ├── ModMenuTypes.java        # メニュータイプ登録
│   │   └── SkillPayload.java        # カスタムパケット
│   └── item/
│       └── ModItems.java            # スポーンエッグ
├── client/java/com/aibots/client/
│   ├── AibotsClient.java            # クライアントエントリ
│   ├── screen/
│   │   ├── AiluuScreen.java         # インベントリGUI (実装済み)
│   │   └── SkillScreen.java         # スキル選択GUI
│   ├── model/
│   │   └── AiluuModel.java          # カスタム猫モデル
│   └── renderer/
│       └── AiluuRenderer.java       # テクスチャ描画
└── main/resources/
    ├── assets/aibots/
    │   ├── sounds.json               # サウンド定義
    │   └── textures/entity/
    │       └── ailuu.png
    └── fabric.mod.json
```
