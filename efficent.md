# Efficient Copilot Prompting (Token-Saving Cheat Sheet)

Use these **general prompt lines** to get the best results in VS Code (Copilot / Auto) while **minimizing tokens**.

---

## 1️⃣ Core Prompt Structure (Use This First)

```
Context: selected code only.
Task: <what to do>
Constraints:
- minimal change
- no explanation
- output <format>
```

Example:

```
Context: selected code only.
Task: fix null pointer bug.
Constraints:
- minimal change
- diff only
- no explanation
```

---

## 2️⃣ Universal Token-Saving Lines

Use these **as-needed**:

* `No explanation.`
* `Code only.`
* `Answer in ≤10 lines.`
* `Do not rewrite unrelated code.`
* `Assume existing imports.`
* `Preserve current style.`

---

## 3️⃣ Debugging Prompts (Efficient)

```
Find root cause.
Fix bug.
Minimal patch.
Diff only.
```

If needed:

```
Root cause in 1 sentence.
Then patch.
```

---

## 4️⃣ Refactoring Prompts (Auto → Strong Model)

```
Refactor selected code.
Preserve behavior.
No regression.
Minimal changes.
Diff only.
```

Multi-file (use carefully):

```
Refactor across referenced files only.
Do not scan entire workspace.
```

---

## 5️⃣ Performance / Optimization

```
Optimize hot path only.
No architectural changes.
No explanation.
```

---

## 6️⃣ Test Generation (Controlled)

```
Generate unit tests.
Cover edge cases only.
No boilerplate.
```

or

```
Add 3 tests:
- happy path
- null input
- boundary case
```

---

## 7️⃣ Design / Decision Questions (Short)

Avoid long explanations.

Instead of:

```
Explain why this is bad
```

Use:

```
Pros/cons: 3 bullets max.
Recommendation: 1 line.
```

---

## 8️⃣ Inline Autocomplete (Cheapest Tokens)

Type intent as a comment:

```cpp
// parse input, validate, return error code
```

Then let Copilot autocomplete.

---

## 9️⃣ Hard Limits (Use When Needed)

```
Stop after solution.
Do not explain reasoning.
Do not include commentary.
```

---

## 🔑 Golden Rules (Remember These)

* **Select code before prompting** (biggest saver)
* Ask for **diffs, not files**
* Avoid "scan project" requests
* Restart chat if context gets long

---

## 🧠 Mental Model

> Tell Copilot **what to change**, **where
