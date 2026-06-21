# Minimal JavaFX Platformer Framework

## 1. This Framework

The current framework keeps only the minimum features required for a level-based platform game:

- one main menu scene
- one settings scene
- three playable levels
- one shared player object
- one shared terrain block object
- one goal area in each level
- one shared base level class for repeated gameplay flow

The framework does not currently include enemies, inventory, keys, doors, audio systems, save systems, or advanced game-state managers.

## 2. Architectural Overview

The project is divided into five functional areas:

### `core`

The `core` package contains application startup and scene routing.

- `Main.java`
  Initializes JavaFX, creates the shared configuration object, and starts the first scene.

- `AppRouter.java`
  Controls navigation between `Menu`, `Settings`, `Level1`, `Level2`, and `Level3`.
  It is the only class responsible for changing the top-level scene on the `Stage`.

### `config`

The `config` package contains shared configuration values.

- `GameConfig.java`
  Stores world size, player constants, colors, and keyboard mappings.

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

- `BaseLevel.java`
  Shared parent class for all playable levels. It contains:
  - scene creation
  - player creation
  - pause overlay creation
  - input handling
  - timer-based update loop
  - collision resolution
  - world-bound clamping
  - goal detection
  - routing requests back through `AppRouter`

- `level.level1.Level1`
- `level.level2.Level2`
- `level.level3.Level3`

Each concrete level file should now contain only:

- level title
- platform layout
- goal position
- previous/next level identifiers
- any truly unique level-specific behavior

### `ui`

The `ui` package contains non-gameplay scenes.

- `MenuView.java`
- `SettingsView.java`

These files should contain interface construction only, not gameplay logic.
They now receive `AppRouter` directly and call simple navigation methods on it.

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
    BaseLevel.java
    level1/Level1.java
    level2/Level2.java
    level3/Level3.java

  ui/
    MenuView.java
    SettingsView.java
```

## 4. Responsibility Boundaries

The following rules should be followed when modifying the project.

### Modify concrete `level` files when:

- the terrain layout of one level changes
- the goal position of one level changes
- one level needs a unique mechanic
- one level needs different title text or level-specific navigation behavior

### Modify `BaseLevel` when:

- all levels need the same pause menu change
- all levels need the same input handling change
- all levels need the same collision or update-loop change
- all levels need the same scene wrapper or overlay behavior

### Modify `entity` files when:

- all levels need a different player movement rule
- all levels need different shared block behavior
- a new reusable gameplay object is introduced

### Modify `config` when:

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

- `src/level/BaseLevel.java`
- `src/core/Main.java`
- `src/core/AppRouter.java`
- `src/config/GameConfig.java`
- `src/entity/GameObject.java`
- `src/entity/Player.java`
- `src/entity/Block.java`

## 6. Modification Rules

When deciding where code should go, apply the following rules:

- if a change affects one specific level only, keep it inside that level file
- if a change affects every level, move it into `BaseLevel`, `GameConfig`, `Player`, `Block`, or `AppRouter`, depending on the type of change
- do not reintroduce duplicated logic into `Level1`, `Level2`, and `Level3` if the same logic already belongs in `BaseLevel`

Avoid reintroducing the old architecture layers unless there is a clear project requirement.

## 7. Current Navigation Model

The routing model is intentionally simple:

- `Main` creates `GameConfig`
- `Main` creates `AppRouter`
- `AppRouter` opens `MenuView`, `SettingsView`, or a level scene
- `MenuView` and `SettingsView` receive `AppRouter` directly
- each level receives `AppRouter` through its constructor
- no callback chains are required for menu navigation

This keeps the code easier to understand for a student team.

## 8. Git Collaboration Workflow

### Recommended overall policy

The recommended process for this project is:

1. modify code locally
2. test locally
3. create a local commit
4. push to GitHub

If multiple people are working at the same time, prefer a feature branch instead of pushing directly to `main`.

### Initial setup for a group member

Clone the repository:

```powershell
git clone https://github.com/yekunsong/Java_Code.git
```

Enter the repository:

```powershell
cd Java_Code
```

If this game folder should live inside the repository, copy or move the project into the correct location before committing.

Check the current branch and working status:

```powershell
git status
git branch
```

### Daily workflow before coding

Before beginning new work, always update the local copy:

```powershell
git pull origin main
```

This reduces the risk of overwriting another contributor's work.

### Local development workflow

After completing one logical unit of work:

```powershell
git status
git add .
git commit -m "Refactor game framework structure"
```

Examples of acceptable commit messages:

- `Refactor game framework structure`
- `Add shared BaseLevel for level flow`
- `Simplify AppRouter and menu navigation`
- `Update framework guide for new architecture`

### Safer branch-based workflow

If the team wants a safer process, use feature branches:

```powershell
git checkout -b feature/game-framework-refactor
git add .
git commit -m "Refactor game framework structure"
git push origin feature/game-framework-refactor
```

This workflow is recommended when multiple contributors are working at the same time because it avoids direct conflict on `main`.

### What contributors should avoid

- do not modify shared files casually
- do not push unfinished work directly without notice
- do not combine unrelated changes into one commit
- do not skip `git pull` before starting new work
- do not overwrite another contributor's work without discussion

## 9. File Ownership Guidance for Git Work

The following ownership mapping is recommended for day-to-day work:

- Level 1 work should mainly affect `src/level/level1/Level1.java`
- Level 2 work should mainly affect `src/level/level2/Level2.java`
- Level 3 work should mainly affect `src/level/level3/Level3.java`
- Shared level-flow work should mainly affect `src/level/BaseLevel.java`
- Menu work should mainly affect `src/ui/MenuView.java`
- Settings work should mainly affect `src/ui/SettingsView.java`

Any change to shared files should be discussed first because it can affect every contributor.

## 10. Future Expansion Suggestions

1. improve level visuals and layout
2. improve the settings screen
3. add additional reusable entities only when more than one level needs them
4. add advanced systems such as audio or save data only after the base structure is stable
