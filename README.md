# 🎮 KuromiCore Engine

**Easy Game & Web Development for Beginners**

KuromiCore is a beginner-friendly programming environment that lets you create games and websites using the simple KuromiScript language. Think of it as "Scratch meets Java" - easy to learn but powerful enough to create real applications!

## ✨ Features

- 🎨 **Visual GUI Editor** - Write code with syntax highlighting and live testing
- 🎮 **Desktop Games** - Create Java-based games that run on any computer
- 🌐 **Web Export** - Compile your games to HTML5 websites
- 📦 **Standalone JARs** - Build executable files you can share with anyone
- 🚀 **Localhost Engine** - Everything runs on your computer, no internet needed
- 📚 **Beginner Friendly** - Simple syntax designed for learning

## 🚀 Quick Start

### Installation

1. **Download KuromiCore** (or clone this repository)
2. **Ensure Java JDK 11+ is installed**
    - Download from: https://adoptium.net/
3. **Build the engine:**

   **Windows:**
   ```bash
   build.bat
   ```

   **Linux/Mac:**
   ```bash
   chmod +x build.sh
   ./build.sh
   ```

### Running KuromiCore

**Launch GUI Editor:**
```bash
java -jar KuromiCore.jar
```
Double-click `KuromiCore.jar` on Windows!

**Run a script from command line:**
```bash
java -jar KuromiCore.jar mygame.kuromi
```

**Compile to HTML:**
```bash
java -jar KuromiCore.jar --web mygame.kuromi
```

**Build standalone JAR:**
```bash
java -jar KuromiCore.jar --jar -o mygame mygame.kuromi
```

## 📖 KuromiScript Language Guide

### Hello World

```kuromi
game 800 600 {
    show "Hello, World!" 400 300 center
    draw circle 400 350 50 "pink"
}
```

### Variables

```kuromi
game 800 600 {
    let x = 100
    let y = 200
    let message = "Hello!"
    
    draw circle x y 30 "blue"
    show message 400 100 center
}
```

### Drawing Shapes

```kuromi
game 800 600 {
    // Rectangle: x, y, width, height, color
    draw rect 100 100 200 150 "red"
    
    // Circle: x, y, radius, color
    draw circle 400 300 75 "blue"
    
    // Line: x1, y1, x2, y2, color
    draw line 50 50 750 550 "green"
}
```

### Control Flow

```kuromi
game 800 600 {
    let score = 85
    
    if (score >= 90) {
        show "Grade: A" 400 300 center
    } else {
        show "Keep trying!" 400 300 center
    }
    
    // While loop
    let i = 0
    while (i < 5) {
        draw circle 100 + (i * 50) 300 20 "yellow"
        i = i + 1
    }
}
```

### Arrays and Loops

```kuromi
game 800 600 {
    let colors = ["red", "blue", "green", "yellow"]
    let x = 100
    
    for color in colors {
        draw circle x 300 30 color
        x = x + 100
    }
}
```

### Functions

```kuromi
game 800 600 {
    fn drawStar(x, y, size) {
        draw circle x y size "yellow"
        return x + size
    }
    
    let pos = drawStar(100, 100, 25)
    drawStar(pos + 50, 100, 30)
}
```

### Loading Images

```kuromi
game 800 600 {
    load player: "player.png"
    load background: "bg.png"
    
    draw image background 0 0
    draw image player 400 300
}
```

Place your images in an `assets/` folder next to your script.

### Text Alignment

```kuromi
game 800 600 {
    show "Left aligned" 100 100 left
    show "Centered" 400 200 center
    show "Right aligned" 700 300 right
}
```

### Available Colors

`red`, `green`, `blue`, `yellow`, `cyan`, `magenta`, `white`, `black`, `gray`

### Operators

**Math:** `+`, `-`, `*`, `/`, `%`

**Comparison:** `==`, `!=`, `<`, `>`, `<=`, `>=`

**Logic:** `and`, `or`, `not`

## 🎯 Complete Examples

### Bouncing Ball Animation

```kuromi
game 800 600 {
    let positions = [100, 150, 200, 250, 300, 250, 200, 150]
    
    for yPos in positions {
        draw circle 400 yPos 30 "blue"
    }
    
    show "Bouncing Ball!" 400 500 center
}
```

### Simple Game Score

```kuromi
game 800 600 {
    fn displayScore(score) {
        if (score > 100) {
            show "🏆 High Score!" 400 100 center
            draw rect 300 200 200 100 "yellow"
        } else {
            show "Keep Going!" 400 100 center
            draw rect 300 200 200 100 "blue"
        }
    }
    
    let playerScore = 150
    displayScore(playerScore)
}
```

### Rainbow Circles

```kuromi
game 800 600 {
    let colors = ["red", "yellow", "green", "cyan", "blue", "magenta"]
    let x = 100
    
    for color in colors {
        draw circle x 300 40 color
        x = x + 120
    }
    
    show "Rainbow Time! 🌈" 400 450 center
}
```

## 🛠️ Command-Line Reference

```bash
# Launch GUI (default)
java -jar KuromiCore.jar

# Run script directly
java -jar KuromiCore.jar script.kuromi
java -jar KuromiCore.jar --run script.kuromi

# Compile to HTML website
java -jar KuromiCore.jar --web script.kuromi
java -jar KuromiCore.jar -w script.kuromi

# Build standalone JAR
java -jar KuromiCore.jar --jar script.kuromi
java -jar KuromiCore.jar -j script.kuromi

# Specify output name
java -jar KuromiCore.jar --jar -o mygame script.kuromi
java -jar KuromiCore.jar --web -o mywebsite script.kuromi

# Show help
java -jar KuromiCore.jar --help

# Show version
java -jar KuromiCore.jar --version
```

