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

1.a Download and install Ollama for Windows from 'https://ollama.com/download/windows':

[Download Ollama for Windows](https://ollama.com/download/windows)


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

2.a You can stop llama3 by pressing CTRL+C and then CTRL+D or by typing /exit command

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
        model: qwen/qwen3.8-27b
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

4.e.In the pom.xml add openai start dependency as shown below 

```
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>

```

4.f How to check if a particular model is still supported by GROQ

So it happened that in Aug-2026 model 'qwen/qwen3.6-27b' was available however by Oct-2026 it got replaced with 'qwen/qwen3.8-27b' so we can check if a model is still available using below CURL
Please Note: You might need to pass in request header => Authorization Bearer '{GROQ_API_KEY}'

```
curl --location 'https://api.groq.com/openai/v1/models/qwen/qwen3.8-27b' \
--header 'Authorization: Bearer '
```


### 5. Steps for configuring in memory H2 DB.

5.a in pom.xml add below dependencies

```
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-h2console</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
```

5.b in application.yaml add below properties

```
spring:
  datasource:
    hikari:
      initialization-fail-timeout: 60000
      minimum-idle: 2
      maximum-pool-size: 10
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: ""
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        '[format_sql]': true
  h2:
    console:
      enabled: true
      path: /h2-console
```

5.c include a sample favicon.ico to prevent 'No static resource favicon.ico for request '/favicon.ico'

```
folder Location: spring-ai-2-learn/src/main/resources/static <-- add favicon.ico file here
You can generate a favicon.ico using website https://favicon.io
```

5.d you can access the h2 console at http://localhost:8080/h2-console

5.e some sample h2 SQL are present in file spring-ai2-learn/h2-sql-cmds.sql


