# 🚀 KuromiCore Quick Start Guide

Get up and running in 5 minutes!

## Step 1: Install Java ☕

KuromiCore needs Java JDK 11 or higher.

**Check if you have Java:**
```bash
java -version
```

**Don't have Java? Download here:**
- Windows/Mac/Linux: https://adoptium.net/

## Step 2: Build KuromiCore 🔨

**Windows:**
```bash
build.bat
```

**Linux/Mac:**
```bash
chmod +x build.sh
./build.sh
```

Wait for "✅ COMPILATION SUCCESSFUL!"

## Step 3: Choose Your Adventure 🎮

### Option A: Visual Editor (Recommended for Beginners)

```bash
java -jar KuromiCore.jar
```

Or double-click `KuromiCore.jar`!

You'll see:
- **Code Editor** - Write your code here
- **Console** - See output and errors
- **Toolbar** - Run, compile, export buttons

### Option B: Command Line (For Pros)

```bash
java -jar KuromiCore.jar examples/hello.kuromi
```

## Step 4: Try Your First Program 🎨

In the GUI editor, type:

```kuromi
game 800 600 {
    show "Hello, KuromiCore!" 400 300 center
    draw circle 400 350 50 "pink"
}
```

**Click "▶ Run (F5)"** or press F5!

A window should appear with your message and circle. 🎉

## Step 5: Try the Examples 📚

```bash
# Hello World
java -jar KuromiCore.jar examples/hello.kuromi

# Moving animation
java -jar KuromiCore.jar examples/moving-ball.kuromi

# Rainbow patterns
java -jar KuromiCore.jar examples/rainbow-pattern.kuromi

# Simple game
java -jar KuromiCore.jar examples/simple-game.kuromi
```

## Step 6: Export Your Creation 📦

### Make a Website:
```bash
java -jar KuromiCore.jar --web mygame.kuromi
```

Opens `mygame.html` in any browser!

### Make a Standalone App:
```bash
java -jar KuromiCore.jar --jar mygame.kuromi
```

Creates `mygame.jar` - share with anyone who has Java!

## Common Commands Cheat Sheet 📝

```bash
# Launch GUI
java -jar KuromiCore.jar

# Run a script
java -jar KuromiCore.jar script.kuromi

# Make HTML website
java -jar KuromiCore.jar --web script.kuromi

# Make JAR app
java -jar KuromiCore.jar --jar script.kuromi

# Custom output name
java -jar KuromiCore.jar --jar -o mygame script.kuromi

# Get help
java -jar KuromiCore.jar --help
```

## Your First Game: Step by Step 🎯

Create a file called `myfirst.kuromi`:

```kuromi
// myfirst.kuromi
game 800 600 {
    // Step 1: Add a title
    show "My First Game!" 400 50 center
    
    // Step 2: Draw the ground
    draw rect 0 500 800 100 "green"
    
    // Step 3: Draw the sky
    draw rect 0 0 800 500 "cyan"
    
    // Step 4: Add a character
    draw circle 400 450 30 "red"
    
    // Step 5: Add details
    draw circle 390 440 5 "white"  // Left eye
    draw circle 410 440 5 "white"  // Right eye
    
    print "Game started!"
}
```

Run it:
```bash
java -jar KuromiCore.jar myfirst.kuromi
```

## Next Steps 🌟

1. **Modify examples** - Change colors, positions, text
2. **Read the full README.md** - Learn all features
3. **Check Help → Documentation** in the GUI
4. **Experiment!** - The best way to learn

## Troubleshooting 🔧

**"Java not found"**
- Install Java from https://adoptium.net/
- Restart your terminal/command prompt

**"Compilation failed"**
- Make sure you're in the KuromiCore directory
- Check that `src/` folder exists
- Try `build.bat` or `build.sh` again

**"Game window doesn't appear"**
- Check the console for error messages
- Make sure your syntax is correct
- Try the examples first to verify installation

**"Command not found"**
- Windows: Use `java -jar KuromiCore.jar`
- Make sure you built successfully first

## Need Help? 💬

1. Check the examples in `examples/` folder
2. Read error messages carefully
3. Start with simple programs
4. Test small changes one at a time

## Tips for Success 💡

✅ **Start small** - Don't build a complex game first
✅ **Save often** - Use Ctrl+S in the GUI
✅ **Test frequently** - Run your code often
✅ **Read errors** - They tell you what's wrong
✅ **Have fun!** - Experiment and be creative

## Example: Make It Interactive 🎮

```kuromi
game 800 600 {
    // Variables make things dynamic
    let score = 0
    let lives = 3
    
    // Display game info
    show "Score: " + score 100 50 left
    show "Lives: " + lives 700 50 right
    
    // Draw based on variables
    if (lives > 0) {
        draw circle 400 300 50 "green"
        show "You're alive!" 400 400 center
    } else {
        draw circle 400 300 50 "red"
        show "Game Over" 400 400 center
    }
}
```

## What's Next? 🚀

- Learn about **functions** for reusable code
- Use **arrays** to manage multiple objects
- Create **loops** for patterns and animations
- Master **conditionals** for game logic

## Welcome to KuromiCore! 🎉

You're now ready to create amazing games and websites!

Remember: Every expert was once a beginner. Keep coding, keep creating, keep having fun!

Happy coding! 🎮✨

---

**Quick Links:**
- Full Documentation: [README.md](README.md)
- Examples: `examples/` folder
- Help in GUI: Help → Documentation
- Build Script: `build.bat` or `build.sh`