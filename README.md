![Reconnect Mod: Reloaded Icon](https://github.com/59xa/reconnect-mod-reloaded/blob/master/src/main/resources/assets/reconnect-mod-reloaded/icon.png?raw=true)
# Reconnect Mod: Reloaded

[![Modrinth](https://img.shields.io/modrinth/dt/PjzgKfEE?label=&logo=Modrinth&labelColor=white&color=00AF5C&style=for-the-badge)](https://modrinth.com/mod/reconnect-mod-reloaded)
[![License](https://img.shields.io/github/license/59xa/reconnect-mod-reloaded?label=&logo=c&style=for-the-badge&color=A8B9CC&labelColor=455A64)](https://github.com/59xa/reconnect-mod-reloaded/blob/master/LICENSE)
[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/59xa/reconnect-mod-reloaded/build.yml?style=for-the-badge&label=&logo=Gradle&labelColor=388E3C)](https://github.com/59xa/reconnect-mod-reloaded/actions)

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.x-green?style=for-the-badge&labelColor=388E3C&color=8BC34A)](https://github.com/59xa/reconnect-mod-reloaded)

A simple mod that supersedes [HiWord9](https://github.com/HiWord9)'s Reconnect Button mod, updated to support 1.21+!
- This mod adds a reconnect button on your in-game menu screen while in a Multiplayer world.
- **NEW**: You can also reconnect to a server by typing **_/reconnect_** in chat.

![Reconnect Mod: Reloaded Example](https://cdn.modrinth.com/data/PjzgKfEE/images/54a7871bc46040b5214402a13cbbd4ab58b64aec.png)

## This mod currently supports Fabric and Quilt 1.21+
- There are no current plans of porting this mod to other modloaders like Forge/Neoforge, but will be considered if there is demand.
- Potential mod backporting to old versions may be possible in the near future if there is also demand for it.

## Will this mod come to CurseForge?
- There are currently no plans of bringing this mod to Curseforge. For now this mod is available for download on [releases page](https://github.com/59xa/reconnect-mod-reloaded/releases/tag/Releases) or on [Modrinth](https://modrinth.com/mod/reconnect-mod-reloaded)

## Is this mod usable in Realms?
- Due to the Realms API being internal, it is impossible to pinpoint the server address for any Realm world that you try to join.
- For the time being, using the reconnect button on Realms is not possible and has been disabled for use.

## Dependencies and Incompatibilities
- There are currently no incompatibilities seen with this mod at the moment.
- This mod no longer requires the Fabric API to function properly.
  -- This in return finally allows support for Quilt modloader clients!

## Known issues
- N/A

## Building the project
- JDK 21 (required)

To get the files from this repository:
```
git clone https://github.com/59xa/reconnect-mod-reloaded/
cd reconnect-mod-reloaded

./gradlew build
cd build/libs
```

## Credits
- HiWord9's implementation ([link](https://github.com/HiWord9/Reconnect-Button-HiWord9-fabric-1.19))
- Fabric ([link](https://fabricmc.net/))
- You (for giving this mod a try! 🤍)
