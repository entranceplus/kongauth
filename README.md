# kongauth

FIXME: description

## Requirements

* boot should be installed https://github.com/boot-clj/boot/#install
* postgresql should be installedd
* provided database should exists

## Usage
Create a profiles.edn file with values for keys {:username :password :db :http-port}

Run migrations:

    $ boot local-migrate

Run dev environment:

    $ boot dev

Run the project's tests (they'll fail until you edit them):

    $ boot test

Build an uberjar from the project:

    $ boot build

Run the uberjar:

    $ java -jar target/kongauth-0.1.0-SNAPSHOT-standalone.jar [args]

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
