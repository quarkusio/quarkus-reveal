# Quarkus Reveal

**Quarkus Reveal** is a command-line tool for creating and presenting slides using [Reveal.js](https://revealjs.com), powered by JBang and designed for developer talks.

It includes a custom **Quarkus theme**, making it ideal for presentations about or at Quarkus-related events.

You write your slides in plain Markdown and present them instantly with live preview in your browser.

---

## 🚀 Installation

To install `quarkus-reveal` via [JBang](https://jbang.dev):

```bash
jbang app install --fresh --force quarkus-reveal@ia3andy/quarkus-reveal
```

---

## 📽 Usage

To start presenting your slides using the **Quarkus theme**:

```bash
quarkus-reveal talk.md -t quarkus
```

This opens `talk.md` in your browser with the Quarkus-styled reveal.js theme.

If no file is provided, it will look for `deck.md` in the current directory, or fall back to a demo deck:

```bash
quarkus-reveal -t quarkus
```

---

## Themes

Provided themes are `quarkus`, `light` and `default`, they can be set using `-t` or via frontmatter data in the deck file:
```markdown
---
theme: light
---
# Slide A
```

---

### Custom themes

It is also possible to specify a custom theme using `-t` or via frontmatter data in the deck file.
In that case, `quarkus-reveal` attempts to obtain all theme assets from a directory named as the theme.
The directory can be located in the directory where the current deck file lives, or in the parent directory.
For example, `quarkus-reveal talk.md -t mytheme` instructs `quarkus-reveal` to load the `style.css` from the path `mytheme/style.css` relative to the current directory.


## ✨ Markdown Features


### Horizontal slides

Use `---` to create horizontal slides (press space to navigate):

```markdown
# Slide A

---

# Slide B
```

### Vertical slides

I use them to sepate the same topic in multiple slides.

Use `--` to create vertical slides (press space to navigate):

```markdown
# Slide A 1

--

# Slide A 2
```

### Step-by-step content

Use `[~]` at the beginning of a line to make items appear one by one:

```markdown
# Features

[~] Fast startup  
[~] Live reload  
[~] Dev Services  
```

---

## 🖨 Exporting to PDF

After launching your deck in the browser, go to:

```
http://localhost:7979/?print-pdf
```

Then use your browser’s **Print to PDF** feature to export your slides.

---

## 🎨 Deck Assets

Add any images, videos, or styles you want to use in your slides inside a folder named:

```
deck-assets/
```

Everything inside this folder will be accessible from your deck.

---

## 🧪 Example

Create a simple Markdown file called `talk.md`:

```markdown
# Hello Quarkus

---

## Let's build modern Java apps

- Supersonic
- Subatomic
- Developer Joy
```

Then run:

```bash
quarkus-reveal talk.md -t quarkus
```


Here are a few example decks I use for my own demos:
https://github.com/ia3andy/demo-decks/

---

## 💡 Tip

See all available options:

```bash
quarkus-reveal --help
```

---

Made with ❤️ by [@ia3andy](https://github.com/ia3andy)
