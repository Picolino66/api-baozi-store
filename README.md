# Baozi Store API

API REST em Java 21, Spring Boot, Spring Data JPA e H2 para cadastrar clientes, produtos e pedidos. Cada pedido pertence a um cliente e contém um produto e sua quantidade.

## Executar

```bash
./mvnw spring-boot:run
```

A API fica em `http://localhost:8080`. O banco H2 é persistido em `./trabalho/data/baozi-store.mv.db`. Para inspeção local, o console H2 fica em `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./trabalho/data/baozi-store`, usuário `sa`, senha vazia). A pasta `trabalho/` guarda os dados locais e não é versionada.

## Endpoints

| Entidade | Criar | Listar | Consultar | Atualizar | Apagar |
|---|---|---|---|---|---|
| Cliente | `POST /clientes` | `GET /clientes` | `GET /clientes/{id}` | `PUT /clientes/{id}` | `DELETE /clientes/{id}` |
| Produto | `POST /produtos` | `GET /produtos` | `GET /produtos/{id}` | `PUT /produtos/{id}` | `DELETE /produtos/{id}` |
| Pedido | `POST /pedidos` | `GET /pedidos` | `GET /pedidos/{id}` | `PUT /pedidos/{id}` | `DELETE /pedidos/{id}` |

Todos os corpos são JSON. O campo `id` é gerado pelo banco. Criação retorna `201` e cabeçalho `Location`; exclusão retorna `204`. Recursos inexistentes retornam `404`, dados inválidos retornam `400`. Clientes e produtos vinculados a pedidos retornam `409` na exclusão; apague primeiro os pedidos.

### Exemplos

```json
POST /clientes
{"nome":"Isaías Gonçalves Ribeiro5145055","clienteDesde":"2026-10-12"}
```

```json
POST /produtos
{"nome":"Baozi de porco","preco":12.50,"estoque":true}
```

```json
POST /pedidos
{"clienteId":1,"produtoId":1,"quantidade":3}
```

Substitua os IDs do pedido pelos valores retornados nos cadastros. `clienteDesde` usa `AAAA-MM-DD`; `preco` deve ser maior que zero; `quantidade` deve ser inteira e positiva. O campo `estoque` indica disponibilidade do produto, como previsto no enunciado; não representa uma contagem de unidades.

## Postman

Importe [`Baozi Store.postman_collection.json`](postman/Baozi%20Store.postman_collection.json). A coleção guarda automaticamente os IDs retornados pelos POSTs. Execute primeiro **Criar cliente**, **Criar produto** e **Criar pedido**. Depois execute as listas, consultas por ID e exclusões, nessa ordem: pedido, cliente, produto. Tire as capturas solicitadas pelo enunciado diretamente no Postman, com a URL, o corpo/resposta e o status visíveis.

## Testes

```bash
./mvnw test
```

O teste de integração usa H2 em memória e verifica criação, listagem, consulta, validação, vínculos e exclusão.

## Relatório da atividade

Os materiais de entrega ficam em `trabalho/` e são ignorados pelo Git. O relatório local em `trabalho/docs/Relatorio Baozi Store.pdf` contém o estudo de caso, o diagrama de caso de uso e a especificação. Edite `trabalho/docs/relatorio.html` para inserir capturas reais do Postman e o link do repositório antes de entregar. Para gerar o PDF novamente com Chromium:

```bash
chromium --headless --no-sandbox --disable-gpu --no-pdf-header-footer \
  --print-to-pdf="trabalho/docs/Relatorio Baozi Store.pdf" "file://$PWD/trabalho/docs/relatorio.html"
```
