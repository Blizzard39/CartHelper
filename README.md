<div align="center">

# 🚃💥 Cart Helper

**A client-side Fabric mod that automates the bow → rail → TNT minecart combo**

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-62B47A?style=for-the-badge&logo=minecraft&logoColor=white)
![Fabric](https://img.shields.io/badge/Loader-Fabric-DBD0B4?style=for-the-badge)
![Side](https://img.shields.io/badge/Side-Client%20only-blue?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21%2B-orange?style=for-the-badge&logo=openjdk&logoColor=white)
[![Release](https://img.shields.io/github/v/release/Blizzard39/CartHelper?style=for-the-badge)](https://github.com/Blizzard39/CartHelper/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/Blizzard39/CartHelper/total?style=for-the-badge)](https://github.com/Blizzard39/CartHelper/releases)

[⬇️ Download](https://github.com/Blizzard39/CartHelper/releases/latest) · [📦 Installation](#-installation) · [🎮 How it works](#-how-it-works) · [⚙️ Controls](#️-controls) · [❓ FAQ](#-faq)

</div>

---

## ✨ What is Cart Helper?

Cart Helper is a small **client-side** quality-of-life mod for TNT minecart PvP. Instead of fumbling with your hotbar in the middle of a fight, you shoot your bow, tap the slot with your rails, and the mod does the rest: it places the rail and then a TNT minecart on top of it, in a single game tick by default (every delay is configurable). 🏹➡️🛤️➡️💣

Because it is client-side only, **nothing needs to be installed on the server** and the mod works in single player and on any server that allows client mods.

## 🎮 How it works

The mod is a simple chain of checks, like a big `if / elif / else`:

1. 🎒 **Preconditions:** your hotbar must contain a **bow**, at least one **TNT minecart** and at least one **rail** (any of the 4 types).
2. 🏹 **Bow fired:** the mod notices when you release the bow after drawing it long enough to actually fire an arrow.
3. ⏱️ **2-second window:** a timer (2 seconds by default) starts right when the arrow is fired.
4. 🔁 **You switch to a rail slot:** if, inside the window, you **switch to** a hotbar slot containing a rail, the mod arms itself. Simply holding a rail never does anything.
5. 🔍 **Placement check:** it looks at the block you are aiming at and checks that the rail can really be placed there (empty or replaceable space, and the rail can survive on the block below).
6. 🛤️ **Rail placed:** the rail is placed instantly.
7. 💣 **Cart placed:** the mod switches to a hotbar slot with a TNT minecart and places it on the new rail.
8. ♻️ **Loop:** shoot again and the whole thing starts over.

### 🧠 Smart details

- ✅ Works with all four rail types: **rail, powered rail, detector rail and activator rail** 🛤️⚡🔎🚦
- ✅ Works with **several TNT minecarts**: it uses the first slot that has one, and skips empty slots next time 💣💣
- ✅ **Never places randomly:** it only reacts when you switch to a rail slot inside the 2-second window after a shot 🎯
- ✅ Does nothing while a menu or inventory is open, or while you are a spectator 🧊
- ✅ Never places a rail if there is no TNT minecart to follow it ⛔

## 📦 Installation

### 🧰 Requirements

| Requirement | Version |
|---|---|
| 🎮 Minecraft: Java Edition | **1.21.11** |
| 🧵 [Fabric Loader](https://fabricmc.net/use/installer/) | latest |
| 🧩 [Fabric API](https://modrinth.com/mod/fabric-api) | for 1.21.11 |
| 🧭 [Mod Menu](https://modrinth.com/mod/modmenu) | optional, only for the in-game settings screen |
| ☕ Java | 21 or newer |

### 🪜 Steps

1. ⬇️ Download the latest `.jar` from the [**Releases**](https://github.com/Blizzard39/CartHelper/releases/latest) page (under **Assets**).
2. 🧵 Install **Fabric Loader** for Minecraft 1.21.11 with the official installer, and create the profile.
3. 🧩 Download **Fabric API** for 1.21.11.
4. 📂 Open your Minecraft folder:
   - 🪟 Windows: press `Win + R`, type `%appdata%\.minecraft`
   - 🐧 Linux: `~/.minecraft`
   - 🍎 macOS: `~/Library/Application Support/minecraft`
5. 📥 Put **both** `.jar` files (Cart Helper and Fabric API) in the `mods` folder. Create the folder if it does not exist.
6. ▶️ Start Minecraft with the **Fabric** profile. You are done! 🎉

## ⚙️ Controls

| Action | Default key |
|---|---|
| 🔛 Turn Cart Helper **on / off** | `J` |

The key can be changed any time: **Options → Controls → Key Binds → Cart Helper → Toggle Cart Helper** ⌨️

When you toggle the mod, a message appears in your own chat: `Cart Helper: ON` ✅ or `Cart Helper: OFF` ❌. The mod starts **off** every time you launch the game.

## License

Cart Helper is licensed under the GNU General Public License v3.0 or later (GPL-3.0-or-later).

This additional licensing offer also applies to earlier versions of Cart Helper authored by the project owner. Previously granted CC0 permissions remain valid.

See [LICENSE](LICENSE) for the full license text.
