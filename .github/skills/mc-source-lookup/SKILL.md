---
name: mc-source-lookup
description: "Use when: クラスのメソッドシグネチャ（戻り値・引数）を確認したい、Mojang 名のメソッドが存在するか確認したい。Fabric MC 1.21.1 Mojang マッピング環境で、javap を用いた deobf jar からのクラス解析手順を提供する。例: 「Inventory.add の戻り値は？」「Container に addItem メソッドはある？」「LivingEntity のすべてのメソッド一覧」"
---

# Minecraft Source Lookup (Mojang Mappings)

このスキルは、Mojang 公式マッピングでビルドされた Minecraft 1.21.1 のクラス情報を `javap` で取得する手順を提供する。

## 前提・準備

### 必須

- **JDK 21** — PowerShell で `$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.2"` が設定されていること
- **`genSources` 済み** — 初回は `.\gradlew.bat genSources` を実行（deobf jar を キャッシュに生成）
- **Python 3.6+** — スクリプト実行用

### 確認方法

```pwsh
java -version          # Java 21 が表示されるか
python --version       # Python 3.6 以上が表示されるか
```

### 初期化（初回のみ）

```pwsh
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.2"
$env:PYTHONIOENCODING = "utf-8"
.\gradlew.bat genSources  # 5-10 分かかる（deobf jar を ~/.gradle/caches に生成）
```

### 1. 便利スクリプトを使う（推奨）

`scripts/mc-lookup.py` が deobf jar のパスを自動解決して `javap` を実行する。

#### セットアップ（初回のみ）

```pwsh
# PowerShell の プロファイルに以下を追加
# $PROFILE で表示されたパスを編集
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.2"
$env:PYTHONIOENCODING = "utf-8"
Set-Alias mc-lookup "python $pwd\.github\skills\mc-source-lookup\scripts\mc-lookup.py"
```

#### 使用例

```pwsh
# クラス概要（public/protected メソッド・フィールド一覧）
python .github\skills\mc-source-lookup\scripts\mc-lookup.py net.minecraft.world.entity.player.Inventory
# 短縮形（プロファイル設定後）
mc-lookup net.minecraft.world.entity.player.Inventory

# 特定のキーワードで絞り込み（例: add メソッドを探す）
python .github\skills\mc-source-lookup\scripts\mc-lookup.py net.minecraft.world.Container add
mc-lookup net.minecraft.world.Container add

# --methods: すべてのメソッド＋フィールドを表示（private も含む）
python .github\skills\mc-source-lookup\scripts\mc-lookup.py net.minecraft.world.entity.player.Inventory --methods

# クライアント限定クラスを参照
python .github\skills\mc-source-lookup\scripts\mc-lookup.py --client net.minecraft.client.gui.screens.MenuScreens
```

### 2. 手動で javap を使う（上級向け）

スクリプトを使わずに直接 `javap` を呼び出す場合:

```pwsh
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.2"
$jar = "$env:USERPROFILE\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-common-deobf\26.2\minecraft-common-deobf-26.2.jar"

# クラス全体
& "$env:JAVA_HOME\bin\javap" -cp $jar net.minecraft.world.entity.player.Inventory

# 特定メソッドを grep で絞り込み
& "$env:JAVA_HOME\bin\javap" -cp $jar net.minecraft.world.Container | Select-String "add"
```

### 3. クラス名の調べ方

Mojang マッピングのクラスは `net.minecraft.*` パッケージ以下。よく使うパス:

| パッケージ                              | 例                                        |
| :-------------------------------------- | :---------------------------------------- |
| `net.minecraft.world.entity`            | `Player`, `LivingEntity`, `PathfinderMob` |
| `net.minecraft.world.entity.player`     | `Inventory`, `Player`                     |
| `net.minecraft.world.item`              | `ItemStack`, `Items`                      |
| `net.minecraft.world.level`             | `Level`, `LightLayer`                     |
| `net.minecraft.world.effect`            | `MobEffects`, `MobEffectInstance`         |
| `net.minecraft.world.Container`         | `Container`, `SimpleContainer`            |
| `net.minecraft.world.ContainerHelper`   | `ContainerHelper`                         |
| `net.minecraft.world.inventory`         | `AbstractContainerMenu`, `MenuType`       |
| `net.minecraft.world.level.block`       | `Blocks`, `BlockState`                    |
| `net.minecraft.world.level.block.state` | `BlockState`, `BlockBehaviour`            |
| `net.minecraft.core`                    | `BlockPos`, `Registry`                    |
| `net.minecraft.nbt`                     | `CompoundTag`                             |
| `net.minecraft.network`                 | `ChatComponent`, `packet`                 |
| `net.minecraft.server.level`            | `ServerLevel`, `ServerPlayer`             |

