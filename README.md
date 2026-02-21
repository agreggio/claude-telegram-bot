# Claude Telegram Bot

A Spring Boot application that bridges **Telegram** and **Claude Code CLI**, letting you interact with Claude Code from any Telegram chat.

Send a message → the bot executes `claude -p` locally → returns the response. Conversations are maintained across messages using Claude's session system.

## Features

- **Conversational context** — maintains Claude sessions across messages
- **File & photo support** — send documents or images to Claude for analysis
- **Configurable working directory** — change where Claude operates with `/cd`
- **Async processing** — non-blocking message handling
- **Message splitting** — automatically splits long responses (Telegram's 4096 char limit)
- **Single-user auth** — restricts access to your chat ID only

## Prerequisites

- **Java 21+**
- **Maven 3.8+**
- **[Claude Code CLI](https://docs.anthropic.com/en/docs/claude-code)** installed and authenticated
- **Telegram Bot Token** from [@BotFather](https://t.me/BotFather)

## Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/agreggio/claude-telegram-bot.git
   cd claude-telegram-bot
   ```

2. **Configure environment variables**

   ```bash
   cp .env.example .env
   ```

   Edit `.env` with your values:
   ```
   TELEGRAM_BOT_TOKEN=your-bot-token-here
   TELEGRAM_CHAT_ID=your-chat-id-here
   ```

   To get your Chat ID, send `/start` to [@userinfobot](https://t.me/userinfobot) on Telegram.

3. **Build and run**

   ```bash
   mvn clean package -DskipTests
   java -jar target/claude-telegram-bot-1.0.0.jar
   ```

## Bot Commands

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

## Configuration

All settings can be configured via environment variables or `application.yml`:

| Variable | Default | Description |
|---|---|---|
| `TELEGRAM_BOT_TOKEN` | *(required)* | Telegram bot token |
| `TELEGRAM_CHAT_ID` | *(required)* | Authorized chat ID |
| `CLAUDE_PATH` | `claude` | Path to Claude CLI binary |
| `CLAUDE_TIMEOUT` | `120` | Command timeout in seconds |
| `DOWNLOAD_PATH` | `~/Downloads/telegram` | Directory for downloaded files |

## Architecture

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

## Tech Stack

- **Java 21** + **Spring Boot 3.4.2**
- **TelegramBots 9.3.0** (long polling)
- **Jackson** for JSON parsing
- **Lombok** for boilerplate reduction

## License

MIT License — see [LICENSE](LICENSE) for details.
