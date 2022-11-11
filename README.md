[![Scala CI](https://github.com/papauschek/crossword-puzzle-maker/actions/workflows/scala.yml/badge.svg)](https://github.com/papauschek/crossword-puzzle-maker/actions/workflows/scala.yml)

# Chris' Crossword Puzzle Generator

A crossword puzzle generator that creates dense puzzles.

**Try it out here:**
**https://papauschek.github.io/crossword-puzzle-maker/**

## Features

* Supports any crossword puzzle size.
* Define your own words for the puzzle, and then in addition you can add words from a dictionary to complete the puzzle.

## Technical details

The crossword puzzle algorithm takes your list of words as input,
and then randomly generates many possible puzzles using these words.
It then selects the puzzle with the most words (most density).

Web Workers are used to distribute the work of creating puzzles across multiple CPUs. 

## Third Party Libraries

Crossword dictionaries from the following library are used: https://github.com/fiee/croisee/tree/master/wordlists
(License: see `./docs/data/croisee`)