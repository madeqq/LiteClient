<p align="center">
  <img src="assets/logo.png" alt="LiteClient logo" width="420">
</p>

<p align="center">
  <strong>Open-source Minecraft client for authorized server security testing</strong><br>
  Free pentest toolkit · Crash & exploit modules · Custom protocol layer · Fabric 1.21.4
</p>

<p align="center">
  <a href="https://github.com/madeqq/FreeClient/releases/latest"><img src="https://img.shields.io/github/v/release/madeqq/FreeClient?label=Release&color=4aa3ff&logo=github" alt="Release"></a>
  <a href="https://github.com/madeqq/FreeClient/actions/workflows/release.yml"><img src="https://github.com/madeqq/FreeClient/actions/workflows/release.yml/badge.svg" alt="Release workflow"></a>
  <a href="https://github.com/madeqq/FreeClient/actions/workflows/ci.yml"><img src="https://github.com/madeqq/FreeClient/actions/workflows/ci.yml/badge.svg" alt="CI"></a>
  <img src="https://img.shields.io/badge/Open%20Source-MIT-57ffad?logo=opensourceinitiative&logoColor=white" alt="Open Source MIT">
  <img src="https://img.shields.io/badge/Pentest-Free-6ecb5a" alt="Free pentest">
  <img src="https://img.shields.io/badge/Minecraft-1.21.4-6ecb5a?logo=minecraft&logoColor=white" alt="Minecraft 1.21.4">
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white" alt="Java 21">
</p>

<p align="center">
  <a href="#download">Download</a> ·
  <a href="#security-testing-toolkit">Toolkit</a> ·
  <a href="#modules">Modules</a> ·
  <a href="#installation">Installation</a> ·
  <a href="#responsible-use">Responsible use</a> ·
  <a href="#development">Development</a>
</p>

---

## What is LiteClient?

**LiteClient** is a free, open-source Fabric client built for **security researchers, server owners, and administrators** who need to **stress-test and audit Minecraft server defenses** in controlled, authorized environments.

LiteClient is created by the developers of XynisClient who want to share their knowledge for free!

Join on Xynis Discord now: https://dsc.gg/xynis

It ships with a dedicated **module framework**, **custom packet pipeline**, and **reconnaissance utilities** so you can probe how your stack (Paper, Spigot, proxies, ViaVersion, anti-cheat plugins) behaves under real exploit and crash payloads, without paying for closed-source “crash clients.”

