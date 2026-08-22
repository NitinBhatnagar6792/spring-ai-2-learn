# spring-ai-2-learn
This repository is for learning Spring AI 2.x

This repository contains my learning exercises and examples while exploring
Spring AI 2 and building AI applications using Java and Spring Boot.

## Technologies
- Java
- Spring Boot 4.x
- Spring AI 2.x
- Ollama
- Llama 3
- Maven

## Prerequisites
- Java 25+
- Maven
- Ollama
- Llama 3

## Local LLM Setup — Windows

### 1. Install Ollama

1.a Download and install Ollama for Windows:

https://ollama.com/download/windows

1.b Verify the installation in windows power shell or command prompt:

```bash
ollama --version
```
1.c Download llama3:

```bash
ollama pull llama3
```
1.d Run llama3:

```bash
ollama run llama3
```
1.e Test llama3 by typing a prompt after >>>

```bash
>>> Explain what is llama3?
You're referring to llama3!
... more response ...

Overall, Llama3 is a powerful tool for image-to-image translation tasks that requires a deep understanding of both the visual and semantic
aspects of the input images.

>>>
```
1.f Stop llama3:

```bash
ollama stop llama3
```

### 2. Additional Useful Ollama commands

2.a You can stop llama3 by pressing CTRL+C and then CTRL+D

2.b. Check the installed models
```bash
ollama list
```
2.c. Check whether Llama 3 is currently running/loaded
```bash
ollama ps
```
2.d. Remove the installed models
```bash
ollama rm llama3
```

### 3. Replacing llama3 with llama3.2 model as my laptop has limited memory
Reason: llama3 used around 5 GB where as llama3.2:1b use around 2 GB

```bash
ollama rm llama3
ollama pull llama3.2
ollama run llama3.2 
```

```
ollama list
NAME               ID              SIZE      MODIFIED
llama3.2:latest    a80c4f17acd5    2.0 GB    19 minutes ago
```

### 4. How to generate free OPENAI compatible keys using GROQ.

Reason: Spring AI's OpenAI integration expects an API that follows the OpenAI API format. Groq intentionally implements that same format, so you can use Spring AI's OpenAiChatModel against Groq simply by changing the base-url

4.a Go to https://console.groq.com/home and sign-up

4.b Go to API Keys page 'https://console.groq.com/keys' and click on Create API Key button

4.c.enter a key name, choose expiration duration and copy the key

4.d.In the application.yml add below section and use the value of key for GROQ_API_KEY

```
spring:
  ai:
    openai:
      base-url: https://api.groq.com/openai/v1
      api-key: ${GROQ_API_KEY}
      chat:
        model: llama-3.3-70b-versatile
```
the meanings are

```
| Configuration  | What it actually means                            |
| -------------- | ------------------------------------------------- |
| `openai:`      | Use Spring AI's **OpenAI-compatible integration** |
| `base-url`     | **Groq's** API endpoint                           |
| `api-key`      | Your **Groq** API key                             |
| `GROQ_API_KEY` | Environment variable containing your Groq key     |
| `model`        | A **Groq-hosted Llama model**                     |
```

