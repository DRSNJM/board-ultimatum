# Board Ultimatum WebApp

A [noir](https://github.com/noir-clojure/noir/blob/master/project.clj) webapp
acting as the front-end to the board-ultimatum board game recommendation engine.

## Usage

Just get the dependencies and run the web app.

```bash
lein deps
lein run
```

### Using a local version of board-ultimatum.engine

Let's suppose you have the following directory structure:

*   `~/repo/`
    *   `board-ultimatum/` (this repository)
    *   `board-ultimatum-engine/` a dependency of this webapp

To use your local version instead of the one in clojars simply use leiningen's
checkouts feature like so:

```bash
# Inside of ~/repo/board-ultimatum/
mkdir -p checkouts
ln -s ../../board-ultimatum-engine checkouts/
```

## License

Copyright (C) 2012 DRSNJM

Distributed under the Eclipse Public License, the same as Clojure.
