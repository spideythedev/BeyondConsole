# BeyondConsole

BeyondConsole is a work-in-progress project exploring a better way to use a PlayStation console from devices people already own.

The first goal is simple: make Remote Play easier to set up and nicer to use on Android and desktop. A PS5 or PS4 still does the actual game processing. BeyondConsole is a client-side project; it does not turn console games into native Android or PC games.

## What we're working towards

- Connect to a console the user owns or is allowed to access.
- Stream video and audio to Android and desktop devices.
- Send controller input back to the console.
- Make touch controls practical on phones and tablets.
- Show useful connection, latency and stream information.
- Handle changing network conditions without making setup complicated.

## Status

The repository is at its starting point. There is no working app or streaming implementation yet. The design and technical choices are being worked out before the first client is built.

## Technical direction

We're evaluating existing Remote Play work before writing protocol code ourselves. [chiaki-ng](https://github.com/chiaki-ng/chiaki-ng) is an important project to study because it already supports PlayStation Remote Play on Android and desktop platforms.

Any code we reuse or build on will keep its original license and notices. In particular, chiaki-ng is licensed under AGPL-3.0, so its terms need to be followed if we integrate or modify it.

## Scope

BeyondConsole is not affiliated with or endorsed by Sony Interactive Entertainment. PlayStation, PS4 and PS5 are trademarks of their respective owners.

The project will not distribute game files, bypass console security, or provide access to consoles without the owner's authorization.

## Development

There is no build yet. Build instructions will be added when the first application target and its dependencies are in place.

For the current plan, see [the architecture notes](docs/architecture.md).
