# Minimal JavaFX Platformer Framework

## 1. This Framework

The current framework keeps only the minimum features required for a level-based platform game:

- one main menu scene
- one settings scene
- three playable levels
- one shared player object
- one shared terrain block object
- one goal area in each level

The framework does not currently include enemies, inventory, keys, doors, audio systems, save systems, or advanced game-state managers.

## 2. Architectural Overview

The project is divided into five functional areas:

### `core`

The `core` package contains application startup and scene routing.

- `Main.java`
  Initializes JavaFX, creates the shared configuration object, and starts the first scene.

- `AppRouter.java`
  Controls navigation between `Menu`, `Settings`, `Level1`, `Level2`, and `Level3`.

### `config`

The `config` package contains shared configuration values.

- `GameConfig.java`
  Stores fixed window size, world size, player constants, colors, and keyboard mappings.

This package should contain only values intended to affect the entire project.

### `entity`

The `entity` package contains reusable gameplay objects.

- `GameObject.java`
  Parent class for simple drawable objects.

- `Player.java`
  Playable character with movement, gravity, jumping, and respawn logic.

- `Block.java`
  Static terrain object used as ground and platforms.

### `level`

The `level` package contains the playable stages.

- `level.level1.Level1`
- `level.level2.Level2`
- `level.level3.Level3`

Each level file is self-contained and includes:

- level layout
- player creation
- goal creation
- collision logic
- update loop
- pause overlay
- routing requests through `AppRouter`

### `ui`

The `ui` package contains non-gameplay scenes.

- `MenuView.java`
- `SettingsView.java`

These files should contain interface construction only, not gameplay logic.

## 3. Current Source Structure

```text
src/
  config/
    GameConfig.java

  core/
    Main.java
    AppRouter.java
    application.css

  entity/
    GameObject.java
    Player.java
    Block.java

  level/
    level1/Level1.java
    level2/Level2.java
    level3/Level3.java

  ui/
    MenuView.java
    SettingsView.java
```

## 4. Responsibility Boundaries

The following rules should be followed when modifying the project.

### Modify `level` files when:

- the terrain layout of one level changes
- the goal position of one level changes
- one level needs a unique mechanic
- one level needs different text or presentation

### Modify `entity` files when:

- all levels need a different player movement rule
- all levels need different shared block behavior
- a new reusable gameplay object is introduced

### Modify `config` when:

- the fixed window size changes
- the shared world size changes
- keyboard controls change for the entire game
- shared colors or global movement constants change

### Modify `core` when:

- a new top-level scene must be routed
- the startup procedure changes
- the stage/window policy changes

### Modify `ui` when:

- menu layout changes
- settings layout changes
- additional non-gameplay screens are introduced

## 5. Recommended Team Division

A five-person team can divide work as follows:

- Member 1: `src/level/level1/Level1.java`
- Member 2: `src/level/level2/Level2.java`
- Member 3: `src/level/level3/Level3.java`
- Member 4: `src/ui/MenuView.java`
- Member 5: `src/ui/SettingsView.java`

Shared files that should be modified carefully and preferably after discussion:

- `src/core/Main.java`
- `src/core/AppRouter.java`
- `src/config/GameConfig.java`
- `src/entity/GameObject.java`
- `src/entity/Player.java`
- `src/entity/Block.java`

## 6. Modification Rules

When deciding where code should go, apply the following rule:

- if a change affects one specific level only, keep it inside that level file
- if a change affects every level, move it into `GameConfig`, `Player`, `Block`, or `AppRouter`, depending on the type of change

Avoid reintroducing the old architecture layers unless there is a clear project requirement.

## 7. Git Collaboration Workflow

### Recommended overall policy

The recommended process for this project is:

1. modify code locally
2. test locally
3. create a local commit
4. wait for review or approval from the repository owner or group leader
5. push to GitHub only after approval

This workflow is preferred because it:

- preserves a clear history of individual changes
- makes review easier
- reduces the chance of directly pushing incorrect or incomplete work to the shared repository

### Initial setup for a group member

Clone the repository:

```powershell
git clone https://github.com/yekunsong/Java_Code.git
```

Enter the project directory:

```powershell
cd Java_Code
```

Check the current branch and working status:

```powershell
git status
```

### Daily workflow before coding

Before beginning new work, always update the local copy:

```powershell
git pull origin main
```

This is important because it reduces the risk of overwriting another contributor's work.

### Local development workflow

After completing one logical unit of work:

```powershell
git status
git add .
git commit -m "Update level 2 layout"
```

Examples of acceptable commit messages:

- `Update level 1 platform layout`
- `Refine settings screen text`
- `Adjust player movement constants`
- `Fix pause overlay routing`

### Push workflow after approval

Once the change has been reviewed or approved:

```powershell
git push origin main
```

### Safer branch-based workflow

If the team wants a safer process, use feature branches:

```powershell
git checkout -b feature/level1-update
git add .
git commit -m "Update level 1 jump layout"
git push origin feature/level1-update
```

This workflow is recommended when multiple contributors are working at the same time because it avoids direct conflict on `main`.

### What contributors should avoid

- do not modify shared files casually
- do not push unfinished work directly without notice
- do not combine unrelated changes into one commit
- do not skip `git pull` before starting new work
- do not overwrite another contributor's work without discussion

## 8. File Ownership Guidance for Git Work

The following ownership mapping is recommended for day-to-day work:

- Level 1 work should mainly affect `src/level/level1/Level1.java`
- Level 2 work should mainly affect `src/level/level2/Level2.java`
- Level 3 work should mainly affect `src/level/level3/Level3.java`
- Menu work should mainly affect `src/ui/MenuView.java`
- Settings work should mainly affect `src/ui/SettingsView.java`

Any change to shared files should be discussed first because it can affect every contributor.

## 9. Future Expansion Suggestions

1. improve level visuals and layout
2. improve the settings screen
3. add additional reusable entities only when more than one level needs them
4. add advanced systems such as audio or save data only after the base structure is stable
.etc