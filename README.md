# Minecraft Companion Ailuu — MVP Specification

## 1. Overview & Purpose

This project is a Fabric mod that adds a **Felyne-style companion** to Minecraft. Inspired by the Felyne (Airou) from the *Monster Hunter* series, the companion follows the player, performs cute idle animations, and assists with simple skills.


### Concept

- Companion name: **Ailuu**
- Cat-like bipedal character (white/cream base)
- Follows the player ("Otomo") from behind
- Vocalizations: "Nya", "Nyatt" (cat-like sounds)
- Idle animations: occasional yawn, head tilt
- Automatically triggers a player-configured skill based on context

---

## 2. MVP Scope

### Premise Features (base behavior, always active)

1. **Player following & teleporting**
   - Follows behind the player at a distance of 2–3 blocks
   - Teleports when the player moves too far away to prevent falling behind
   - Reuses the existing `FollowCompanionGoal` (implemented)

### Included Features

1. **Item holding & equipment**
   - Ailuu can equip tools (sword, pickaxe, etc.) in a hand slot
   - Right-clicking with an item transfers it to the Ailuu
   - Equipped items are rendered on the model

2. **Skill system (one skill at a time, auto-trigger)**
   - Right-click → GUI to select **one skill**
   - Ailuu monitors conditions and **autonomously triggers** the selected skill
   - Cooldown prevents spam
   - Dedicated animation and effects during activation

   | Skill | Effect | Trigger condition |
   |:---|:---|:---|
   | **Heal** | Grants player Regeneration II for 3s | Player HP ≤ 6 and cooldown ready |
   | **Cheer** | Grants player Speed for 10s | Player takes damage or enemy nearby, and cooldown ready |
   | **Collect** | Picks up dropped items within 8 blocks | Items on ground and Ailuu idle |
   | **Alert** | Marks hostile mobs with glowing effect | Enemy mob within 8 blocks |
   | **Light** | Places torches in dark areas (light ≤ 7) | Dark area and player inventory has torches |

3. **Multiple companion management**
   - A player can have multiple Ailuu at once
   - Each Ailuu can be named individually (name tag compatible)
   - Each Ailuu can have a different skill assigned

### Out of Scope (Phase 2+)

- Complex automation (e.g. chest sorting)
- Combat participation (shielding player)
- Sit command
- Idle animations
- Sound effects
- Particle effects
- Spawn egg
- Custom model & texture

---

## 3. System Architecture

```
Minecraft Client / Server (Fabric 1.21.1)
├── AiluuEntity (PathfinderMob)
│   ├── FollowCompanionGoal (following)
│   ├── SkillAutoTriggerGoal (auto-trigger)
│   │   ├── Heal / Cheer / Collect / Alert / Light
│   │   └── Cooldown manager
│   └── Skill activation animation
├── SkillScreen (client GUI)
│   └── ScreenHandler (server-side container)
├── CustomPayload (skill selection network)
├── AiluuModel (custom model)
│   ├── Head, ears, body, tail, 4 legs
│   └── Animation support
├── AiluuRenderer (texture rendering)
└── ModSoundEvents (sound definitions)
```

**No AI/LLM components.** No Python bridge server required.

---

## 4. Technology Stack

| Component | Technology | Notes |
| :--- | :--- | :--- |
| **Minecraft Mod Core** | Fabric (Java 21) | 1.21.1 |
| **Entity** | `PathfinderMob` extension | Vanilla AI reuse |
| **Model** | `HierarchicalModel` + `ModelPart` | Built with CubeListBuilder |
| **Sound** | `SoundEvent` registry | `.json` sound definitions |
| **GUI** | `ScreenHandler` + `HandledScreen` | Fabric Networking (CustomPayload) |
| **Skills** | `enum` + `interface` | Cooldown stored in NBT |

---

## 5. Building & Running

### Prerequisites

- **JDK 21** (required). The default `java` on this machine is JDK 17, so set `JAVA_HOME` before running.
- Fabric Loom and Minecraft dependencies are downloaded automatically by Gradle on first run.

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

## 6. File Structure (Target)

```
src/
├── main/java/com/aibots/
│   ├── Aibots.java                  # Mod entry point
│   ├── entity/
│   │   ├── ModEntityTypes.java      # Entity registration
│   │   ├── AiluuEntity.java         # Ailuu entity
│   │   ├── skill/
│   │   │   ├── AiluuSkill.java      # Skill enum (5 types)
│   │   │   ├── SkillAutoTriggerGoal.java  # Auto-trigger goal
│   │   │   └── SkillRegistry.java   # Skill effect definitions
│   │   └── ai/
│   │       └── FollowCompanionGoal.java
│   ├── screen/
│   │   ├── AiluuScreenHandler.java  # Inventory GUI
│   │   ├── ModMenuTypes.java        # Menu type registration
│   │   └── SkillPayload.java        # Custom packet
│   └── item/
│       └── ModItems.java            # Spawn egg
├── client/java/com/aibots/client/
│   ├── AibotsClient.java            # Client entry point
│   ├── screen/
│   │   ├── AiluuScreen.java         # Inventory GUI
│   │   └── SkillScreen.java         # Skill selection GUI
│   ├── model/
│   │   └── AiluuModel.java          # Custom cat model
│   └── renderer/
│       └── AiluuRenderer.java       # Texture rendering
└── main/resources/
    ├── assets/aibots/
    │   ├── sounds.json               # Sound definitions
    │   └── textures/entity/
    │       └── ailuu.png
    └── fabric.mod.json
```