# Minecraft Companion Meou — Fabric Mod (MC 1.21.1)

コンパニオン猫 "Meou" を追加するマインクラフト Fabric モッド。外部 AI / Python ブリッジは一切使わない。
スペック・機能は [README.md](README.md) を参照。

---

## 目次
1. [クイックスタート](#クイックスタート)
2. [ビルド / 実行](#ビルド--実行)
3. [ソース構成](#ソース構成)
4. [キーコンセプト](#キーコンセプト)
5. [実装状況](#実装状況)
6. [Mixin 注意](#mixin-注意)
7. [トラブルシューティング](#トラブルシューティング)

---

## クイックスタート

**初回セットアップ（必須）**
```powershell
# PowerShell で実行（cmd.exe は不可）
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.2"  # JDK 21 を明示的に設定
.\gradlew.bat runClient  # ゲーム起動；初回は時間がかかる
```

**よく使うコマンド**
| タスク | コマンド | 説明 |
|--------|---------|------|
| ビルド | `.\gradlew.bat build` | JAR を生成（`build/libs/` に出力） |
| ゲーム実行 | `.\gradlew.bat runClient` | 開発用クライアントを起動 |
| サーバー起動 | `.\gradlew.bat runServer` | 開発用サーバーを起動 |
| ソース生成 | `.\gradlew.bat genSources` | Minecraft ソースを逆コンパイル（必要な場合） |

**重要**: クラス名・メソッド名は **Mojang 公式マッピング** を使用。Yarn ではない。

---

## ビルド / 実行

- **Mojang 公式マッピングを使用**（Yarn ではない）。クラス名は Mojang 名（`PathfinderMob` など）。メソッド名を調べるには `.\gradlew.bat genSources` で生成されたソースを参照すること。
- **JDK 21 必須**。`java` のデフォルトは JDK 17 なので、起動前に `$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.2"` を設定すること。
- Windows では `.\gradlew.bat`（`./gradlew` は pwsh で実行不可）。コマンド:
  - `.\gradlew.bat build` — ビルド（CI `.github/workflows/build.yml` は JDK 25 で同じ `build` を実行）
  - `.\gradlew.bat runClient` / `runServer` — ゲーム起動（`run/` は gitignore 済みの開発用ディレクトリ）
  - `.\gradlew.bat genSources` — Minecraft ソース生成
- テストタスクは無い（`test NO-SOURCE`）。

## ソース構成（Loom `splitEnvironmentSourceSets()`）

- `src/main/java` = common/サーバー側。エンティティ、AI Goal、登録、Mixin。
- `src/client/java` = クライアント限定。モデル、レンダラー、GUI。エントリポイントは `com.meou.client.MeouClient`。
- クライアント限定クラス（`net.minecraft.client.*` や Fabric クライアント API）は `src/main` から参照しない。モデル/レンダラー登録は `MeouClient.onInitializeClient()` で行う。
- エントリポイント: `com.meou.Meou`（main）。ResourceLocation は `Meou.id(path)` ヘルパーを使用。
- エンティティ・属性の登録は `ModEntityTypes`（static field + `registerAll()`）。新規エンティティはここに追加。
- アイテムの登録は `com.meou.item.ModItems`（static field + `registerAll()`、クリエイティブタブ登録も `Meou.onInitialize()` から）。

## キーコンセプト

### NBT 永続化パターン
`MeouEntity` で使用する NBT キー：
| キー | 型 | 用途 |
|------|-----|------|
| `"Owner"` | UUID | オーナープレイヤーの UUID |
| `"SelectedSkill"` | String | 選択スキル（enum の `.getKey()` 値、例 `"attack"`） |
| `"SkillCooldown"` | Int | 残りクールダウン (tick) |
| inventory items | List | 手持ち + 保管庫（`ContainerHelper` で管理） |

**重要**: `SelectedSkill` は enum 名ではなく `getKey()` で保存。NBT ロード時は `MeouSkill.byKey()` で復元。

### ネットワーク通信（C2S Payload）
スキル変更・名前変更はカスタムペイロードで実装。すべて `Meou.registerPayloads()` で登録：
- `SkillSelectPayload(entityId, skillOrdinal)` — スキル変更
- `RenamePayload(entityId, newName)` — 名前変更

クライアント側は `MenuScreens.register()` で `MeouScreen` を登録し、ボタン・入力フィールドから `ClientPlayNetworking.send()` で送信。

### リソースロケーション（ID）
すべて `Meou.id(path)` ヘルパーを使用：
```java
Meou.id("meou_spawn_egg")  // → ResourceLocation("meou", "meou_spawn_egg")
```

### 翻訳キー規約
```
dialogue.meou.<prefix>.<n>        (チャット会話。prefix: heal, cheer, death など)
skill.meou.<key>.desc             (スキル説明。key: heal, cheer, collect など)
container.meou.meou               (画面タイトル)
tab.meou.inventory / tab.meou.skill
```

---

## 実装状況（README の「最終形」とは現状が異なる）

- **実装済み**: `MeouEntity`（`PathfinderMob` 継承、owner UUID を NBT キー `Owner` に保存、`setPersistenceRequired()`、owner 未割当時5秒でデスポーン）、`FollowCompanionGoal`（追従 + 遠距離時テレポート）、アイテム手持ち + 27スロット保管庫（`MeouScreenHandler` / `MeouScreen`、Shift+右クリックで開く）、スキルシステム（`entity/skill/` パッケージ: `MeouSkill` enum **6種**（`HEAL`/`CHEER`/`COLLECT`/`ALERT`/`LIGHT`/`ATTACK`）+ `SkillAutoTriggerGoal`、選択スキルとクールダウンは NBT キー `SelectedSkill` / `SkillCooldown` に永続化、デフォルト `HEAL`）、**タブ式スキル選択 GUI と `CustomPayload` 通信**（`SkillSelectPayload` / `RenamePayload` を `Meou.onInitialize()` の `registerPayloads()` で C2S 登録、`MeouScreen` から送信、クライアント側は `MenuScreens.register` で `MeouScreen::new`）、**スポーンエッグ**（`ModItems.MEOU_SPAWN_EGG`、`CreativeModeTabs.SPAWN_EGGS` に登録）、**手持ちアイテムのレンダリング**（`MeouModel` が `ArmedModel` 実装 + `MeouRenderer` に `ItemInHandLayer`）、**死亡時セリフ**（`MeouDialogue.sayDeath()`、スパム防止チェックなしで必ず1回発言）。
- **ATTACK スキルの仕組み（注意）**: `MeouSkill.ATTACK` は `companion.setTarget()` + `MeouEntity.setAttackModeTicks()`（100tick）で戦闘モードに入り、`MeouEntity` 内の専用 `MeleeAttackGoal`（`attackModeTicks` が 0 以外のときのみ `canUse`）で近接攻撃する。手持ちアイテムのダメージを `updateHeldAttackDamage()` で攻撃力に反映。NBT キー `SelectedSkill` は enum 名ではなく `getKey()`（例 `"attack"`）で保存。
- **LIGHT スキルの仕組み（注意）**: 松明は **Meou 自身の保管庫からだけ**消費する（プレイヤーインベントリは見ない）。設置位置は「空気ブロックかつ足元が固体（`belowState.isSolid()`）」かつ暗い場所のみ。
- **セリフの仕組み**: `MeouDialogue` の `LINE_COUNTS` に翻訳キーごとの行数（`dialogue.meou.<prefix>.<n>`）を定義。`say()` は 60tick（3秒）のスパム防止あり、`sayDeath()` はなし。`death` は日英各4種。
- **調整済みの数値**: 全スキルのクールダウン短縮（HEAL 40tick / CHEER 60 / COLLECT 30 / ALERT 60 / LIGHT 60 / ATTACK 80）、移動速度 `0.32D`（プレイヤーより少し速い）。
- **複数体管理**: 同一プレイヤーが複数体所持可。`FollowCompanionGoal` が各 Meou の UUID から決定的に導出した擬似ランダム角度で、オーナー周囲半径5.0ブロックのリング状配置（目標地点）へ追従・テレポート。`FORMATION_RADIUS` と `deriveFormationAngle()` 参照。
- **鳴き声**: ランダム間隔（300〜900tick = 1分に約0.5〜1.5回）でバニラの猫サウンド（`CAT_AMBIENT`/`CAT_PURR`/`CAT_PURREOW`）をランダム再生。ピッチは0.8〜1.2にランダム化。チャットセリフとは独立（別系統）。`ModSounds.MEOU_MEOWS` と `MeouEntity.tryMeow()` 参照。
- **未実装**: サウンド、カスタム猫型モデル、独り言（追加予定: 1分に1〜2回、`sayMumble` 予定）。README の「Out of Scope」フェーズ3機能は未実装。

## Mixin

- `src/main/resources/meou.mixins.json` / `src/client/resources/meou.client.mixins.json` はテンプレート由来の `ExampleMixin` / `ExampleClientMixin`（空の `@Inject`）が残っている。
- どちらも `defaultRequire: 1`（`required: true`）のため、ターゲットメソッド名が解決できないと**ビルドではなくゲームロード時にクラッシュ**する。新しい Mixin クラスは必ず対応する JSON に追加すること。

## トラブルシューティング

### ビルド・実行時のエラー

| エラー | 原因 | 解決策 |
|--------|------|--------|
| `No matching variant of com.java_lang:java:21` | JDK バージョンが不適切 | `$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.2"` を設定し、PowerShell で実行 |
| `./gradlew が見つからない` / 実行不可 | cmd.exe で実行した | **必ず PowerShell を使用**。`.\gradlew.bat` は PowerShell でのみ実行可 |
| ゲーム起動時にクラッシュ `Mixin application failed` | Mixin JSON に無効なターゲットメソッドがある | `meou.mixins.json` / `meou.client.mixins.json` の `@Inject` ターゲットを確認；`required: true` の場合は必ずメソッドが存在する必要がある |
| メソッド名が不明 | Mojang マッピングのソースがない | `.\gradlew.bat genSources` で Minecraft ソースを生成し、IDE で検索 |
| `UnsupportedClassVersionError` | Java バージョンが古い | `java -version` で確認；JDK 21 が必須 |

### 開発時によくある問題

**スキルが動作しない**
- `MeouSkill.canTrigger()` の条件を確認。デバッグ用に `System.out.println()` で条件をログ出力
- NBT 永続化時に `selectedSkill = MeouSkill.byKey(nbt.getString("SelectedSkill"))` を使用しているか確認（ordinal ではなく `getKey()` を使う）
- `SkillAutoTriggerGoal` が `goalSelector` に追加されているか確認（[MeouEntity](src/main/java/com/meou/entity/MeouEntity.java) 内）

**GUI が表示されない**
- `MenuScreens.register()` が `MeouClient.onInitializeClient()` で呼び出されているか確認
- `MeouScreen` の `DataSlot` 同期が機能しているか確認（`MeouScreenHandler` の `addDataSlots()` を参照）

**エンティティがデスポーンする**
- owner UUID が設定されているか確認（NBT キー `"Owner"`）
- 未割当時は 5 秒でデスポーン。`MeouEntity.unownedTicks` の値を確認

**複数体の Meou が重なる**
- `FollowCompanionGoal` の `FORMATION_RADIUS = 5.0` を調整可能
- 各 Meou の UUID から決定的に導出された角度が異なるはず。`deriveFormationAngle()` を確認

### よくある実装ミス

- ❌ `SelectedSkill` を `nbt.putString("SelectedSkill", selectedSkill.ordinal())` で保存
  - ✅ `nbt.putString("SelectedSkill", selectedSkill.getKey())` を使用
- ❌ `src/main` から `net.minecraft.client.*` をインポート
  - ✅ クライアント限定コードは `src/client` に配置；`src/main` から参照しない
- ❌ ResourceLocation を直接文字列で作成
  - ✅ 常に `Meou.id(path)` を使用
- ❌ Mixin 登録後に `@Inject` ターゲットメソッドが存在しないまま起動
  - ✅ 不要な Mixin は JSON からコメントアウトするか、テンプレートから削除

