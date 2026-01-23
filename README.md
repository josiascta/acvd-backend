# Guia de Execução do Backend

Este documento descreve os passos necessários para configurar e executar o ambiente de desenvolvimento do backend.

## 1. Configuração de Segurança (JWT)

Para que a autenticação JWT funcione corretamente, é necessário fornecer as chaves RSA.

1.  Adicione os arquivos de chave pública (`app.pub`) e privada (`app.key`) na pasta:
    `src/main/resources/`

2.  Certifique-se de que o arquivo de propriedades (`application.properties`) aponte corretamente para estes arquivos:

    ```properties
    jwt.public.key=classpath:app.pub
    jwt.private.key=classpath:app.key
    ```

---

## 2. Configuração do Google OAuth2

É necessário configurar as credenciais do Google para permitir o login social.

1.  Abra o arquivo `src/main/resources/application.properties`.
2.  Preencha as seguintes chaves com suas credenciais obtidas no Google Cloud Console:

    ```properties
    spring.security.oauth2.client.registration.google.client-id=SUA_CLIENT_ID_AQUI
    spring.security.oauth2.client.registration.google.client-secret=SEU_CLIENT_SECRET_AQUI
    ```

---

## 3. Inicialização do Banco de Dados

O banco de dados é gerenciado via Docker Compose.

1.  Abra o terminal e navegue até a pasta `db`:
    ```bash
    cd db
    ```

2.  Suba o container do banco de dados em segundo plano:
    ```bash
    docker compose up -d
    ```

---

## 4. Executando a Aplicação

Após realizar as configurações acima e garantir que o banco de dados esteja rodando, você pode iniciar a aplicação Spring Boot normalmente através da sua IDE ou via terminal (na raiz do projeto):

