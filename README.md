# nit

AI code review that runs on your machine. No SaaS, no API keys, no sending your code anywhere.

---

## Overview

Nit is a self-hosted AI code review agent. Point it at a file, a folder, or a GitHub PR and it gives you a real review instead of just style suggestions.

## Features

- Review local files and directories
- Generate tests for individual files
- Review GitHub PRs, commits, and file URLs
- Stream output in real time as the model is thinking
- Web UI included out of the box

## How It Works

Nit runs two models back to back. (`qwen2.5-coder:14b`) performs the full review across correctness, security, performance, style, and test coverage. A smaller model (`qwen2.5-coder:7b`) then critiques the output and flags anything weak or missing.

No AI frameworks. Just a Spring Boot backend making raw HTTP calls to a local Ollama instance with structured prompts.

## Prerequisites

- [Ollama](https://ollama.com) installed and running
- Java 17+

## Getting Started

Pull the models:

```bash
ollama pull qwen2.5-coder:14b
ollama pull qwen2.5-coder:7b
```

Clone and run:

```bash
git clone https://github.com/ChauAry21/nit.git
cd nit/nit-agent
./mvnw spring-boot:run
```

Open `http://localhost:8080` in your browser.

## Configuration

| Variable | Default | Description |
|---|---|---|
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama instance URL |
| `GITHUB_TOKEN` | - | Required for private GitHub repo access |

## Roadmap

- [ ] Docker Compose setup for one-command local deployment
- [ ] CLI interface (`nit-cli`)
- [ ] Multi-provider support (bring your own API key)

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot |
| AI Inference | Ollama (`qwen2.5-coder:14b` / `7b`) |
| Streaming | Server-Sent Events (SSE) |
