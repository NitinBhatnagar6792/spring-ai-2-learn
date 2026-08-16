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