## 📁 Project Structure

```
KuromiCore/
├── src/
│   ├── Main.java              # Entry point
│   ├── lexer/                 # Tokenizer
│   │   ├── Lexer.java
│   │   ├── Token.java
│   │   └── TokenType.java
│   ├── parser/                # AST Builder
│   │   ├── Parser.java
│   │   ├── ASTNode.java
│   │   └── AST.java
│   ├── interpreter/           # Runtime Executor
│   │   ├── Interpreter.java
│   │   └── Environment.java
│   ├── runtime/               # Compilers & Builders
│   │   ├── Value.java
│   │   ├── Compiler.java
│   │   ├── JarBuilder.java
│   │   └── StandaloneRunner.java
│   └── gui/                   # Visual Editor
│       └── KuromiCoreGUI.java
├── examples/                  # Sample scripts
├── assets/                    # Game resources
├── build.bat                  # Windows build
├── build.sh                   # Linux/Mac build
└── README.md                  # This file
```

## 🎨 Using the GUI Editor

1. **Launch:** Double-click `KuromiCore.jar`
2. **Write Code:** Type in the code editor
3. **Test:** Click "▶ Run (F5)" to test your game
4. **Save:** File → Save or Ctrl+S
5. **Export:**
    - Click "🌐 Web" for HTML website
    - Click "📦 JAR" for standalone executable

### Keyboard Shortcuts

- `Ctrl+N` - New file
- `Ctrl+O` - Open file
- `Ctrl+S` - Save file
- `F5` - Run script

## 🌐 Web Export Details

When you compile to HTML, KuromiCore generates a complete website:

```html
<!-- Generated file: game.html -->
<!DOCTYPE html>
<html>
  <head>
    <title>KuromiScript Game</title>
    <style>/* Beautiful styling */</style>
  </head>
  <body>
    <canvas id="gameCanvas" width="800" height="600"></canvas>
    <script>
      // Your KuromiScript converted to JavaScript
    </script>
  </body>
</html>
```

Open the HTML file in any browser - no server needed! For advanced features or image loading, you can run a local server:

```bash
# Python 3
python -m http.server

# Python 2
python -m SimpleHTTPServer

# Node.js
npx http-server
```

Then open `http://localhost:8000/game.html`

## 📦 JAR Export Details

Standalone JAR files include:
- Complete KuromiCore runtime
- Your script embedded inside
- All necessary classes
- Can run on any computer with Java

Share your JAR files with friends - they just need Java installed!

## 🔧 Advanced Usage

### Custom Colors (Coming Soon)

```kuromi
// Future feature
draw rect 100 100 200 100 "#FF69B4"
```

### Mouse Input (Coming Soon)

```kuromi
// Future feature
on click(x, y) {
    draw circle x y 10 "red"
}
```

### Animation Loop (Coming Soon)

```kuromi
// Future feature
fn update() {
    // Called every frame
}
```

## 🐛 Troubleshooting

### "Java not found"
- Install Java JDK 11 or higher from https://adoptium.net/
- Make sure Java is in your system PATH

### "Compilation failed"
- Check that all source files are present
- Make sure you're running from the project root directory
- Verify Java version: `java -version`

### "Game window doesn't appear"
- Check console for error messages
- Make sure your script syntax is correct
- Try running the examples first

### "Images don't load"
- Create an `assets/` folder next to your script
- Place images in the assets folder
- Use correct filenames in `load` statements

## 🤝 Contributing

KuromiCore is designed to be beginner-friendly! Ideas for contributions:

- Add more example scripts
- Improve error messages
- Add new built-in functions
- Create tutorials
- Report bugs

## 📄 License

Free to use for learning and personal projects!

## 🎓 Educational Use

KuromiCore is perfect for:
- Learning programming basics
- Teaching game development
- Creating portfolio projects
- Prototyping game ideas
- Understanding compilers and interpreters

## 📚 Learning Path

1. **Start with examples** - Modify the included examples
2. **Learn the basics** - Variables, loops, conditions
3. **Draw shapes** - Create visual patterns
4. **Build a game** - Combine everything you learned
5. **Share it** - Export to HTML or JAR and show friends!

## 🌟 What You Can Build

- Simple arcade games
- Interactive art
- Educational simulations
- Web animations
- Portfolio projects
- Learning tools

## 💡 Tips for Beginners

1. **Start small** - Don't try to build a complex game right away
2. **Experiment** - Change numbers and see what happens
3. **Use the GUI** - The editor helps catch mistakes
4. **Read errors** - Error messages tell you what's wrong
5. **Try examples** - Learn by modifying working code

## 🚀 Next Steps

Once you're comfortable with KuromiScript:

1. Learn Java to understand how KuromiCore works
2. Learn JavaScript to enhance your web exports
3. Study game development patterns
4. Create your own programming language!

## 📞 Support

- Check examples in the `examples/` folder
- Read error messages carefully
- Test small pieces of code first
- Use the GUI's documentation (Help → Documentation)

## 🎉 Have Fun!

KuromiCore is all about making programming fun and accessible. Don't be afraid to experiment and create!

Happy coding! 🎮✨

---

Made with ❤️ for aspiring developers everywhere