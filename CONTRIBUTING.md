# Simple Team Git Workflow

This project uses one shared GitHub repository.

- everyone works in the same repository
- everyone creates their own branch
- nobody pushes code directly to `main`
- the team leader reviews Pull Requests and merges them

## 1. First-time setup

Clone the repository:

```powershell
git clone https://github.com/yekunsong/Java_Code.git
cd Java_Code
```

## 2. Start a new task

Always update your code first:

```powershell
git pull origin main
```

Create your own branch:

```powershell
git checkout -b yourname-feature
```

Examples:

- `song-level1`
- `ali-menu`
- `mina-settings`

## 3. Finish your work

After your code is ready:

```powershell
git add .
git commit -m "Finish your task"
git push origin yourname-feature
```

Then go to GitHub and create a Pull Request to `main`.

## 4. If there is need for changes

Do not create a new branch.

Keep editing the same branch, then run:

```powershell
git add .
git commit -m "Update after review"
git push origin yourname-feature
```

Your Pull Request will update automatically.

## 5. Team rules

- always run `git pull origin main` before starting work
- do not code directly on `main`
- one task should use one branch
- say in the group chat which file or module you plan to change
- discuss shared files before editing them

## 6. Shared files to be careful with

- `src/level/BaseLevel.java`
- `src/core/Main.java`
- `src/core/AppRouter.java`
- `src/config/GameConfig.java`
- `src/entity/GameObject.java`
- `src/entity/Player.java`
- `src/entity/Block.java`

## 7. Very short version

Remember only these steps:

```powershell
git pull origin main
git checkout -b yourname-feature
git add .
git commit -m "Finish your task"
git push origin yourname-feature
```

Then create a Pull Request on GitHub.
