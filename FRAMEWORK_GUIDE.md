# Minimal JavaFX Platformer Framework

## Goal

This version keeps only the minimum structure needed for a team platformer assignment:

- one `Menu`
- one `Settings`
- three levels
- one player
- simple blocks and one goal area per level

## Final Structure

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

## Team Ownership

- Member 1: `level.level1.Level1`
- Member 2: `level.level2.Level2`
- Member 3: `level.level3.Level3`
- Member 4: `ui.MenuView`
- Member 5: `ui.SettingsView`

Shared files that should be changed carefully:

- `core.Main`
- `core.AppRouter`
- `config.GameConfig`
- `entity.Player`
- `entity.Block`

## Rules

- `Main` is the only entry point
- `AppRouter` only switches scenes
- each level file contains its own layout, player spawn, goal, buttons, and update loop
- `Player` already includes the old movement logic from `Character`
- no enemy, key, door, pause, audio, runtime, factory, or builder layer remains

## Practical Rule

If a change belongs to one level only, keep it in that level file.
If a change affects all levels, put it in `GameConfig`, `Player`, or `AppRouter`.

## Git Workflow

### Recommended rule

For this project, the safer workflow is:

1. modify code locally
2. test locally
3. `commit` locally
4. wait for the team leader or repository owner to review and agree
5. `push` to GitHub

This is better than pushing immediately because:

- it keeps each member's work traceable
- it is easier to review one feature at a time
- if something breaks, the team can identify which commit changed it

### Recommended team process

Each member should follow this process:

1. clone the repository
2. open the project locally
3. implement only their own assigned part
4. run and check the game locally
5. commit with a clear message
6. wait for approval from the team leader
7. push to GitHub

### First-time setup

Clone the repository:

```powershell
git clone https://github.com/yekunsong/Java_Code.git
```

Enter the project folder:

```powershell
cd Java_Code
```

Check current status:

```powershell
git status
```

### Daily update before coding

Before starting new work, each member should get the newest code:

```powershell
git pull origin main
```

This avoids overwriting other teammates' changes.

### Commit after finishing one feature

Check changed files:

```powershell
git status
```

Stage the files:

```powershell
git add .
```

Create a commit:

```powershell
git commit -m "Refactor level 1 layout"
```

Commit message examples:

- `Add level 2 platform layout`
- `Update settings screen`
- `Refactor player movement`
- `Fix level 3 goal position`

### Push only after approval

After the team leader agrees:

```powershell
git push origin main
```

If your team later wants a safer workflow, use branches instead of pushing directly to `main`.

### Suggested branch workflow for teammates

Example:

```powershell
git checkout -b feature/level1-layout
```

Then work normally:

```powershell
git add .
git commit -m "Update level 1 layout"
git push origin feature/level1-layout
```

This is safer because each member can work independently before merging.

### What each teammate should avoid

- Do not edit shared files unless necessary
- Do not push unfinished code directly without telling the team
- Do not mix many unrelated changes into one commit
- Do not overwrite others' work by skipping `git pull`

### Recommended ownership with Git

- Level 1 owner mainly changes `src/level/level1/Level1.java`
- Level 2 owner mainly changes `src/level/level2/Level2.java`
- Level 3 owner mainly changes `src/level/level3/Level3.java`
- Menu owner mainly changes `src/ui/MenuView.java`
- Settings owner mainly changes `src/ui/SettingsView.java`
- Shared structure changes should be discussed before commit
