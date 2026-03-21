# BerryBushPlugin

## Basic Information

A Minecraft plugin that adds slow breaking for sweet berries.

### Versions

- Minecraft **1.14.4 - 1.21.11**
- \*WorldGuard **7.0.1**
- \*Vault **1.7.3**

  _\*Optional_

### Permissions

- `bbush.fast` – if a player has this permission, sweet berries will become ripe instantly.
- `bbush.access` – allows interaction with sweet berries.

## Configuration

- **Bush**
- - `bush.visible` – displays the bush-breaking animation.
- - `bush.cooldown` – cooldown time after breaking a bush.
- - `bush.break-time` – time required to break the bush.
- - `bush.final-action` – the final action that will occur with the bush.
- - - There are 3 actions:
- - - - `SET_TO_MINIMAL` – sets the plant to its minimum age.
- - - - `ONLY_DROP` – does nothing, just drops items.
- - - - `BREAK` – breaks the plant.
- - `bush.cooldown` – cooldown time after breaking a bush.