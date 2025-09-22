# dynamo-test

Projeto Java (Spring) para demonstrar integração com AWS DynamoDB usando o AWS SDK v2 e o cliente Enhanced. Inclui endpoints básicos para verificar conexão e operar em uma tabela DynamoDB.

## Propósito
- Expor uma API simples que lê/escreve itens no DynamoDB.
- Facilitar o uso local com LocalStack ou uma conta AWS real.
- Fornecer configuração mínima via `application.properties`.

## Requisitos
- Java 25
- Maven 3.9+
- (Opcional) LocalStack com DynamoDB
- Credenciais AWS configuradas (se usar AWS real), por exemplo:
  - `aws configure` ou variáveis de ambiente padrão do SDK

## Configuração
Edite `src/main/resources/application.properties` conforme necessário:
- Região:
  - `app.dynamodb.region=us-east-1`
- Endpoint (opcional, para LocalStack):
  - `app.dynamodb.endpoint=http://localhost:4566`
- Nome da tabela:
  - `app.dynamodb.tableName=ddb-testable`

Exemplo (LocalStack):
```
app.dynamodb.region=us-east-1 
app.dynamodb.endpoint=http://localhost:4566 
app.dynamodb.tableName=ddb-testable
```

## Como rodar

- Via Maven:
  ```
  mvn spring-boot:run
  ```

- Via JAR:
  ```
  mvn clean package
  java -jar target/dynamo-test-*.jar
  ```

## Endpoints (resumo)
- Health/info do DynamoDB e tabela.
- CRUD básico de itens.
- Scan/consulta simples.

Obs: as rotas seguem convenções REST no contexto do projeto e podem incluir endpoints como:
- GET de informações do DynamoDB/tabela
- Operações de item (criar, buscar, listar)

## Uso com LocalStack (opcional)
1. Suba o LocalStack:
   ```
   docker run -p 4566:4566 localstack/localstack
   ```
2. Configure `app.dynamodb.endpoint` conforme acima.
3. As credenciais podem ser as padrão do LocalStack (qualquer valor) ou `aws configure` com perfil local.