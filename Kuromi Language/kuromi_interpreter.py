import re, time

def run_kuromi_code(code, print_func=print):
    variables = {}
    lines = code.splitlines()
    i = 0
    total_lines = len(lines)

    while i < total_lines:
        line = lines[i].strip()
        if not line or line.startswith("#"):
            i += 1
            continue

        # 🎮 Game start
        if line.startswith("Kuromi <Game Start>"):
            print_func("✨ Kuromi Game Started ✨")

        # 🖨️ Print
        elif line.startswith("Print "):
            match = re.match(r'Print "(.*)"', line)
            if match:
                print_func(match.group(1))
            else:
                expr = line.replace("Print ", "")
                try:
                    print_func(eval(expr, {}, variables))
                except Exception as e:
                    print_func(f"[Error evaluating expression: {e}]")

        # 💾 Variables
        elif line.startswith("Let "):
            match = re.match(r"Let (\w+) = (.*)", line)
            if match:
                var_name, value = match.groups()
                try:
                    variables[var_name] = eval(value, {}, variables)
                except Exception:
                    variables[var_name] = value.strip('"')
            else:
                print_func(f"[Syntax Error] Invalid Let statement: {line}")

        # 🔁 Repeat loop
        elif line.startswith("Repeat "):
            match = re.match(r"Repeat (\d+) Times:", line)
            if match:
                count = int(match.group(1))
                block_lines = []
                i += 1
                # capture indented block or next lines until blank
                while i < total_lines and lines[i].startswith("    "):
                    block_lines.append(lines[i][4:])
                    i += 1
                for _ in range(count):
                    run_kuromi_code("\n".join(block_lines), print_func)
                continue
            else:
                print_func(f"[Syntax Error] Invalid Repeat syntax: {line}")

        # ⏳ Wait
        elif line.startswith("Wait "):
            match = re.match(r"Wait (\d+)", line)
            if match:
                time.sleep(int(match.group(1)))
            else:
                print_func(f"[Syntax Error] Invalid Wait syntax: {line}")

        # ❓ Unknown command
        else:
            print_func(f"[Unknown Command] {line}")

        i += 1
