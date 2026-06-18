# Arquitetura com DTO, Entity, Service, Repository e Mapper

## Objetivo

Esta arquitetura tem como objetivo separar claramente as responsabilidades de cada camada da aplicação, reduzindo o acoplamento entre a API, as regras de negócio e a persistência de dados.

### Benefícios

* Maior organização do código.
* Melhor manutenção e evolução da aplicação.
* Evita exposição acidental de dados sensíveis.
* Facilita testes unitários.
* Permite alterações na API sem impactar o banco de dados.
* Centraliza a lógica de conversão entre objetos.

---

# Visão Geral

```text
Cliente
   │
   ▼
Controller
   │
   ▼
DTO
   │
   ▼
Mapper
   │
   ▼
Entity
   │
   ▼
Service
   │
   ▼
Repository
   │
   ▼
Banco de Dados
```

Cada camada possui uma responsabilidade específica:

| Camada     | Responsabilidade                |
| ---------- | ------------------------------- |
| Controller | Receber e responder requisições |
| DTO        | Transporte de dados             |
| Mapper     | Conversão de objetos            |
| Entity     | Persistência                    |
| Service    | Regras de negócio               |
| Repository | Acesso aos dados                |

---

# Entity

Uma Entity representa uma tabela do banco de dados.

É o objeto gerenciado pelo JPA/Hibernate.

## Exemplo

```java
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String email;

    private String senha;

    // getters e setters
}
```

## Responsabilidades

* Representar tabelas.
* Definir relacionamentos.
* Participar da persistência.
* Ser gerenciada pelo JPA.

## Evite

* Expor Entities diretamente pela API.
* Utilizar Entities como contrato externo.

---

# DTO (Data Transfer Object)

DTOs representam os dados que trafegam entre camadas ou entre a aplicação e sistemas externos.

Não possuem responsabilidade de persistência.

## DTO de Entrada

```java
public record UsuarioRequestDTO(
        String nome,
        String email,
        String senha
) {
}
```

## DTO de Saída

```java
public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email
) {
}
```

## Benefícios

Permite expor apenas os dados necessários.

Exemplo:

Mesmo que a Entity possua:

```java
private String senha;
private Boolean ativo;
private LocalDateTime dataCriacao;
```

A API pode retornar apenas:

```json
{
  "id": 1,
  "nome": "João",
  "email": "joao@email.com"
}
```

---

# Mapper

## O que é

Mapper é um componente responsável pela conversão entre objetos.

Normalmente converte:

* DTO → Entity
* Entity → DTO

## Exemplo

```java
@Component
public class UsuarioMapper {

    public Usuario toEntity(UsuarioRequestDTO dto) {

        Usuario usuario = new Usuario();

        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(dto.senha());

        return usuario;
    }

    public UsuarioResponseDTO toDTO(Usuario usuario) {

        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail()
        );
    }
}
```

## Benefícios

Sem Mapper:

```java
Usuario usuario = new Usuario();
usuario.setNome(dto.nome());
usuario.setEmail(dto.email());
usuario.setSenha(dto.senha());
```

Esse código acaba espalhado por diversos lugares.

Com Mapper:

```java
Usuario usuario = mapper.toEntity(dto);
```

A conversão fica centralizada e reutilizável.

---

# Controller

Responsável pela comunicação com o mundo externo.

## Responsabilidades

* Receber requisições HTTP.
* Validar entrada.
* Chamar Services.
* Retornar respostas.

## Exemplo

```java
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;
    private final UsuarioMapper mapper;

    public UsuarioController(
            UsuarioService service,
            UsuarioMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public UsuarioResponseDTO criar(
            @RequestBody UsuarioRequestDTO dto) {

        Usuario usuario = mapper.toEntity(dto);

        Usuario salvo = service.salvar(usuario);

        return mapper.toDTO(salvo);
    }
}
```

---

# Service

Responsável pelas regras de negócio.

## Responsabilidades

* Aplicar validações.
* Orquestrar processos.
* Manipular Entities.
* Utilizar Repositories.

## Exemplo

```java
@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(
            UsuarioRepository repository
    ) {
        this.repository = repository;
    }

    public Usuario salvar(Usuario usuario) {

        validarEmail(usuario.getEmail());

        return repository.save(usuario);
    }

    private void validarEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email obrigatório"
            );
        }
    }
}
```

## Evite

* Código HTTP.
* RequestBody.
* ResponseEntity.
* Dependência de Controllers.

---

# Repository

Responsável pelo acesso aos dados.

## Exemplo

```java
@Repository
public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

}
```

## Responsabilidades

* Salvar dados.
* Buscar dados.
* Atualizar registros.
* Remover registros.

## Evite

* Regras de negócio.
* Conversão para DTO.

---

# Fluxo Completo

## Requisição

```http
POST /usuarios
```

```json
{
  "nome": "João",
  "email": "joao@email.com",
  "senha": "123456"
}
```

### 1. Controller

Recebe:

```java
UsuarioRequestDTO
```

### 2. Mapper

Converte:

```java
UsuarioRequestDTO
```

para

```java
Usuario
```

### 3. Service

Recebe a Entity.

Aplica regras de negócio.

### 4. Repository

Persiste a Entity.

### 5. Service

Recebe a Entity salva.

### 6. Mapper

Converte:

```java
Usuario
```

para

```java
UsuarioResponseDTO
```

### 7. Controller

Retorna:

```json
{
  "id": 1,
  "nome": "João",
  "email": "joao@email.com"
}
```

---

# Estrutura Recomendada

```text
src/main/java/com/exemplo

├── controller
│   └── UsuarioController.java
│
├── dto
│   ├── UsuarioRequestDTO.java
│   └── UsuarioResponseDTO.java
│
├── entity
│   └── Usuario.java
│
├── mapper
│   └── UsuarioMapper.java
│
├── repository
│   └── UsuarioRepository.java
│
└── service
    └── UsuarioService.java
```

---

# Boas Práticas

## Faça

✅ Utilize DTOs para entrada e saída da API.

✅ Utilize Entities apenas para persistência.

✅ Centralize conversões em Mappers.

✅ Mantenha Controllers simples.

✅ Mantenha Services focados em regras de negócio.

✅ Mantenha Repositories focados em acesso a dados.

---

## Evite

❌ Retornar Entities diretamente pela API.

❌ Colocar regras de negócio no Controller.

❌ Colocar conversões espalhadas pelo sistema.

❌ Fazer Service depender de Controller.

❌ Utilizar DTOs dentro do Repository.

---

# Evolução Natural

Em projetos maiores, a implementação manual dos Mappers pode ser substituída pelo MapStruct.

Exemplo:

```java
@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    Usuario toEntity(UsuarioRequestDTO dto);

    UsuarioResponseDTO toDTO(Usuario usuario);
}
```

O MapStruct gera automaticamente a implementação em tempo de compilação.

---

# Conclusão

A combinação de DTO + Mapper + Entity cria uma separação clara entre:

* Contratos da API.
* Regras de negócio.
* Persistência.

Essa abordagem reduz acoplamento, facilita manutenção, melhora a testabilidade e torna a aplicação mais preparada para crescer ao longo do tempo.
