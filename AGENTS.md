# AGENTS.md

This file provides guidance to Qoder (qoder.com) when working with code in this repository.

## Project Overview

Bingo is a Minecraft Bukkit/Spigot plugin for quest/task management (Bingo-style gameplay). It supports optional Pixelmon mod integration (both legacy 1.12.2 and native 1.16.5 versions). The plugin is developed by AiYo Studio, targets Java 8, and depends on AyCore and NBTAPI at runtime, with optional PlaceholderAPI support.

## Build Commands

```bash
# Full build (produces fat JAR with all subprojects merged)
./gradlew allJar

# Build individual subprojects
./gradlew :common:build
./gradlew :module:module-pixelmon-legacy:build
./gradlew :module:module-pixelmon-native:build

# Clean build
./gradlew clean allJar
```

The `allJar` task is the main build target. It merges all subproject JARs into a single `Bingo-<version>.jar` in `build/libs/`. The default `jar` task is disabled.

The `release.sh` script packages the built JAR with files from `release/exts/` into a zip for distribution.

## Project Structure (Gradle Multi-Project)

- **Root `build.gradle`**: Defines the project version (`ver` property), allprojects config (Java 8, repos, Lombok), and the `allJar` aggregation task. Uses Shadow plugin 4.0.2.
- **`common/`**: Core plugin module. Contains all plugin logic: main class, commands, listeners, cache framework, data sources, views, API, i18n, cron jobs. The `common` build uses Shadow to relocate Quartz scheduler dependencies (`org.quartz` -> `org.bingo.quartz`, etc.).
- **`module/module-pixelmon-legacy/`**: Optional Pixelmon support for Forge 1.12.2. Uses ForgeGradle. Depends on `common` and local JARs in `libs/`.
- **`module/module-pixelmon-native/`**: Optional Pixelmon support for Forge 1.16.5. Uses ForgeGradle. Depends on `common` and local JARs in `libs/`.

## Architecture

### Plugin Lifecycle (`Bingo.java`)
Main class extends `AyPlugin` (from AyCore). On enable: initializes cron scheduler, loads configs, creates data source, registers command/listeners, reflectively loads Pixelmon model services, registers PlaceholderAPI hook, starts scheduled tasks. On disable: saves all player caches.

### Cache Framework (`cacheframework/`)
`CacheManager` is the central static registry holding all in-memory caches:
- `PlayerCache` (ConcurrentHashMap, keyed by UUID) - player quest progress, loaded/unloaded on join/quit
- `QuestCache` - quest definitions loaded from `quests/` YAML files (supports nested directories)
- `ViewCache` - GUI view definitions from `view/` YAML files
- `GroupCache` - quest group definitions from `groups/` YAML files
- `NodeCache` - permission nodes from `node.yml`
- `JobCache` - cron job definitions from `jobs/` YAML files

### Data Sources (`dao/`)
`IDataSource` interface with `AbstractDataSourceImpl` factory (`of()` method). Three implementations:
- `YamlDataSourceImpl` - file-based YAML storage (default)
- `SQLiteDataSourceImpl` - SQLite database
- `MysqlDataSourceImpl` - MySQL database

Selection is driven by `data-option.type` in `config.yml`.

### View System (`view/`)
`IView` -> `AbstractView` -> implementations (`DefaultViewImpl`, `RandomViewImpl`). Views are GUI inventories using AyCore's `GuiModel`. `ViewRegistry` maps view type strings to implementation classes and supports external registration.

### Model Services (`service/`)
`IModelService` interface for optional mod integrations. Pixelmon services are loaded reflectively by class name - if the class is not on the classpath (mod not installed), `ClassNotFoundException` is silently caught.

### Cron Jobs (`cron/`)
Uses Quartz scheduler (relocated to `org.bingo.quartz`) for periodic job execution. Jobs are defined in `jobs/` YAML with cron expressions.

### API (`api/`)
`BingoApi` provides `submit()` for external plugins to report quest progress, and `getCommandsOfNodes()` for node-based command resolution. Custom Bukkit events: `BingoQuestCompleteEvent`, `BingoUnlockGroupEvent`, `DataSourceInitializeEvent`.

### Resource Token Replacement
The `@version@` token in YAML resources (e.g., `plugin.yml`) is replaced at build time with the `ver` property from the root `build.gradle`.

## Key Dependencies

- **AyCore**: Required plugin dependency, provides `AyPlugin` base class, `GuiModel`, and utilities
- **NBTAPI** (`de.tr7zw:item-nbt-api-plugin`): Required for NBT item manipulation
- **PlaceholderAPI**: Optional, for placeholder expansion
- **Quartz Scheduler**: Embedded (relocated) for cron-based job scheduling
- **Lombok**: Used throughout for `@Getter`, `@Setter`, etc.
- **ForgeGradle**: Used only by Pixelmon modules for Minecraft Forge integration

## Important Notes

- Java source/target compatibility is **1.8** - do not use Java 9+ APIs or language features.
- All source files use **UTF-8** encoding.
- Gradle JVM args are set to `-Xmx3G` for Minecraft decompilation in Pixelmon modules.
- The Pixelmon modules require local Forge/Pixelmon JARs in their respective `libs/` directories.
- Player cache uses `ConcurrentHashMap` for thread safety; bulk save operations use `HookState.onSave` flag to prevent concurrent saves.
