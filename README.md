# Claude Telegram Bot

A Spring Boot application that bridges **Telegram** and **Claude Code CLI**, letting you interact with Claude Code from any Telegram chat.

Send a message → the bot executes `claude -p` locally → returns the response. Conversations are maintained across messages using Claude's session system.

**[Español](#español)** | **[English](#english)**

---

## English

### Features

- **Conversational context** — maintains Claude sessions across messages
- **File & photo support** — send documents or images to Claude for analysis
- **Configurable working directory** — change where Claude operates with `/cd`
- **Async processing** — non-blocking message handling
- **Message splitting** — automatically splits long responses (Telegram's 4096 char limit)
- **Single-user auth** — restricts access to your chat ID only

### Prerequisites

- **Java 21+** — [Download](https://adoptium.net/)
- **Maven 3.8+** — [Download](https://maven.apache.org/download.cgi)
- **Claude Code CLI** — [Installation guide](https://docs.anthropic.com/en/docs/claude-code)
- **Telegram account**

### Step 1: Install Claude Code CLI

If you don't have Claude Code installed yet:

```bash
npm install -g @anthropic-ai/claude-code
```

Then authenticate with your Anthropic API key:

```bash
claude
# Follow the prompts to log in or set your API key
```

Verify it works:

```bash
claude -p "Hello, are you working?" --output-format json
```

You should get a JSON response with a `result` field. If this works, Claude Code is ready.

### Step 2: Create a Telegram Bot

1. Open Telegram and search for **[@BotFather](https://t.me/BotFather)**
2. Send `/newbot`
3. Choose a **name** for your bot (e.g., "My Claude Bot")
4. Choose a **username** (must end in `bot`, e.g., `my_claude_code_bot`)
5. BotFather will give you a **token** like `1234567890:ABCDefGhIJKlmNoPQRsTUVwxYZ`. Save it.

### Step 3: Get Your Chat ID

1. Open Telegram and search for **[@userinfobot](https://t.me/userinfobot)**
2. Send `/start`
3. The bot will reply with your **Chat ID** (a number like `123456789`). Save it.

### Step 4: Clone and Configure

```bash
git clone https://github.com/agreggio/claude-telegram-bot.git
cd claude-telegram-bot
```

Create your environment file:

```bash
cp .env.example .env
```

Edit `.env` with your values:

```
TELEGRAM_BOT_TOKEN=1234567890:ABCDefGhIJKlmNoPQRsTUVwxYZ
TELEGRAM_CHAT_ID=123456789
```

Optional settings (uncomment in `.env` if needed):

| Variable | Default | Description |
|---|---|---|
| `CLAUDE_PATH` | `claude` | Path to Claude CLI binary (if not in PATH) |
| `CLAUDE_TIMEOUT` | `120` | Max seconds to wait for Claude response |
| `DOWNLOAD_PATH` | `~/Downloads/telegram` | Where to save received files |

### Step 5: Build and Run

```bash
mvn clean package -DskipTests
java -jar target/claude-telegram-bot-1.0.0.jar
```

You should see Spring Boot start up. Now open your bot in Telegram and send a message — Claude will respond!

### Usage

#### Bot Commands

| Command | Description |
|---|---|
| `/help` | Show available commands |
| `/new` | Start a new conversation (clear context) |
| `/session` | Show current session ID |
| `/pwd` | Show current working directory |
| `/cd <path>` | Change working directory |
| `/timeout <seconds>` | Set command timeout |
| `/downloads` | Show downloads folder |

Any other text is sent directly to Claude Code.

#### Examples

- **Ask a question:** Just type naturally — `"Explain what a REST API is"`
- **Work on a project:** `/cd /path/to/your/project` then `"Read the README and summarize this project"`
- **Send a file:** Attach a document or photo, optionally with a caption like `"Review this code"`
- **Reset context:** `/new` to start a fresh conversation

### Architecture

```
Telegram API
    ↓ (long polling)
ClaudeCodeBot
    ├── Auth check (chatId)
    ├── Documents/Photos → download → prompt
    └── Text → command or query
            ↓
    ClaudeCodeService
        └── claude -p "prompt" --output-format json [--resume sessionId]
            ↓
        Parse JSON → extract result + session_id
            ↓
        Send response via Telegram API
```

### Tech Stack

- **Java 21** + **Spring Boot 3.4.2**
- **TelegramBots 9.3.0** (long polling)
- **Jackson** for JSON parsing
- **Lombok** for boilerplate reduction

### Troubleshooting

| Problem | Solution |
|---|---|
| `claude: command not found` | Set `CLAUDE_PATH` in `.env` to the full path (e.g., `/home/user/.local/bin/claude`) |
| Bot doesn't respond | Check that `TELEGRAM_CHAT_ID` matches your actual chat ID |
| Timeout errors | Increase `CLAUDE_TIMEOUT` in `.env` (complex queries may take longer) |
| Java version error | Ensure Java 21+ is installed: `java -version` |

---

## Español

### Funcionalidades

- **Contexto conversacional** — mantiene sesiones de Claude entre mensajes
- **Soporte de archivos y fotos** — envia documentos o imagenes para que Claude los analice
- **Directorio de trabajo configurable** — cambia donde opera Claude con `/cd`
- **Procesamiento asincrono** — manejo de mensajes sin bloqueo
- **Division de mensajes** — divide automaticamente respuestas largas (limite de 4096 caracteres de Telegram)
- **Autenticacion de usuario unico** — restringe el acceso solo a tu chat ID

### Requisitos previos

- **Java 21+** — [Descargar](https://adoptium.net/)
- **Maven 3.8+** — [Descargar](https://maven.apache.org/download.cgi)
- **Claude Code CLI** — [Guia de instalacion](https://docs.anthropic.com/en/docs/claude-code)
- **Cuenta de Telegram**

### Paso 1: Instalar Claude Code CLI

Si aun no tenes Claude Code instalado:

```bash
npm install -g @anthropic-ai/claude-code
```

Luego autenticate con tu API key de Anthropic:

```bash
claude
# Segui las instrucciones para iniciar sesion o configurar tu API key
```

Verifica que funcione:

```bash
claude -p "Hola, estas funcionando?" --output-format json
```

Deberias recibir una respuesta JSON con un campo `result`. Si funciona, Claude Code esta listo.

### Paso 2: Crear un Bot de Telegram

1. Abri Telegram y busca **[@BotFather](https://t.me/BotFather)**
2. Envia `/newbot`
3. Elegi un **nombre** para tu bot (ej: "Mi Bot Claude")
4. Elegi un **username** (debe terminar en `bot`, ej: `mi_claude_code_bot`)
5. BotFather te dara un **token** como `1234567890:ABCDefGhIJKlmNoPQRsTUVwxYZ`. Guardalo.

### Paso 3: Obtener tu Chat ID

1. Abri Telegram y busca **[@userinfobot](https://t.me/userinfobot)**
2. Envia `/start`
3. El bot te respondera con tu **Chat ID** (un numero como `123456789`). Guardalo.

### Paso 4: Clonar y configurar

```bash
git clone https://github.com/agreggio/claude-telegram-bot.git
cd claude-telegram-bot
```

Crea tu archivo de entorno:

```bash
cp .env.example .env
```

Edita `.env` con tus valores:

```
TELEGRAM_BOT_TOKEN=1234567890:ABCDefGhIJKlmNoPQRsTUVwxYZ
TELEGRAM_CHAT_ID=123456789
```

Configuraciones opcionales (descomenta en `.env` si las necesitas):

| Variable | Default | Descripcion |
|---|---|---|
| `CLAUDE_PATH` | `claude` | Ruta al binario de Claude CLI (si no esta en PATH) |
| `CLAUDE_TIMEOUT` | `120` | Segundos maximos de espera para la respuesta de Claude |
| `DOWNLOAD_PATH` | `~/Downloads/telegram` | Donde guardar archivos recibidos |

### Paso 5: Compilar y ejecutar

```bash
mvn clean package -DskipTests
java -jar target/claude-telegram-bot-1.0.0.jar
```

Deberias ver Spring Boot iniciarse. Ahora abri tu bot en Telegram y envia un mensaje — Claude va a responder!

### Uso

#### Comandos del Bot

| Comando | Descripcion |
|---|---|
| `/help` | Muestra los comandos disponibles |
| `/new` | Inicia una nueva conversacion (limpia el contexto) |
| `/session` | Muestra el ID de sesion actual |
| `/pwd` | Muestra el directorio de trabajo actual |
| `/cd <ruta>` | Cambia el directorio de trabajo |
| `/timeout <segundos>` | Configura el timeout del comando |
| `/downloads` | Muestra la carpeta de descargas |

Cualquier otro texto se envia directamente a Claude Code.

#### Ejemplos

- **Hacer una pregunta:** Escribi naturalmente — `"Explicame que es una API REST"`
- **Trabajar en un proyecto:** `/cd /ruta/a/tu/proyecto` y despues `"Lee el README y resumime este proyecto"`
- **Enviar un archivo:** Adjunta un documento o foto, opcionalmente con un caption como `"Revisa este codigo"`
- **Resetear contexto:** `/new` para comenzar una conversacion nueva

### Solucion de problemas

| Problema | Solucion |
|---|---|
| `claude: command not found` | Configura `CLAUDE_PATH` en `.env` con la ruta completa (ej: `/home/user/.local/bin/claude`) |
| El bot no responde | Verifica que `TELEGRAM_CHAT_ID` coincida con tu chat ID real |
| Errores de timeout | Aumenta `CLAUDE_TIMEOUT` en `.env` (consultas complejas pueden tardar mas) |
| Error de version de Java | Asegurate de tener Java 21+: `java -version` |

---

## License / Licencia

MIT License — see [LICENSE](LICENSE) for details.
