# Board Ultimatum WebApp

A [noir](https://github.com/noir-clojure/noir/blob/master/project.clj) webapp
acting as the front-end to the board-ultimatum board game recommendation engine.

## Setup

Just get the dependencies and run the web app.

```bash
lein deps # Not entierly necessary since lein run will do this itself.
lein run
```

You can also launch a REPL with `lein repl`.

### Modifying styles

You'll want to get the [our bootstrap](https://github.com/DRSNJM/bootstrap)
submodule first. You can either use the `--recursive` flag when you clone.

    git clone git@github.com:DRSNJM/board-ultimatum.git --recursive

Or if you have already cloned, from the base of the repository:

    git submodule init
    git submodule update

Once you've pulled in the submodule you can use the Makefile in the root of this
project. It has four helpful tasks:

1.  `make deps` -- Install bootstrap compiliation dependencies. Requires having
    npm installed already.
2.  `make bootstrap` -- The default task (so simply running `make` will work
    just as well) compiles all bootstrap resources completely.
3.  `make quickbs` -- Does a quick version of the bootstrap compilation that
    only compiles LESS resources and puts them uncompressed into
    `resources/public/css/bootstrap.min.css`. This is so you can use the
    `quickbs` task during development without having to change the CSS resourced
    referenced by the layout.  However, this also means that for production
    ready code you must run the `bootstrap` task before committing.
4.  `make watch` -- A helpful task that starts a watchr (`gem install watchr`)
    process that runs `make quickbs` whenever a LESS file is modified.

A normal development process once the bootstrap submodule set up would go
something like this:

1.  `make watch`
2.  Modify LESS files and view the results by simply reloading the page you are
    working on.
3.  Once you've got something you like commit the changes in the submodule.
4.  `^C` the `make watch` process and run `make` to generate the compressed
    resources.
5.  Commit everything in the main project (e.g. the updated submodule commit and
    the updated compiled resources).

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
