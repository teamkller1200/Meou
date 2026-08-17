# AGENTS.md

Minecraft Fabric モッド `aibots`（コンパニオン "Ailuu"、MC 1.21.1）。外部 AI / Python ブリッジは一切使わない。仕様は `README.md`（日本語）参照。

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
- `src/client/java` = クライアント限定。モデル、レンダラー、GUI。エントリポイントは `com.aibots.client.AibotsClient`。
- クライアント限定クラス（`net.minecraft.client.*` や Fabric クライアント API）は `src/main` から参照しない。モデル/レンダラー登録は `AibotsClient.onInitializeClient()` で行う。
- エントリポイント: `com.aibots.Aibots`（main）。ResourceLocation は `Aibots.id(path)` ヘルパーを使用。
- エンティティ・属性の登録は `ModEntityTypes`（static field + `registerAll()`）。新規エンティティはここに追加。

## 実装状況（README の「最終形」とは現状が異なる）

- **実装済み**: `AiluuEntity`（`PathfinderMob` 継承、owner UUID を NBT キー `Owner` に保存、`setPersistenceRequired()`）、`FollowCompanionGoal`（追従 + 遠距離時テレポート）、アイテム手持ち + 27スロット保管庫（`AiluuScreenHandler` / `AiluuScreen`、Shift+右クリックで開く）、スキルシステム（`entity/skill/` パッケージ: `AiluuSkill` enum 5種 + `SkillAutoTriggerGoal`、選択スキルとクールダウンは NBT キー `SelectedSkill` / `SkillCooldown` に永続化、デフォルト `HEAL`）。
- **未実装**: スキル選択 GUI / `CustomPayload` 通信（スキル選択の仕組み自体は NBT 保存のみ）、スポーンエッグ、サウンド、マルチ体管理、手持ちアイテムのレンダリング。README のファイル構成は目標であり、現在のソースツリーを表していない。

## Mixin

- `src/main/resources/aibots.mixins.json` / `src/client/resources/aibots.client.mixins.json` はテンプレート由来の `ExampleMixin` / `ExampleClientMixin`（空の `@Inject`）が残っている。
- どちらも `defaultRequire: 1`（`required: true`）のため、ターゲットメソッド名が解決できないと**ビルドではなくゲームロード時にクラッシュ**する。新しい Mixin クラスは必ず対応する JSON に追加すること。