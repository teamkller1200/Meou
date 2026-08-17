# Minecraft Companion Meou — MVP Specification

## 1. Overview & Purpose

This project is a **Fabric mod** (MC 1.21.1) that adds a **cat-like companion "Meou" (ミュー)** to Minecraft. Meou follows the player, and automatically triggers a player-configured skill based on the situation to support the player.

No external AI (LLM) of any kind is used. All behavior is implemented with vanilla Minecraft Entity/AI systems.

### Concept

- Companion name: **Meou** (ミュー)
- Cat-like bipedal character (white/cream base)
- Treats the player as its "partner" and always follows from behind
- Automatically triggers the skill set by the player based on the situation
- Sends "Nya"-style (〜ニャ) lines to chat during actions

---

## 2. MVP Scope

### Premise Features (base behavior, always active)

1. **Player following & teleporting**
   - Follows behind the player at a distance of 2–3 blocks
   - Teleports when the player moves too far away to prevent falling behind
   - `FollowCompanionGoal` (implemented)

2. **Automatic owner assignment**
   - On spawn, the nearest player is automatically assigned as the owner
   - Owner UUID is stored in the NBT key `Owner`

3. **Despawn when unowned**
   - Despawns 5 seconds after spawning if no owner has been assigned

4. **Always-visible name plate**
   - The name (default "Meou", or a user-set name) is always shown above the head

### Included Features

1. **Item holding & storage**
   - 1 hand slot + 27 storage slots = 28 slots total
   - Shift+right-click (owner) opens the tabbed GUI
   - The held item contributes damage to the attack skill

2. **Skill system (one skill at a time, auto-trigger)**
   - Set **one skill** via the GUI (default: Heal)
   - Meou monitors conditions and **autonomously triggers** the skill when met
   - Cooldown prevents spam; the selected skill and cooldown are persisted to NBT

   | Skill | Effect | Auto-trigger condition |
   |:---|:---|:---|
   | **Heal** | Removes poison + Regeneration II for 3s | Player HP ≤ 6, or poisoned, or on fire |
   | **Cheer** | Grants Speed for 10s | Enemy within 8 blocks |
   | **Collect** | Collects dropped items around | Dropped item within 8 blocks |
   | **Alert** | Marks hostile mobs with glowing | Enemy within 8 blocks |
   | **Light** | Places a torch in dark areas (consumes torch) | Dark area and owner has torches |
   | **Attack** | Attacks the nearest enemy for 5s | Enemy within 10 blocks |

   - **Attack support**: attacks with the weapon damage of the held item (sword, etc.); bare hands deal minimal damage. Combat takes priority over following.

3. **Rename (GUI)**
   - Enter a name in the skill tab input field and press the "Set" button to change it
   - Sent to the server via `RenamePayload` (C2S), which calls `setCustomName()`

4. **Dialogue system**
   - Random lines are sent to the owner's chat on skill activation and teleport
   - Spam prevention (min. 3s interval), prefixed with `[name]`

5. **Tabbed GUI (vanilla-style)**
   - "Items" and "Skill" tabs at the top (creative-tab style design)
   - Items tab: inventory operations
   - Skill tab: skill selection + description + rename (inventory hidden, compact panel)

### Out of Scope (Phase 2+)

- Multiple companion management
- Sit command
- Idle animations (head tilt, yawn)
- Sound effects
- Particle effects
- Spawn egg
- Custom model & texture (cat-shaped)
- Advanced automation (e.g. chest sorting)
- Shielding the player (taking damage for them)

---

## 3. System Architecture

```
Minecraft Client / Server (Fabric 1.21.1)
├── MeouEntity (PathfinderMob)
│   ├── FollowCompanionGoal (follow & teleport)
│   ├── MeleeAttackGoal (only active during attack support)
│   ├── SkillAutoTriggerGoal (auto-trigger, cooldown)
│   └── held item → attack damage (ATTACK_DAMAGE)
├── MeouScreenHandler (server-side container, data sync)
│   └── ModMenuTypes (menu registration)
├── MeouScreen (client GUI)
│   ├── Items tab / Skill tab
│   └── Rename UI (EditBox)
├── CustomPayload (C2S)
│   ├── SkillSelectPayload (skill selection)
│   └── RenamePayload (rename)
├── MeouDialogue (line sending)
├── MeouModel (custom model, placeholder)
└── MeouRenderer (texture rendering)
```

