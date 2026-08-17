---
name: mc-source-lookup
description: "Mojang マッピングの Minecraft ソースコードを参照する。このプロジェクト（aibots, Fabric 1.21.1, Mojang mappings）のビルド環境で、クラスのメソッドシグネチャ（戻り値・引数）を確認したいとき、Mojang 名のメソッドが存在するか調べたいとき、javap でクラスを解析する手順を提供する。使うタイミング: クラス名やメソッド名を指定して「Inventory.add の戻り値は？」「Container に addItem はある？」「LivingEntity のメソッド一覧」といった質問があったとき。"
---

# Minecraft Source Lookup (Mojang Mappings)

このスキルは、Mojang 公式マッピングでビルドされた Minecraft 1.21.1 のクラス情報を `javap` で取得する手順を提供する。

## 前提

- JDK 21 の `javap` が使えること（`$env:JAVA_HOME` が設定されていること）
- `.\gradlew.bat genSources` が一度実行済みであること（deobf jar がキャッシュに存在する）

## 手順

### 1. 便利スクリプトを使う（推奨）

`scripts/mc-lookup.py` が deobf jar のパスを自動解決して `javap` を実行する。

```pwsh
$env:PYTHONIOENCODING = "utf-8"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.2"

# クラス概要（メソッド・フィールド一覧）
python .opencode\skills\mc-source-lookup\scripts\mc-lookup.py net.minecraft.world.entity.player.Inventory

# 特定のキーワードで絞り込み
python .opencode\skills\mc-source-lookup\scripts\mc-lookup.py net.minecraft.world.Container addItem

# --methods: すべてのメソッドを表示（デフォルトは public/protected のみ）
python .opencode\skills\mc-source-lookup\scripts\mc-lookup.py net.minecraft.world.entity.player.Inventory --methods
```

### 2. 手動で javap を使う

deobf jar の場所（Loom 1.17.19 + MC 1.21.1 環境）:
`~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-common-deobf/26.2/minecraft-common-deobf-26.2.jar`

```pwsh
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.2"
$jar = "$env:USERPROFILE\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-common-deobf\26.2\minecraft-common-deobf-26.2.jar"

# クラス全体
& "C:\Program Files\Java\jdk-21.0.2\bin\javap" -cp $jar net.minecraft.world.entity.player.Inventory

# 特定メソッドを grep
& "C:\Program Files\Java\jdk-21.0.2\bin\javap" -cp $jar net.minecraft.world.Container | Select-String "add"
```

### 3. クラス名の調べ方

Mojang マッピングのクラスは `net.minecraft.*` パッケージ以下。よく使うパス:

| パッケージ | 例 |
|:---|:---|
| `net.minecraft.world.entity` | `Player`, `LivingEntity`, `PathfinderMob` |
| `net.minecraft.world.entity.player` | `Inventory`, `Player` |
| `net.minecraft.world.item` | `ItemStack`, `Items` |
| `net.minecraft.world.level` | `Level`, `LightLayer` |
| `net.minecraft.world.effect` | `MobEffects`, `MobEffectInstance` |
| `net.minecraft.world.Container` | `Container`, `SimpleContainer` |
| `net.minecraft.world.ContainerHelper` | `ContainerHelper` |
| `net.minecraft.world.inventory` | `AbstractContainerMenu`, `MenuType` |
| `net.minecraft.world.level.block` | `Blocks`, `BlockState` |
| `net.minecraft.world.level.block.state` | `BlockState`, `BlockBehaviour` |
| `net.minecraft.core` | `BlockPos`, `Registry` |
| `net.minecraft.nbt` | `CompoundTag` |
| `net.minecraft.network` | `ChatComponent`, `packet` |
| `net.minecraft.server.level` | `ServerLevel`, `ServerPlayer` |

ネストクラスは `$` で区切る（例: `net.minecraft.world.entity.raid.Raid$RaidStatus`）。

### 4. jarc（ツリー表示）でクラスを探す

パッケージ内の全クラスを確認したい場合:

```pwsh
& "C:\Program Files\Java\jdk-21.0.2\bin\jar" tf $jar | Select-String "world/entity/player/"
```

## 注意点

- クライアント限定クラス（`net.minecraft.client.*`）は `minecraft-clientonly-deobf` の jar にある。スクリプトは common のみ対応。クライアントクラスが必要な場合は `javap -cp` の jar パスを `minecraft-clientonly-deobf\26.2\minecraft-clientonly-deobf-26.2.jar` に変更すること。
- クラス名が間違っていると `javap` がエラーを返す。完全修飾名（FQCN）で指定すること。