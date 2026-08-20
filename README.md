# Meou

> Minecraft 1.21.1 に、プレイヤーを支える猫型コンパニオンを追加する Fabric mod

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-5b7f3a)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Mod%20loader-Fabric-dbd0b3)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-21-e76f00)](https://adoptium.net/)

Meou（ミュー）は、プレイヤーを「パートナー」として追従し、状況に応じて設定したスキルを自動で使うコンパニオンです。

## できること

- プレイヤーを追従し、距離が開いたときはテレポートして合流
- スポーン時に近くのプレイヤーを所有者として設定
- 複数体を所有可能。各個体がプレイヤーの周囲に分散して追従
- 手持ちアイテム 1 スロットと保管用 27 スロットのインベントリ
- 所有者の操作で開ける Items / Skill タブ付き GUI
- GUI から名前と自動発動スキルを変更
- 行動・つぶやき・死亡時の会話と、ランダムな猫の鳴き声
- クリエイティブインベントリから使えるスポーンエッグ

### スキル

Meou は一度に 1 つのスキルを装備し、条件を満たすと自動で発動します。クールダウンと選択中のスキルは保存されます。

| スキル  | 効果                           | 発動条件                           |
| ------- | ------------------------------ | ---------------------------------- |
| Heal    | 毒を解除し、再生効果を付与     | プレイヤーの体力が少ない、毒、炎上 |
| Cheer   | 移動速度上昇を付与             | 近くに敵がいる                     |
| Collect | 周囲に落ちたアイテムを回収     | 近くに落ちたアイテムがある         |
| Alert   | 周囲の敵を発光させる           | 近くに敵がいる                     |
| Light   | 松明を消費して暗い場所を照らす | 暗所かつ松明を所持                 |
| Attack  | 近くの敵を攻撃                 | 近くに敵がいる                     |

Attack は手持ち武器の攻撃力を利用します。

## 導入

現在は開発版です。リリースページに配布 jar が公開されていない場合は、ソースからビルドしてください。

1. Minecraft 1.21.1 用の Fabric Loader と Fabric API をインストールします。
2. `build/libs/` に生成された `meou-*.jar` を、Fabric プロファイルの `mods/` フォルダーへコピーします。
3. Minecraft を起動し、スポーンエッグまたはコマンドで Meou を召喚します。

## 開発環境

必要なもの:

- JDK 21
- Git

依存関係は Gradle が自動で取得します。Windows では `gradlew.bat`、macOS / Linux では `./gradlew` を使用してください。

```powershell
# Windows PowerShell
.\gradlew.bat build
.\gradlew.bat runClient
```

```bash
# macOS / Linux
./gradlew build
./gradlew runClient
```

生成物は `build/libs/` に出力されます。Minecraft のソースを IDE から参照したい場合は、次を実行します。

```bash
./gradlew genSources
```

## プロジェクト構成

```text
src/main/java/       サーバー共通のエンティティ、AI、スキル、GUI、通信
src/client/java/     クライアント専用の画面、モデル、レンダラー
src/main/resources/  mod metadata、言語ファイル、テクスチャ
```

主な実装:

- `MeouEntity`: コンパニオン本体と所有者・インベントリの管理
- `FollowCompanionGoal`: 追従、複数体の隊列、テレポート
- `SkillAutoTriggerGoal`: スキルの条件判定と自動発動
- `MeouScreenHandler` / `MeouScreen`: インベントリとスキル設定 GUI
- `MeouDialogue`: 会話、クールダウン、鳴き声