> **100% free. Fully open source.** Every merge to `main` produces a signed build artifact on [GitHub Releases](https://github.com/madeqq/FreeClient/releases/latest) with an auto-generated changelog.

---

## Why LiteClient?

| | Closed crash clients | **LiteClient** |
|---|---------------------|----------------|
| **Cost** | Paid / gated | **Free forever** |
| **Source code** | Closed | **MIT inspect, fork, extend** |
| **Purpose** | Often abuse-focused | **Documented security testing workflow** |
| **Extensibility** | Locked | **Module API + custom protocol layer** |
| **Updates** | Manual | **CI/CD release on every merge to `main`** |

Use it to answer questions like:

- Will oversized book NBT or container clicks take down my Paper fork?
- Does ViaVersion/ViaBackwards sanitize malicious item data on older protocols?
- Can a bundle exploit crash the main thread on 1.21.2+?
- Which plugins are exposed on my test instance (`!plugins`)?

---

## Security testing toolkit

### Exploit & crash modules
Configurable payloads executed through a unified module system. Open **ClickGUI** (`Right Shift`), pick a module, tune parameters, and fire with live toast feedback.

### Custom protocol layer
LiteClient bypasses vanilla packet limits where needed and sends crafted play-stage payloads (`PacketCodec`, container clicks, bundle packets) for reproducible tests.

### Recon & utility commands
Default chat prefix: `!` (configurable)

| Command | Role in pentest |
|---------|-----------------|
| `!crash` | List / run crash modules |
| `!exploit` | List / run exploit modules |
| `!plugins` | Enumerate server plugins |
| `!playerlist` | Inspect online players |
| `!hideentity` / `!hidenick` / `!fakegm` | Client-side test utilities |
| `!help` | Command reference |
| `!prefix` | Change command prefix |

### Operational UI
Built for long testing sessions not a reskinned vanilla client.

- **ClickGUI** - categorized modules (Crashers, Exploits, …) with per-module argument panels
- **Alt Manager** - offline + Microsoft profiles for multi-account test scenarios
- **Multiplayer screen** - async ping, player counts, direct connect
- **Connection progress** - staged join flow (handshake → auth → login → world)
- **HUD** - server IP, brand/engine fingerprint, last packet timing
- **ViaFabricPlus** integration - test cross-version protocol paths (optional)

---

## Modules

Current built-in payloads (more can be added via the module API):

| Module | Type | Target / notes |
|--------|------|----------------|
| **Book** | Crasher | Oversized writable book payloads Paper & ViaVersion/ViaBackwards stress |
| **Charged Projectiles** | Crasher | Projectile-related crash vectors |
| **Bundle** | Exploit | 1.21.2+ bundle instant-crash (protocol 768/769/770) |

Each module exposes typed arguments (packet count, payload size, timing, …) and documents usage in ClickGUI.

---

## Download

<a id="download"></a>

**[Latest release →](https://github.com/madeqq/FreeClient/releases/latest)**

1. Download `LiteClient-x.y.z.jar` from Releases.
2. Install [Fabric Loader](https://fabricmc.net/use/) for **Minecraft 1.21.4**.
3. Add [Fabric API](https://modrinth.com/mod/fabric-api) to `.minecraft/mods`.
4. Drop the LiteClient JAR into `.minecraft/mods`.
5. Launch with the Fabric profile.

```
.minecraft/mods/
├── fabric-api-….jar
└── LiteClient-1.0.0.jar
```

---

## Installation

| Requirement | Version |
|-------------|---------|
| Minecraft | **1.21.4** |
| Fabric Loader | **≥ 0.19.3** |
| Fabric API | [1.21.4 build](https://modrinth.com/mod/fabric-api) |
| Java | **21** |

**Recommended for cross-version tests:** [ViaFabricPlus](https://modrinth.com/mod/viafabricplus)

---

## Quick start (authorized testing)

1. Deploy a **dedicated test server** you own or have written permission to test.
2. Join with LiteClient and open **ClickGUI** (`Right Shift`).
3. Run recon: `!plugins`, `!playerlist` baseline the target stack.
4. Select a module (e.g. **Book**), configure conservative parameters, execute.
5. Monitor server logs, TPS, and crash reports document findings.
6. Patch, redeploy, re-test.

Config persists under `.minecraft/config/freeclient/`.

---

## Responsible use

<a id="responsible-use"></a>

> **LiteClient is a security research tool, not a weapon.**

- Test **only** servers you **own** or where you have **explicit written authorization**.
- Unauthorized denial-of-service or disruption of third-party servers is **illegal** in most jurisdictions and violates Minecraft’s Terms of Service.
- The authors provide this software **as-is** for defensive research and hardening. **You** are responsible for how it is used.

By using LiteClient, you agree to apply it ethically and legally.

---

## Development

<a id="development"></a>

### Build

```bash
git clone https://github.com/madeqq/FreeClient.git
cd FreeClient
./gradlew build
# → build/libs/LiteClient-<version>.jar
```

### Run client

```bash
./gradlew runClient
```

### Project layout

```
src/client/java/me/madeq/client/
├── module/       # Crash & exploit modules + framework
├── protocol/     # Custom Netty packet pipeline
├── command/      # Chat command system
├── gui/          # ClickGUI & test-oriented menus
├── alt/          # Multi-account testing
├── notify/       # Execution feedback toasts
└── hud/          # Server fingerprint overlay
```

### CI/CD

| Workflow | Trigger | Output |
|----------|---------|--------|
| **CI** | PR & push to `main` | Build verification (+ PR artifact) |
| **Release** | Merge to `main` | GitHub Release + JAR + changelog |

Release tags: `v<mod_version>-build.<run_number>` (e.g. `v1.0.0-build.42`).

---

## Contributing

Contributions welcome especially **new modules**, **protocol improvements**, and **documentation** for defensive hardening.

1. Fork the repository
2. Create a feature branch
3. Open a pull request with a clear description of the test scenario your change supports

---

## Credits

**LiteClient** | **madeq** & **0WhiteDev**

Built with [Fabric](https://fabricmc.net/) · [Fabric Loom](https://github.com/FabricMC/fabric-loom) · [MinecraftAuth](https://github.com/RaphiMC/MinecraftAuth)

---

## License

[MIT License](LICENSE) - free to use, modify, and distribute with attribution.

---

<p align="center">
  <img src="assets/logo.png" alt="LiteClient" width="180"><br>
  <sub>Open-source server security testing for Minecraft · Use responsibly</sub>
</p>
