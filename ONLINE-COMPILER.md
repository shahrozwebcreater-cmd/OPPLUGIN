# OPCratesWorks — Online Compiler Ready

This is a complete Maven source project for a Paper 1.21.1 server plugin.
It is designed so an online IDE/compiler can build it without you creating Java package folders manually.

## Fastest method: GitHub Actions

1. Create a GitHub repository.
2. Upload the contents of this ZIP.
3. Open **Actions**.
4. Select **Build OPCratesWorks**.
5. Click **Run workflow** (or push to `main`).
6. When the build finishes, open the workflow run and download the artifact named **OPCratesWorks-JAR**.
7. Put the resulting `.jar` into your Paper server's `plugins` folder.

GitHub provides Java 21 and Maven for the build, and Maven downloads the Paper 1.21.1 API dependency automatically.

## Replit

Import this project into a Java/Replit workspace. The included `.replit` and `replit.nix` files select Java 21 and Maven. Run:

    mvn clean package

The finished plugin will be:

    target/opcrates-works-1.0.0.jar

## Local Maven

Java 21 is required.

    mvn clean package

## Server

- Paper 1.21.1
- Put `target/opcrates-works-1.0.0.jar` into `plugins/`
- Restart the server

## Commands

    /opcrates help
    /opcrates givekey <player> [amount]
    /opcrates crate <player>
    /opcrates saber <player>
    /opcrates armor <player>
    /opcrates reload

The plugin does not require Skript, SkBee, ItemsAdder, Oraxen, MythicMobs, or another plugin dependency.
