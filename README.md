# autorizador

Autorizador simples de transações de cartão de crédito.

## Instalação

Faça clone deste repositório no seu diretório de projetos
```
git clone https://github.com/hbolzan/autorizador.git
```

Este projeto depende do leiningen. Por favor, veja as instruções de instalação no [Codeberg](https://codeberg.org/leiningen/leiningen) ou no [GitHub](https://github.com/technomancy/leiningen). O projeto foi desenvolvido com a versão 2.11.2 do leiningen. Não foi testado com versões anteriores.

## Como usar

- Abra uma sessão do terminal
- `cd` para a pasta raiz do projeto

Para rodar os testes
```
lein test
```

Para iniciar o servidor, rode
```
lein run
```

Ao ser iniciado, o servidor é populado com amostras de dados de contas e comerciantes. Segue exemplo de chamada para o endpoint de transações
```
curl -X POST http://localhost:8838/api/v1/transaction -d '{"transaction": {"id": "bc71f950-ef3f-46ff-a71d-838a3ec85012", "account-id": "0df8e5ed-6052-4bce-aeb7-496ff698954b", "amount": "100.00", "merchant": "PADARIA DO ZE               SAO PAULO BR", "mcc": "5811"}}'
```

No namespace `autorizador.playground` existem vários exemplos de código que interagem com os controllers e até com o servidor. É uma boa opção para interagir com o serviço usando o REPL.

## Sobre o projeto

### Arquitetura
A arquitetura segue o padrão de portas e adaptadores. Esse padrão promove a separação de responsabilidades, facilitando a testabilidade, manutenção e extensibilidade do código.


A cada desafio, abri um PR separado para possibilitar a visualização da evolução do projeto em cada iteração.
- Desafio L1 - [PR 1](https://github.com/hbolzan/autorizador/pull/1)
- Desafio L2 - [PR 2](https://github.com/hbolzan/autorizador/pull/2)
- Desafio L3 - [PR 3](https://github.com/hbolzan/autorizador/pull/3)

### Testes
O projeto é amplamente coberto por testes unitários, visando abranger todos os casos de uso. Além disso, foram incluídos testes de integração. A análise dos testes permite compreender o funcionamento da lógica, conhecer os payloads e entender as intenções por trás do design de cada componente.

### Endpoints
`/api/v1/transaction, POST` - Envio de transações para autorização
`/api/v1/accounts`, GET` - Consulta a lista de todas as contas cadastradas
`/api/v1/accounts/:id`, GET` - Consulta uma conta
`/api/v1/merchants`, GET` - Consulta a lista de todos os comerciantes cadastrados
`/api/v1/merchants/:name`, GET` - Consulta um comerciante

### Banco de dados
Para o banco de dados, optei por uma abordagem simplificada utilizando um atom do Clojure como storage. Essa escolha me ajudou a manter o foco no desafio principal sem adicionar a complexidade de um banco de dados real. Ao mesmo tempo, as atualizações do atom são atômicas e serializadas, replicando o comportamento de um banco de dados com capacidades de transações atômicas.
 Isso garante a consistência nas operações de escrita. Para a persistência dos dados em disco, incluí no módulo de dados funções que realizam a gravação e leitura em arquivos `.edn` separados para cada entidade. As funções do banco de dados estão localizadas no namespace `autorizador.diplomat.db`.
 **Possíveis melhorias (fora do escopo do desafio):** Apesar de ter implementado funções de persistência em disco, a gravação não está sendo feita automaticamente. Eu utilizei esse recurso apenas para gerar os dados de amostra que são lidos automaticamente no momento que o servidor inicia. Uma possível melhoria seria a inclusão de um job periódico para fazer a persistência assíncrona.

### Modelos de dados
Utilizei Plumatic Schema para definir os modelos de dados, o que proporciona uma boa documentação tanto dos modelos quanto das funções, além de ser uma ferramenta valiosa para a qualidade dos testes. Em alguns casos, usei apenas os schemas wire sem conversão para modelos internos, pois isso consumiria tempo e teria pouca relevância considerando os objetivos do desafio. Isso deixa uma oportunidade para refatoração futura, onde modelos internos e seus adaptadores podem ser incluídos.

#### Sobre o modelo de accounts
O modelo de `accounts` possui um atributo `:transactions` que armazena cada transação que afeta os saldos da conta. No entanto, o modelo também contém os saldos, e essa redundância é intencional para aumentar o desempenho. As atualizações são atômicas, realizadas pela função passada para o swap! do atom, garantindo a consistência entre a lista de transações e os saldos. Uma possível melhoria seria calcular os saldos no momento da leitura dos dados persistidos.
