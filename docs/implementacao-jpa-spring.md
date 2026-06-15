# Guia: Implementando JPA com Spring Boot

Este guia explica como configurar e usar **JPA (Java Persistence API)** com **Spring Data JPA** em um projeto Java Spring Boot, usando como referência a estrutura deste repositório (`com.br.app`).

---

## Índice

1. [Conceitos básicos](#1-conceitos-básicos)
2. [Pré-requisitos](#2-pré-requisitos)
3. [Passo a passo](#3-passo-a-passo)
4. [Exemplo completo: módulo User](#4-exemplo-completo-módulo-user)
5. [Relacionamentos entre entidades](#5-relacionamentos-entre-entidades)
6. [Consultas personalizadas](#6-consultas-personalizadas)
7. [Boas práticas](#7-boas-práticas)
8. [Problemas comuns](#8-problemas-comuns)

---

## 1. Conceitos básicos

| Termo | Descrição |
|-------|-----------|
| **JPA** | Especificação Java para mapear objetos Java em tabelas de banco de dados (ORM). |
| **Hibernate** | Implementação de referência do JPA usada pelo Spring Boot por padrão. |
| **Spring Data JPA** | Camada do Spring que simplifica repositórios — você declara interfaces e o Spring gera as queries. |
| **Entity** | Classe Java anotada com `@Entity` que representa uma tabela. |
| **Repository** | Interface que estende `JpaRepository` e expõe operações CRUD. |

**Fluxo típico:**

```
Controller → Service → Repository → Banco de dados
     ↑          ↑           ↑
   HTTP      Regras     JPA/Hibernate
             de negócio
```

---

## 2. Pré-requisitos

O projeto já possui a dependência principal no `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

**Falta adicionar um driver de banco de dados.** Sem ele, a aplicação não consegue se conectar. Escolha uma opção:

### Opção A — H2 (desenvolvimento / testes)

Banco em memória, ideal para aprender e testar localmente.

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Opção B — PostgreSQL (produção)

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Opção C — MySQL

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## 3. Passo a passo

### Passo 1 — Configurar o banco de dados

Edite `src/main/resources/application.properties`:

**Com H2 (desenvolvimento):**

```properties
spring.application.name=app

# H2 em memória
spring.datasource.url=jdbc:h2:mem:appdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Console H2 (acesse em http://localhost:8080/h2-console)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA / Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

**Com PostgreSQL:**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/appdb
spring.datasource.username=postgres
spring.datasource.password=sua_senha

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

> **`ddl-auto` — valores possíveis:**
> - `create` — recria as tabelas a cada inicialização (apaga dados)
> - `create-drop` — cria ao iniciar e apaga ao encerrar
> - `update` — atualiza o schema sem apagar dados (bom para dev)
> - `validate` — só valida o schema (recomendado em produção)
> - `none` — não altera o schema

---

### Passo 2 — Criar a entidade (Entity)

A entidade é a classe que representa uma linha da tabela no banco.

Crie o arquivo `src/main/java/com/br/app/user/User.java`:

```java
package com.br.app.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;
}
```

**Anotações principais:**

| Anotação | Função |
|----------|--------|
| `@Entity` | Marca a classe como entidade JPA |
| `@Table(name = "...")` | Nome da tabela no banco |
| `@Id` | Campo chave primária |
| `@GeneratedValue` | Gera o ID automaticamente |
| `@Column` | Configura coluna (nullable, unique, tamanho) |

---

### Passo 3 — Criar o repositório (Repository)

O repositório abstrai o acesso ao banco. O Spring implementa a interface automaticamente.

Crie `src/main/java/com/br/app/user/UserRepository.java`:

```java
package com.br.app.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
```

**Métodos herdados de `JpaRepository` (já disponíveis sem implementar):**

```java
userRepository.save(user);           // criar ou atualizar
userRepository.findById(1L);         // buscar por ID
userRepository.findAll();            // listar todos
userRepository.deleteById(1L);       // deletar por ID
userRepository.count();              // contar registros
```

**Query methods:** o Spring interpreta o nome do método e gera a query. Exemplos:

```java
List<User> findByName(String name);
List<User> findByNameContainingIgnoreCase(String name);
List<User> findByEmailAndName(String email, String name);
```

---

### Passo 4 — Criar o serviço (Service)

A camada de serviço concentra a lógica de negócio e fica entre o controller e o repositório.

Crie `src/main/java/com/br/app/user/UserService.java`:

```java
package com.br.app.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + id));
    }

    @Transactional
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("E-mail já cadastrado: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, User updated) {
        User existing = findById(id);
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        return userRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado: " + id);
        }
        userRepository.deleteById(id);
    }
}
```

> **`@Transactional`:** garante que operações de escrita sejam atômicas. Use `readOnly = true` em consultas para melhor performance.

---

### Passo 5 — Criar o controller REST

Substitua ou complemente o controller existente usando `@RestController` para retornar JSON.

Crie `src/main/java/com/br/app/user/UserRestController.java`:

```java
package com.br.app.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) {
        return userService.update(id, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

### Passo 6 — Executar e testar

```bash
./mvnw spring-boot:run
```

**Testes com curl:**

```bash
# Criar usuário
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"João Silva","email":"joao@email.com","password":"123456"}'

# Listar todos
curl http://localhost:8080/api/users

# Buscar por ID
curl http://localhost:8080/api/users/1

# Atualizar
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"João Santos","email":"joao@email.com","password":"123456"}'

# Deletar
curl -X DELETE http://localhost:8080/api/users/1
```

---

## 4. Exemplo completo: módulo User

Estrutura de pacotes sugerida:

```
src/main/java/com/br/app/
├── AppApplication.java
└── user/
    ├── User.java              ← Entidade (@Entity)
    ├── UserRepository.java  ← Repositório (JpaRepository)
    ├── UserService.java     ← Lógica de negócio
    └── UserRestController.java ← Endpoints REST
```

**Resumo das responsabilidades:**

| Camada | Responsabilidade |
|--------|------------------|
| **Entity** | Mapeamento objeto ↔ tabela |
| **Repository** | Acesso a dados (CRUD, queries) |
| **Service** | Regras de negócio, validações |
| **Controller** | Receber requisições HTTP, retornar respostas |

---

## 5. Relacionamentos entre entidades

### One-to-Many (1:N) — Um usuário tem vários pedidos

```java
// User.java
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Order> orders = new ArrayList<>();

// Order.java
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
```

### Many-to-Many (N:N) — Usuários e papéis (roles)

```java
// User.java
@ManyToMany
@JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
private Set<Role> roles = new HashSet<>();
```

### FetchType

| Tipo | Comportamento |
|------|---------------|
| `LAZY` | Carrega o relacionamento só quando acessado (padrão recomendado) |
| `EAGER` | Carrega junto com a entidade principal (pode causar N+1 queries) |

---

## 6. Consultas personalizadas

### JPQL com `@Query`

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
    List<User> searchByName(@Param("name") String name);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findUserByEmail(@Param("email") String email);
}
```

### SQL nativo

```java
@Query(value = "SELECT * FROM users WHERE name ILIKE %:name%", nativeQuery = true)
List<User> searchNative(@Param("name") String name);
```

### Paginação

```java
// Repository
Page<User> findByNameContaining(String name, Pageable pageable);

// Service
public Page<User> findPaginated(String name, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
    return userRepository.findByNameContaining(name, pageable);
}

// Controller
@GetMapping("/search")
public Page<User> search(
        @RequestParam String name,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    return userService.findPaginated(name, page, size);
}
```

---

## 7. Boas práticas

1. **Nunca exponha a entidade diretamente** em APIs públicas se houver campos sensíveis (ex.: `password`). Use DTOs:

```java
public record UserRequestDTO(String name, String email, String password) {}

public record UserResponseDTO(Long id, String name, String email) {}
```

2. **Use `@Transactional` no Service**, não no Controller.

3. **Prefira `LAZY`** em relacionamentos para evitar carregar dados desnecessários.

4. **Em produção**, use `ddl-auto=validate` ou ferramentas de migração (Flyway/Liquibase) em vez de `update`.

5. **Trate exceções** com `@ControllerAdvice` para retornar erros HTTP padronizados:

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handle(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
```

6. **Evite `findAll()`** em tabelas grandes — use paginação.

7. **Injeção por construtor** (como nos exemplos) é preferível a `@Autowired` em campos.

---

## 8. Problemas comuns

| Erro | Causa provável | Solução |
|------|----------------|---------|
| `Failed to configure a DataSource` | Sem driver de banco no `pom.xml` | Adicione H2, PostgreSQL ou MySQL |
| `Table 'users' doesn't exist` | `ddl-auto=none` ou banco vazio | Use `ddl-auto=update` em dev |
| `LazyInitializationException` | Acesso a relacionamento LAZY fora de transação | Use `@Transactional` no service ou fetch join |
| `Duplicate entry for email` | Constraint `unique` violada | Valide com `existsByEmail()` antes de salvar |
| Entidade não encontrada pelo Spring | Classe fora do pacote scan | Mantenha entidades em subpacotes de `@SpringBootApplication` |

---

## Referências

- [Spring Data JPA — Documentação oficial](https://docs.spring.io/spring-data/jpa/reference/)
- [Hibernate ORM — Guia de referência](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/)
- [Baeldung — Spring Data JPA](https://www.baeldung.com/the-persistence-layer-with-spring-data-jpa)
