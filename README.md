# MoonGod

A Crystal PvP client for Minecraft **1.21.4**, built on the **Fabric** mod loader and written in **Kotlin**.

## Features

- **Modules** — organized into Combat, Movement, Player, Render, Misc and Client categories: Killaura, MoonAura, Surround, AutoTrap, AutoArmor, AutoWeb, Criticals, HoleFiller, Selftrap, Speed, ElytraFlight, Step, NoFall, ESP, HoleESP, NameTags, Fullbright, NoRender, HUD, ClickGui, and more.
- **Click GUI** — mouse-driven module and setting management with a custom font renderer.
- **Command system** — prefix-based chat commands for modules, binds, friends, config, and reload.
- **Event bus** — a lightweight event system (2D/3D render, packet, chat, movement, etc.) wired to mixins.
- **Config** — JSON-based settings persistence with save/reload support.

## Requirements

| Component | Version |
|-----------|---------|
| Java | 21+ |
| Minecraft | 1.21.4 |
| Fabric Loader | >= 0.19.5 |
| Fabric API | * |
| Fabric Language Kotlin | * |

## Build

```bash
./gradlew build
```

The built jar is placed in `build/libs/`.

## Install

1. Install the Fabric Loader for Minecraft 1.21.4.
2. Place Fabric API and Fabric Language Kotlin in the `mods` folder.
3. Copy the built MoonGod jar into the `mods` folder.
4. Launch the game with the Fabric profile.

## Commands

The default command prefix is `.` (changeable via the prefix command):

- `.bind <module> <key>` — bind a module to a key
- `.module` / `.modules` — list or toggle modules
- `.friend <add|del|list> <name>` — manage friends
- `.config <save|load|list>` — manage configs
- `.reload` — reload the client
- `.unload` — unload the client
- `.help` — show help

## Disclaimer

This project is provided for educational purposes only. Use it only on servers that permit such clients. Using it on servers that forbid it may violate server rules or the Minecraft EULA. The author is not responsible for any consequences.

## License

[MIT](LICENSE)
