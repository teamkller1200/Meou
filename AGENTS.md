# AGENTS.md

Minecraft Fabric モッド `meou`（コンパニオン "Meou"、MC 1.21.1）。外部 AI / Python ブリッジは一切使わない。仕様は `README.md`（英語）参照。

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

## 実装状況（README の「最終形」とは現状が異なる）

- **実装済み**: `MeouEntity`（`PathfinderMob` 継承、owner UUID を NBT キー `Owner` に保存、`setPersistenceRequired()`、owner 未割当時5秒でデスポーン）、`FollowCompanionGoal`（追従 + 遠距離時テレポート）、アイテム手持ち + 27スロット保管庫（`MeouScreenHandler` / `MeouScreen`、Shift+右クリックで開く）、スキルシステム（`entity/skill/` パッケージ: `MeouSkill` enum **6種**（`HEAL`/`CHEER`/`COLLECT`/`ALERT`/`LIGHT`/`ATTACK`）+ `SkillAutoTriggerGoal`、選択スキルとクールダウンは NBT キー `SelectedSkill` / `SkillCooldown` に永続化、デフォルト `HEAL`）、**タブ式スキル選択 GUI と `CustomPayload` 通信**（`SkillSelectPayload` / `RenamePayload` を `Meou.onInitialize()` の `registerPayloads()` で C2S 登録、`MeouScreen` から送信、クライアント側は `MenuScreens.register` で `MeouScreen::new`）、**スポーンエッグ**（`ModItems.MEOU_SPAWN_EGG`、`CreativeModeTabs.SPAWN_EGGS` に登録）、**手持ちアイテムのレンダリング**（`MeouModel` が `ArmedModel` 実装 + `MeouRenderer` に `ItemInHandLayer`）、**死亡時セリフ**（`MeouDialogue.sayDeath()`、スパム防止チェックなしで必ず1回発言）。
- **ATTACK スキルの仕組み（注意）**: `MeouSkill.ATTACK` は `companion.setTarget()` + `MeouEntity.setAttackModeTicks()`（100tick）で戦闘モードに入り、`MeouEntity` 内の専用 `MeleeAttackGoal`（`attackModeTicks` が 0 以外のときのみ `canUse`）で近接攻撃する。手持ちアイテムのダメージを `updateHeldAttackDamage()` で攻撃力に反映。NBT キー `SelectedSkill` は enum 名ではなく `getKey()`（例 `"attack"`）で保存。
- **LIGHT スキルの仕組み（注意）**: 松明は **Meou 自身の保管庫からだけ**消費する（プレイヤーインベントリは見ない）。設置位置は「空気ブロックかつ足元が固体（`belowState.isSolid()`）」かつ暗い場所のみ。
- **セリフの仕組み**: `MeouDialogue` の `LINE_COUNTS` に翻訳キーごとの行数（`dialogue.meou.<prefix>.<n>`）を定義。`say()` は 60tick（3秒）のスパム防止あり、`sayDeath()` はなし。`death` は日英各4種。
- **調整済みの数値**: 全スキルのクールダウン短縮（HEAL 40tick / CHEER 60 / COLLECT 30 / ALERT 60 / LIGHT 60 / ATTACK 80）、移動速度 `0.32D`（プレイヤーより少し速い）。
- **複数体管理**: 同一プレイヤーが複数体所持可。`FollowCompanionGoal` が各 Meou の UUID から決定的に導出した擬似ランダム角度で、オーナー周囲半径5.0ブロックのリング状配置（目標地点）へ追従・テレポート。`FORMATION_RADIUS` と `deriveFormationAngle()` 参照。
- **鳴き声**: ランダム間隔（600〜1200tick = 1分に1〜2回）でバニラの猫サウンド（`CAT_AMBIENT`/`CAT_PURR`/`CAT_PURREOW`）をランダム再生。ピッチは0.8〜1.2にランダム化。チャットセリフとは独立（別系統）。`ModSounds.MEOU_MEOWS` と `MeouEntity.tryMeow()` 参照。
- **未実装**: サウンド、カスタム猫型モデル、独り言（追加予定: 1分に1〜2回、`sayMumble` 予定）。README の「Out of Scope」フェーズ3機能は未実装。

## Mixin

- `src/main/resources/meou.mixins.json` / `src/client/resources/meou.client.mixins.json` はテンプレート由来の `ExampleMixin` / `ExampleClientMixin`（空の `@Inject`）が残っている。
- どちらも `defaultRequire: 1`（`required: true`）のため、ターゲットメソッド名が解決できないと**ビルドではなくゲームロード時にクラッシュ**する。新しい Mixin クラスは必ず対応する JSON に追加すること。
