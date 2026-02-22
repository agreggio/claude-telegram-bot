# Contributing / Contribuir

**[English](#english)** | **[Español](#español)**

---

## English

Thanks for your interest in contributing to Claude Telegram Bot!

### How to Contribute

1. **Fork** the repository
2. **Create a branch** from `main` for your changes:
   ```bash
   git checkout -b feature/my-feature
   ```
3. **Make your changes** and test them locally
4. **Commit** with a clear message describing what you changed and why
5. **Push** to your fork and open a **Pull Request** against `main`

### Development Setup

```bash
git clone https://github.com/<your-username>/claude-telegram-bot.git
cd claude-telegram-bot
cp .env.example .env
# Edit .env with your credentials
mvn clean package -DskipTests
```

### Guidelines

- Keep changes focused — one feature or fix per PR
- Follow the existing code style (Java conventions, Spring Boot patterns)
- Test your changes locally before submitting
- Update the README if your change affects usage or configuration
- Write clear commit messages

### Reporting Bugs

Open an [issue](https://github.com/agreggio/claude-telegram-bot/issues/new?template=bug_report.md) with:
- Steps to reproduce
- Expected vs actual behavior
- Your environment (Java version, OS)

### Suggesting Features

Open an [issue](https://github.com/agreggio/claude-telegram-bot/issues/new?template=feature_request.md) describing:
- The problem you want to solve
- Your proposed solution
- Any alternatives you considered

### Code of Conduct

Be respectful and constructive. We're all here to build something useful.

---

## Español

Gracias por tu interes en contribuir a Claude Telegram Bot!

### Como Contribuir

1. Hace **fork** del repositorio
2. **Crea un branch** desde `main` para tus cambios:
   ```bash
   git checkout -b feature/mi-feature
   ```
3. **Hace tus cambios** y testeelos localmente
4. **Commiteá** con un mensaje claro describiendo que cambiaste y por que
5. **Pusheá** a tu fork y abri un **Pull Request** contra `main`

### Setup de Desarrollo

```bash
git clone https://github.com/<tu-usuario>/claude-telegram-bot.git
cd claude-telegram-bot
cp .env.example .env
# Edita .env con tus credenciales
mvn clean package -DskipTests
```

### Lineamientos

- Mantené los cambios enfocados — un feature o fix por PR
- Segui el estilo de codigo existente (convenciones Java, patrones Spring Boot)
- Testea tus cambios localmente antes de enviar
- Actualiza el README si tu cambio afecta el uso o la configuracion
- Escribi mensajes de commit claros

### Reportar Bugs

Abri un [issue](https://github.com/agreggio/claude-telegram-bot/issues/new?template=bug_report.md) con:
- Pasos para reproducir
- Comportamiento esperado vs real
- Tu entorno (version de Java, SO)

### Sugerir Features

Abri un [issue](https://github.com/agreggio/claude-telegram-bot/issues/new?template=feature_request.md) describiendo:
- El problema que queres resolver
- Tu solucion propuesta
- Alternativas que consideraste

### Codigo de Conducta

Se respetuoso y constructivo. Estamos todos aca para construir algo util.
