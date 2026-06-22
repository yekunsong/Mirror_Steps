# Simple Team Git Workflow

This project uses one shared GitHub repository.

- everyone works in their clone project
- everyone push in their clone project
- Add a "Pull request" in the "https://github.com/yekunsong/Java_Code"
- Song reviews Pull Requests and merges them

## 0. Fork the project

Fork the project from "https://github.com/yekunsong/Java_Code"

The link of the forked project named "https://github.com/YourAccountName/YourProjectName"

## 1. First-time setup

Clone the repository in the position you choosen position, take "D:\JavaProject" as example:

```powershell
cd D:\JavaProject
git clone https://github.com/YourAccountName/YourProjectName
cd Java_Code
```

## 2. Make change

Now you have all the files, import the project to eclipse

how to import to eclipse: File -> Import -> Projects from Folder or Archive -> Choose direction you clone the file -> Finish

then: right click the file -> Properities -> Java Build Path -> Modulepath -> Add Library -> JavaFX SDK

then you can run the project and add whatever functions you want

## 3. Finish your work and upload to github

After your code is ready, run the following to upload to your clone project:

```powershell
git status
git add .
git status
git commit -m "Comments about What did you did"
git push origin main
```

Then go to GitHub "https://github.com/yekunsong/Java_Codeand" create a Pull Request to `main`.

how to create a pull request: got to "https://github.com/yekunsong/Java_Code" -> click "Pull requests" -> select "New pull request" -> select "compare across forks" -> choose your own cloned repository -> create pull request -> add several words to explain what you changed -> finish