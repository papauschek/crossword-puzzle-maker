[![Scala CI](https://github.com/papauschek/crossword-puzzle-maker/actions/workflows/scala.yml/badge.svg)](https://github.com/papauschek/crossword-puzzle-maker/actions/workflows/scala.yml)

# Chris' Crossword Puzzle Maker

A crossword puzzle generator that creates dense puzzles, written in Scala.js.

## Try it out here:
**https://papauschek.github.io/crossword-puzzle-maker**

<img width="500" alt="image" src="https://user-images.githubusercontent.com/1398727/201483795-6776a7a8-7f2b-4639-8251-1839086355c0.png">

## Features

* Supports fixed crossword puzzle sizes, tries to use the available space most efficiently.
* Supports adding words from a dictionary to complete the puzzle
* Can be printed / exported as PDF (using the browser)

## Why?

Crossword puzzle generators often create puzzles that need a lot of space, and contain a lot of whitespace (gaps).
Sometimes you want a puzzle that fits a given size or space (e.g. for printing), and this is what this generator does.

## Technical details

The crossword puzzle algorithm takes your list of words as input,
and then randomly generates many possible puzzles using these words.
It then selects the puzzle with the most words (most density).

Web Workers are used to distribute the work of creating puzzles across multiple CPUs.

## How to build

You need to have `sbt` installed. Run `sbt fullOptJS` to build the app. This bundles the app as a JavaScript file in `docs/js/main.js`.

Then you need to open `docs/index.html` in your browser. However, there is a catch: this app needs Web Workers to run in the browser. Most browsers (e.g. Chrome) disallow running web workers on files loaded from the local filesystem.
So you need some a local web server to serve the docs folder, and then you're good to go. Some IDEs like IntelliJ have a small web server included, so there you can right-click `index.html` and select "Open in Browser". 

## Third Party Libraries

Crossword dictionaries from the following library are used: https://github.com/fiee/croisee/tree/master/wordlists
(License: see `./docs/data/croisee`)

## TODO

* The current algorithm also allows creating puzzles that wrap around the left and right edges of the puzzle. But this is currently not possible on the UI.

## License

[MIT License](LICENSE.md)
