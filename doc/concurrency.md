# L4 - Questão Aberta

## Lidando com Transações Concorrentes

### Problema
A autorização de transações de crédito deve garantir que o mesmo saldo não seja utilizado mais de uma vez em caso de requisições concorrentes. Além disso, como as transações são síncronas e o timeout é inferior a 100 ms, a latência deve ser extremamente baixa para garantir uma resposta rápida.

### Sugestões

#### Baixa Latência
Para manter a latência baixa, convém utilizar um banco de dados em memória, armazenando apenas os dados essenciais para as transações e mantendo todas as contas sempre disponíveis na memória.

#### Atomicidade
Para tratar a concorrência, um banco de dados rápido e capaz de garantir transações atômicas, como, por exemplo, o Redis, um data store em memória que é single-threaded e atômico, com latência na faixa dos microssegundos, pode ser adequado para resolver problemas de concorrência sob a imposição de um timeout muito curto.

#### Consistência eventual
Qualquer operação que não seja essencial para a autorização, pode ser realizada de forma assíncrona. Ao processar a transação, o autorizador deve gerar uma mensagem para uma fila para que outros serviços possam processar a transação de forma assíncrona e garantir a consistência com o autorizador. Exemplos de casos de uso incluem:
Exibição do resultado da transação e atualização do saldo no aplicativo
- Atualização do banco de dados de transações das contas
- Adição da transação ao serviço de cobrança do cartão
