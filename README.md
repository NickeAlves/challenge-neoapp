# 📱 NeoApp - Desafio Estágio - REST API com Spring Boot

![Java](https://img.shields.io/badge/Java-17-red?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql) ![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?logo=docker)
![Fly.io](https://img.shields.io/badge/Deployed%20on-Fly.io-purple?logo=fly.io)

Uma **API REST** desenvolvida em **Java + Spring Boot**, com autenticação via **JWT**, documentação em **Swagger UI**, persistência em **PostgreSQL** e deploy via **Fly.io**.  
O projeto foi estruturado seguindo boas práticas de arquitetura, testes unitários e conteinerização com **Docker/Docker Compose**.

---

## 🔗 Links úteis

- **Diario de Criatividade**: [Notion](https://www.notion.so/Diario-de-criatividade-NeoApp-261e233f12c080d7bdecf6bde311c7cf?source=copy_link)
- **REST API**: https://challenge-neoapp.fly.dev/
- **Swagger UI**: https://challenge-neoapp.fly.dev/swagger-ui.html
- **Kanban (Trello)**: [Quadro do projeto](https://trello.com/invite/b/68af914cb00351a4dd03c803/ATTI31a240818db7bebe395ccccc46004857A420130E/neoapp)

---

## ⚙️ Tecnologias utilizadas

- **Java 17**
- **Spring Boot** (Web, Data JPA, Security, Validation, OpenAPI, Actuator)
- **PostgreSQL**
- **JWT (Auth0)**
- **Docker & Docker Compose**
- **Fly.io (Deploy)**
- **JUnit 5 e Mockito (Testes unitários)**

---

## 🚀 Funcionalidades principais

### 👤 Usuários (CRUD completo)

- Criar, atualizar, deletar e listar usuários (com paginação e ordenação).
- Buscar por **ID**, **CPF**, **e-mail**, **nome** ou **sobrenome**.

### 🔐 Autenticação

- Registro de usuário com validações.
- Login via **JWT**.
- Proteção de rotas com Spring Security.

### 📑 Documentação

- Swagger UI disponível em `/swagger-ui.html`.

---

## 🛠️ Endpoints

### AuthController (`/auth/v1`)

| Método | Endpoint  | Descrição                                                                          |
| ------ | --------- | ---------------------------------------------------------------------------------- |
| POST   | /register | Registra um novo usuário. Recebe `RegisterUserDTO`, retorna `RegisterResponseDTO`. |
| POST   | /login    | Realiza login. Recebe `LoginRequestDTO`, retorna `LoginResponseDTO`.               |

### UserController (`/users`)

| Método | Endpoint         | Descrição                                                                               |
| ------ | ---------------- | --------------------------------------------------------------------------------------- |
| GET    | /                | Lista usuários paginados (`page`, `size`, `sortBy`, `sortDirection`).                   |
| GET    | /{id}            | Busca usuário pelo `id`.                                                                |
| GET    | /email           | Busca usuário pelo `email`.                                                             |
| GET    | /cpf             | Busca usuário pelo `cpf`.                                                               |
| GET    | /search          | Busca usuários por termo `q` com paginação e ordenação.                                 |
| GET    | /search/name     | Busca usuários por `name`.                                                              |
| GET    | /search/lastname | Busca usuários por `lastName`.                                                          |
| PUT    | /{id}            | Atualiza usuário pelo `id`. Recebe `UpdateRequestUserDTO`, retorna `UpdateResponseDTO`. |
| DELETE | /{id}            | Deleta usuário pelo `id`. Retorna `DeleteResponseDTO`.                                  |

---

## 🧪 Testes

Foram implementados **testes unitários** cobrindo os principais fluxos do `UserService`, incluindo:

- Listagem paginada.
- Busca por ID, CPF e e-mail.
- Registro e login com validações.
- Atualização e exclusão de usuários.
- Utilitários (`capitalizeFirstLetters`, `calculateAge`).

---

## 🐳 Docker & Deploy

### Rodar localmente com Docker

```bash
# Build da imagem
docker build -t neoapp .

# Subir containers (API + PostgreSQL)
docker-compose up
```

### Deploy

O deploy foi realizado no **Fly.io**, com CI/CD integrado ao GitHub.

---

## 📚 Aprendizados

Durante o desenvolvimento aprendi e reforcei pontos importantes:

- A importância do **planejamento com ferramentas visuais** (Notion, Trello).
- Boas práticas no uso do **Spring Boot** (segurança, DTOs, validações).
- Criação de **testes unitários abrangentes**.
- **Conteinerização e deploy em nuvem** (Railway x Fly.io).
- A relevância de **documentar bem a API** (Swagger + DTOs claros).

---

## 📊 Resumo da Jornada

- **Dia 1:** Setup do projeto, dependências e CRUD inicial.
- **Dia 2:** CRUD completo + autenticação JWT.
- **Dia 3:** Testes unitários + Docker/Docker Compose.
- **Dia 4:** Deploy no Fly.io + ajustes de CORS.
- **Dia 5:** Refinamento de DTOs + exemplo público com docker-compose.

---

## 👤 Autor

**Nicolas Alves**  
🔗 [Portfólio](https://nportifolio.com/)  
📌 [GitHub](https://github.com/NickeAlves)
