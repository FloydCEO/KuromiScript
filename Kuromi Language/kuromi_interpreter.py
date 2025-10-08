import re

def run_kuromi_code(code, print_func=print):
    variables = {}

    lines = code.splitlines()
    for line in lines:
        line = line.strip()
        if not line or line.startswith("#"):
            continue

        # Game start
        if line.startswith("Kuromi <Game Start>"):
            print_func("✨ Kuromi Game Started ✨")

        # Print
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

        # Let variable
        elif line.startswith("Let "):
            match = re.match(r"Let (\w+) = (.*)", line)
            if match:
                var_name, value = match.groups()
                try:
                    variables[var_name] = eval(value, {}, variables)
                except:
                    variables[var_name] = value.strip('"')
            else:
                print_func(f"[Syntax Error] Invalid Let statement: {line}")

        else:
            print_func(f"[Unknown Command] {line}")
