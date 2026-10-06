# OPCratesWorks 1.0.0

Paper 1.21.1 custom OP crate plugin source project.

## Features
- OP crate key
- OP crate GUI
- Random OP rewards
- Void Saber
- Void Armor set
- Saber special hit effect
- Full armor set passive effects
- Custom Model Data IDs for resource-pack support
- No Skript or third-party plugin dependency

## Build
This ZIP is SOURCE, not a server-installable JAR.

Java 21 + Maven:

    mvn clean package

Output:

    target/opcrates-works-1.0.0.jar

For an online build, use the included GitHub Actions workflow or Replit configuration. See `ONLINE-COMPILER.md`.

## Commands
- `/opcrates help`
- `/opcrates givekey <player> [amount]`
- `/opcrates crate <player>`
- `/opcrates saber <player>`
- `/opcrates armor <player>`
- `/opcrates reload`

## Version
- Paper API: 1.21.1
- Java: 21