ネストクラスは `$` で区切る（例: `net.minecraft.world.entity.raid.Raid$RaidStatus`）。

### 4. jar の内容を参照してクラス名を探す

パッケージ内の全クラスを確認する場合、`jar tf` コマンドで一覧表示:

```pwsh
$jar = "$env:USERPROFILE\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-common-deobf\26.2\minecraft-common-deobf-26.2.jar"

# world/entity/player/ 配下のクラスを一覧
& "C:\Program Files\Java\jdk-21.0.2\bin\jar" tf $jar | Select-String "world/entity/player/"

# 出力例:
# net/minecraft/world/entity/player/Inventory.class
# net/minecraft/world/entity/player/Player.class
# net/minecraft/world/entity/player/StackedContents.class
```

この方法で、完全なクラス名（`.class` を除いた部分）を確認してから `javap` で解析できます。

## トラブルシューティング

### スクリプトが deobf jar を見つからない

```
ERROR: common deobf jar not found. Run `gradlew.bat genSources` first.
```

**原因**: Minecraft ソースが逆コンパイルされていない  
**対処**:

```pwsh
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.2"
.\gradlew.bat genSources  # 5-10 分かかる
```

### `JAVA_HOME` が設定されていない

```
ERROR: javap not found at javap. Set JAVA_HOME.
```

**対処**:

```pwsh
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.2"
# 永続的に設定：システム環境変数に追加するか、PowerShell プロファイルに記載
```

### クラス名が見つからない

```
ERROR: javap failed for net.minecraft.world.Foobar
```

**原因**: クラス名が間違っているか存在しない  
**対処**:

- 完全修飾名（FQCN）で指定しているか確認
- `net.minecraft.server.level.ServerPlayer` は正しい
- `net.minecraft.entity.player.EntityPlayer` は誤り（Mojang マッピング では `Player` 等に変わっている）
- 不確かな場合は「3. クラス名の調べ方」の表を参照するか、`jar tf` でパッケージ一覧を確認

### クライアント限定クラスを参照したい

**原因**: `--client` フラグなしでクライアントクラスを指定した  
**対処**:

```pwsh
python .github\skills\mc-source-lookup\scripts\mc-lookup.py --client net.minecraft.client.gui.screens.MenuScreens
```

## FAQ

### Q: バージョンを変えたい（MC 1.21.1 以外）

**A**: `scripts/mc-lookup.py` の以下を編集:

```python
DEOBF_JARS = {
    "common": find_jar("minecraft-common-deobf-26.2.jar"),      # 26.2 は MC 1.21.1
    "client": find_jar("minecraft-clientonly-deobf-26.2.jar"),
}
```

バージョン対応表:
| Minecraft | Loom マッピング |
|:---|:---|
| 1.21.1 | 26.2 |
| 1.20.1 | 25.1 |
| 1.20 | 24.0 |

jar ファイルはビルド時に `~/.gradle/caches/fabric-loom/minecraftMaven/` に自動保存される。

### Q: メソッドの使用例を見たい

**A**: javap はシグネチャのみ出力。実装を見るには IDE の "Go to Definition" を使用するか、逆コンパイル結果を確認:

```pwsh
$jar = "$env:USERPROFILE\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-common-deobf\26.2\minecraft-common-deobf-26.2.jar"
# jar を IDE で開く、または cfr/procyon などのデコンパイラを使用
```

### Q: すべてのメソッドを見たい（private も含む）

**A**: `--methods` フラグを使用:

```pwsh
python .github\skills\mc-source-lookup\scripts\mc-lookup.py net.minecraft.world.entity.LivingEntity --methods
```

デフォルトは `public` と `protected` のみ表示。

### Q: 継承元のメソッドも見たい

**A**: javap はそのクラスのメソッドのみ出力。親クラスを別途確認:

```pwsh
# Entity の全メソッドを見る
python .github\skills\mc-source-lookup\scripts\mc-lookup.py net.minecraft.world.entity.Entity --methods

# LivingEntity の親クラス: Entity
# さらに遡る場合も同様に各クラスを指定
```

### Q: フィールド（変数）のみを見たい

**A**: 現在のスクリプトはメソッド主体。フィールドも表示されます:

```
public static final net.minecraft.world.item.Items APPLE;
public int age;
```

`--methods` フラグで全表示しても区別がつく。grep で絞り込み:

```pwsh
python .github\skills\mc-source-lookup\scripts\mc-lookup.py net.minecraft.world.entity.player.Inventory --methods | Select-String "public final"
```