**No AI/LLM components.** No Python bridge server required.

---

## 4. Technology Stack

| Component | Technology | Notes |
| :--- | :--- | :--- |
| **Minecraft Mod Core** | Fabric (Java 21) | 1.21.1 |
| **Entity** | `PathfinderMob` extension | Vanilla AI reuse |
| **GUI** | `AbstractContainerScreen` + `MenuType` | Tab switching, dynamic window size |
| **Networking** | Fabric Networking (`CustomPacketPayload`) | C2S: skill selection, rename |
| **Skills** | `enum` (`MeouSkill`) + Goal | Cooldown stored in NBT |
| **Dialogue** | `MeouDialogue` | Translation keys + random selection |
| **Model** | `HierarchicalModel` + `ModelPart` | Placeholder |

---

## 5. Building & Running

### Prerequisites

- **JDK 21** (required). The default `java` on this machine is JDK 17, so set `JAVA_HOME` before running.
- Fabric Loom and Minecraft dependencies are downloaded automatically by Gradle on first run.
- Mojang official mappings are used (not Yarn). Methods are named by Mojang naming (e.g. `PathfinderMob`).

### Build

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.2"
.\gradlew.bat build
```

The resulting mod jar is written to `build/libs/`.

### Run (client / server)

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.2"
.\gradlew.bat runClient
# or
.\gradlew.bat runServer
```

The `run/` directory is a gitignored development workspace.

### Generate Minecraft sources (for IDE navigation)

```powershell
.\gradlew.bat genSources
```

> **Note (Windows):** use `.\gradlew.bat` — `./gradlew` does not work in PowerShell.

---

## 6. File Structure

```
src/
├── main/java/com/meou/
│   ├── Meou.java                  # Mod entry point, payload registration
│   ├── entity/
│   │   ├── ModEntityTypes.java      # Entity registration
│   │   ├── MeouEntity.java          # Meou entity
│   │   ├── ai/
│   │   │   └── FollowCompanionGoal.java  # Follow goal
│   │   └── skill/
│   │       ├── MeouSkill.java       # Skill enum (6 types)
│   │       ├── SkillAutoTriggerGoal.java  # Auto-trigger goal
│   │       └── MeouDialogue.java    # Line sending
│   ├── screen/
│   │   ├── ModMenuTypes.java        # Menu type registration
│   │   ├── MeouScreenHandler.java   # Server-side container
│   │   ├── SkillSelectPayload.java  # Skill selection packet
│   │   └── RenamePayload.java       # Rename packet
│   └── mixin/
│       └── ExampleMixin.java        # Template-derived (unused)
├── client/java/com/meou/client/
│   ├── MeouClient.java            # Client entry point
│   ├── screen/
│   │   └── MeouScreen.java          # Tabbed GUI (items / skill / rename)
│   ├── model/
│   │   └── MeouModel.java           # Custom model (placeholder)
│   ├── renderer/
│   │   └── MeouRenderer.java        # Texture rendering
│   └── mixin/
│       └── ExampleClientMixin.java  # Template-derived (unused)
└── main/resources/
    ├── assets/meou/
    │   ├── lang/
    │   │   ├── ja_jp.json           # Japanese translations (skill names, descriptions, lines)
    │   │   └── en_us.json           # English translations
    │   └── textures/
    │       ├── entity/meou.png
    │       └── gui/
    │           ├── meou_tab_selected.png
    │           ├── meou_tab_unselected.png
    │           └── container/meou.png
    ├── fabric.mod.json
    ├── meou.mixins.json
    └── meou.client.mixins.json
```
