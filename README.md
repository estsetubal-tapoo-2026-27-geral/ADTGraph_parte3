# ADT Graph — Parte 3 | Travessias na rede de metro

**Tópicos Avançados de POO — 2026/27**  
**Aula A3.3 — Atividades BFS e DFS**

## Antes de começar

Este projeto fornece o **código da Parte 2 já concluído**, incluindo a rede de metro, as classes `Station` e `Connection`, as estatísticas e a visualização. Executem o projeto e confirmem o seu funcionamento antes de acrescentarem as travessias.

Nesta atividade vão implementar duas operações: percorrer as estações em **largura (BFS)** e em **profundidade (DFS)** a partir de uma estação escolhida.

Após concluírem as travessias, realizem a **atividade complementar da secção 7**: verificar a existência de um caminho, devolver as suas arestas por ordem e calcular o tempo total.

### Projeto fornecido e execução

Utilizem **Java 17 ou superior e Maven**. Abram como projeto Maven a pasta que contém `pom.xml`.

| Ficheiro | Estado no projeto da Parte 3 |
|---|---|
| `MetroNetworkImpl` | Remoções, estatísticas, cores e paralelas da Parte 2 resolvidas; cinco métodos A3.3 com TODO |
| `MetroNetwork` | Assinaturas e contratos das novas operações já preparados |
| `LineColour` e `Connection` | Cor da linha e tempo da ligação implementados |
| `MetroView` | Manipulação, estatísticas, seleção da linha e cores dos traços implementadas |
| `MetroSample` | Rede fictícia com quatro ligações coloridas e Aeroporto isolado |
| `Main` | Demonstração funcional das Partes 1–2; comentários indicam onde acrescentar os resultados A3.3 |
| `MetroNetworkTest` | Esqueleto da classe de testes JUnit 5; comentários indicam onde construir a rede de teste e acrescentar os testes A3.3 |

Os ficheiros Java estão em `src/main/java/tapoo/metro`; os testes estão em `src/test/java/tapoo/metro/model`. `Connection` é uma classe independente em `model/Connection.java`, mantendo a organização da Parte 2, e guarda o tempo estimado e a cor da linha. Quando necessário, importem `tapoo.metro.model.Connection`.

A rede identifica estações pelo **nome normalizado**, não pela identidade dos objetos `Station`. `checkStation` recebe `Station`, valida-a e devolve o vértice correspondente (`Vertex<Station>`), cujo elemento é a estação registada. Para comparar estações em resultados, resolvam-nas com `checkStation` e comparem os vértices, ou comparem os nomes normalizados; `Station` não redefine `equals`, pelo que `contains` com um novo objeto `Station` devolve `false`.

A rede é fictícia. Permite ligações paralelas para cores diferentes e rejeita laços e duplicados do mesmo par/cor. `directTravelMinutes` devolve o menor tempo entre ligações **diretas**. O `Main` acrescenta uma paralela BLUE Alameda–Saldanha para demonstrar grau versus vizinhos; a vista usa uma instância independente da rede inicial.

### Classes e interfaces Java a utilizar

| Finalidade | Classes/interfaces | Operações |
|---|---|---|
| Estações descobertas e controlo de repetições | `Set<Vertex<Station>>`, com `HashSet` | `add`, `contains` |
| Resultado pela ordem de processamento | `List<Station>`, com `ArrayList` | `add` |
| Estações pendentes no BFS | `Queue<Vertex<Station>>`, com `LinkedList` | `offer`, `poll`, `isEmpty` |
| Estações pendentes no DFS iterativo | `Stack<Vertex<Station>>` | `push`, `pop`, `isEmpty` |
| Aresta de descoberta de cada estação (atividade complementar) | `Map<Vertex<Station>, Edge<Connection, Station>>`, com `HashMap` | `put`, `get` |
| Caminho ordenado (atividade complementar) | `List<Edge<Connection, Station>>`, com `ArrayList` | `add`, iteração |
| Inverter o caminho reconstruído | `Collections` | `reverse` |

Utilizem os tipos `Vertex`, `Edge` e `Graph` do ADT Graph já utilizado no projeto. Para obter os vizinhos, recorram a `incidentEdges` e `opposite`. **Não acedam à representação interna do grafo.**

O `Set` controla repetições; a `List` regista a ordem do resultado. No DFS, uma estação pode surgir mais de uma vez na pilha, mas só deve ser processada uma vez.

## 1. Objetivos e organização

- Relacionar FIFO com exploração em largura e LIFO com exploração em profundidade.
- Evitar processamento repetido em grafos com ciclos e arestas paralelas.
- Distinguir estações alcançáveis de todas as estações da rede.
- Integrar os algoritmos no domínio do metro e verificar os resultados.

Organização sugerida para a aula de 100 minutos: 25 minutos de teoria e 75 minutos de trabalho nas travessias. Como as atividades principais requerem cerca de 80–110 minutos de trabalho prático, prevejam concluir a validação fora da aula se necessário. As complementares de caminhos e tempo requerem cerca de 70–100 minutos e destinam-se a trabalho extra-aula.

## 2. Preparação — contrato das novas operações

Analisem as seguintes operações, já declaradas na interface `MetroNetwork`, e completem os métodos correspondentes em `MetroNetworkImpl`:

```java
List<Station> breadthFirstTraversal(Station start);
List<Station> depthFirstTraversal(Station start);
```

**Contrato comum:**

- Cada estação alcançável a partir da inicial surge exatamente uma vez.
- As estações de outras componentes do grafo não aparecem no resultado.
- Uma estação isolada origina uma lista com essa estação apenas.
- A operação não modifica estações, ligações nem a estrutura do grafo.
- Uma estação inicial `null` ou inexistente é rejeitada através de `InvalidMetroException`.
- Uma rede vazia não tem uma estação inicial válida; aplica-se a mesma regra de rejeição.

Reutilizem a resolução e validação de estações existente na Parte 2: utilizem `checkStation` para validar a estação recebida e obter o vértice correspondente na rede, segundo o critério do nome normalizado.

Antes de implementar, leiam o Javadoc fornecido e completem-no se necessário: finalidade, parâmetro, resultado e exceções.

## 3. Atividade 1 — Explorar a rede em largura (BFS)

**Pergunta:** em que ordem exploramos as estações quando começamos numa estação e visitamos primeiro as que estão a uma ligação de distância, depois a duas, e assim sucessivamente?

### 3.1. Simular antes de implementar

Escolham uma estação da rede da Parte 2. Simulem o BFS em papel e registem, em cada passo:

| Passo | Estação retirada da fila | Estações descobertas | Fila após o passo | Resultado |
|---|---|---|---|---|
| Inicial | — | Estação inicial | Estação inicial | Vazio |

Escrevam a fila da frente para o fim. Indiquem a ordem pela qual consideram os vizinhos e mantenham essa ordem durante a simulação.

### 3.2. Implementar

Implementem `breadthFirstTraversal` em `MetroNetworkImpl`, seguindo o pseudocódigo:

```pascal
Algorithm BFS(graph, start)
    visited <- empty set
    pending <- empty queue
    result <- empty list

    add start to visited
    enqueue(pending, start)

    while pending is not empty do
        current <- dequeue(pending)
        append current.element to result

        for each edge in graph.incidentEdges(current) do
            neighbour <- graph.opposite(current, edge)
            if neighbour is not in visited then
                add neighbour to visited
                enqueue(pending, neighbour)
            end if
        end for
    end while

    return result
```

A estação é marcada como **descoberta quando é colocada na fila**, pelo que nunca entra nela mais do que uma vez.

### 3.3. Executar e interpretar

No `Main`, apresentem o resultado do BFS a partir da estação escolhida e comparem-no com a simulação. Se a ordem diferir, verifiquem se é igualmente válida: o ADT pode devolver os vizinhos noutra ordem (ver secção 5).

Respondam:

1. Porque são processadas primeiro as estações mais próximas em número de ligações?
2. A cor da linha ou o tempo estimado de uma ligação altera este algoritmo?
3. Se o resultado tiver menos estações do que a rede, o que podemos concluir?

O BFS considera aqui apenas a existência de ligações. A lista da travessia **não é um itinerário**: duas estações consecutivas na lista podem não estar diretamente ligadas. Para reconstruir um caminho com menos ligações, seria necessário guardar também a aresta pela qual cada estação foi descoberta (aresta de descoberta); essa extensão é proposta na atividade complementar da secção 7.

## 4. Atividade 2 — Explorar a rede em profundidade (DFS)

**Pergunta:** como exploramos um ramo da rede antes de regressar às alternativas ainda pendentes?

### 4.1. Simular antes de implementar

Utilizem a mesma estação inicial e a mesma ordem de consideração dos vizinhos da atividade anterior. Registem:

| Passo | Estação retirada da pilha | Estações visitadas | Pilha após o passo | Resultado |
|---|---|---|---|---|
| Inicial | — | Vazio | Estação inicial | Vazio |

Identifiquem explicitamente o topo da pilha. O último vizinho colocado será o primeiro retirado. Se a estação retirada já tiver sido visitada, é ignorada.

### 4.2. Implementar

Implementem `depthFirstTraversal` em `MetroNetworkImpl` usando `Stack`:

```pascal
Algorithm DFS(graph, start)
    visited <- empty set
    pending <- empty stack
    result <- empty list
    
    add start to visited
    push(pending, start)
    
    while pending is not empty do
        current <- pop(pending)
        append current.element to result
        
        for each edge in graph.incidentEdges(current) do
            neighbour <- graph.opposite(current, edge)
            if neighbour is not in visited then
                add neighbour to visited
                push(pending, neighbour)
            end if
        end for
    end while

    return result
```

A estrutura é idêntica à do BFS, substituindo apenas a fila (*queue*, FIFO) por uma pilha (*stack*, LIFO). Como cada estação é marcada como **descoberta logo quando é empilhada**, nunca entra na pilha mais do que uma vez. *(Nota: numa variante estrita do DFS iterativo, a marcação em `visited` só ocorre ao retirar da pilha com `pop`, o que permitiria entradas repetidas na pilha para garantir uma ordem de profundidade estrita em grafos com ciclos; contudo, nesta atividade adotamos a marcação ao inserir por simplicidade e simetria direta com o BFS).*

Como a pilha segue uma disciplina LIFO, o último vizinho empilhado será o primeiro a ser explorado. Se quiserem visitar primeiro o primeiro vizinho devolvido pelo ADT, terão de colocar os vizinhos na pilha pela ordem inversa. Essa adaptação é opcional; documentem a ordem adotada.


### 4.3. Executar e comparar

No `Main`, apresentem BFS e DFS a partir da mesma estação.

Respondam:

1. A ordem de visita é necessariamente igual nos dois algoritmos?
2. Os conjuntos de estações devolvidos devem ser iguais? Justifiquem.
3. Porque é necessário guardar as estações já descobertas/visitadas num grafo com ciclos?
4. O DFS garante um caminho com o menor número de ligações?
5. Se duas linhas ligarem o mesmo par de estações, essa estação deve aparecer duas vezes?

Tal como no BFS, a lista do DFS representa a **ordem de processamento**, não um itinerário entre as estações listadas.

## 5. Verificação dos resultados

Comecem pela rede da Parte 2. Para verificar os casos que essa rede não contenha, criem redes pequenas e independentes nos testes JUnit (`MetroNetworkTest`), sem alterar permanentemente os dados do exemplo.

| Caso | O que verificar em BFS e DFS |
|---|---|
| Estação inicial válida | É o primeiro elemento do resultado |
| Rede com ciclo | A execução termina e não repete estações |
| Estação isolada | O resultado contém apenas a estação inicial |
| Duas componentes | Só aparecem estações da componente inicial |
| Arestas paralelas | Não provocam repetição de estações |
| Estação desconhecida ou `null` | É lançada `InvalidMetroException` |
| Rede vazia | A estação inicial é rejeitada |
| Duas execuções sucessivas | O estado da primeira não interfere na segunda |
| Preservação da rede | As estações e as ligações mantêm-se após a travessia |

Para comparar uma **sequência exata**, a ordem dos vizinhos tem de estar definida. A implementação `GraphEdgeList` **não garante** a ordem devolvida por `incidentEdges`, que pode mudar entre execuções. Por isso, nos testes, verifiquem as propriedades do algoritmo (primeira estação, conjunto de estações alcançadas, ausência de repetições e, no BFS, que as estações mais próximas surgem antes das mais distantes), sem exigir uma única sequência entre estações igualmente elegíveis.

### Rede pequena para simulação e testes

Criem quatro estações de teste `A`, `B`, `C` e `D`, com ligações não orientadas `A–B`, `A–C`, `B–C` e `C–D`. Acrescentem uma estação isolada `E`. Usem tempos válidos e cores de linha admitidas na Parte 2. Construam esta rede no método `setUp` de `MetroNetworkTest`, para que cada teste comece com uma rede nova. Se atribuírem já `3` minutos a `A–C` e `4` minutos a `C–D`, poderão reutilizá-la na secção 7.3.

Na simulação em papel, assumindo que os vizinhos são considerados por ordem alfabética e colocados nas estruturas nessa ordem:

| Operação | Resultado esperado |
|---|---|
| BFS a partir de A | A, B, C, D |
| DFS a partir de A, com o pseudocódigo acima | A, C, D, B |
| BFS ou DFS a partir de E | E |

Ao executar o código, BFS e DFS podem devolver outra ordem igualmente válida (por exemplo, BFS `A, C, B, D`), consoante a ordem dos vizinhos devolvida pelo ADT. Esta rede permite observar ciclos, uma estação isolada e a diferença entre a fila e a pilha. Os nomes são apenas dados de teste; as operações continuam a trabalhar com objetos `Station`.

## 6. Critérios de conclusão

- [ ] A Parte 2 continua funcional, incluindo a visualização e as estatísticas.
- [ ] A interface contém as operações e o respetivo Javadoc.
- [ ] O BFS utiliza `Queue` com `LinkedList`.
- [ ] O DFS iterativo utiliza `Stack`.
- [ ] Os algoritmos utilizam exclusivamente as operações públicas do ADT Graph.
- [ ] As listas contêm cada estação alcançável uma única vez.
- [ ] As entradas inválidas são tratadas de acordo com o contrato.
- [ ] O `Main` apresenta os dois percursos a partir da mesma estação.
- [ ] Foram verificadas as situações da tabela aplicáveis ao modelo.
- [ ] O grupo consegue explicar FIFO, LIFO, ciclos e arestas paralelas.
- [ ] O grupo distingue uma travessia de um caminho e de um caminho de menor tempo.

**Finalização:** alterações à interface, implementação das duas travessias, demonstração no `Main` e testes. Acrescentem ao README do grupo as duas simulações e uma breve comparação dos resultados.

## 7. Atividade complementar — Encontrar um caminho e calcular o seu tempo
**Estes exercícios deverão ser realizados em tempo extra-aula. A sua resolução é vivamente aconselhada.**

Realizem estes exercícios pela ordem apresentada. Utilizem a rede não orientada do metro e as classes de domínio já existentes. As três operações principais já estão declaradas na interface `MetroNetwork`. Leiam os respetivos contratos e completem as implementações com TODO em `MetroNetworkImpl`. A operação auxiliar para avançar num caminho com `opposite` (secção 7.2) é acrescentada por vocês à interface.

Tal como nas travessias, validem as estações antes de iniciar a pesquisa e lancem `InvalidMetroException` para parâmetros `null` ou estações inexistentes.

### 7.1. Exercício adicional 1 — Existe um caminho?

Implementem:

```java
boolean hasPath(Station origin, Station destination);
```

O método deve devolver `true` se for possível chegar à estação de destino a partir da origem, utilizando zero ou mais ligações, e `false` caso contrário.

**Orientações:**

1. Reutilizem o BFS ou o DFS para identificar as estações alcançáveis a partir da origem.
2. Verifiquem se a estação de destino pertence ao resultado, segundo o critério de identidade/igualdade da Parte 2.
3. Comparem esta operação com `hasDirectConnection`: pode existir um caminho sem existir uma ligação direta.

Se a origem e o destino representarem a mesma estação válida, o resultado é `true`: existe um caminho de comprimento zero. Validem sempre os dois parâmetros, mesmo nesse caso.

**Verificação:** na rede da secção 5, `hasPath(A, D)` deve devolver `true`, `hasPath(A, E)` deve devolver `false` e `hasPath(E, E)` deve devolver `true`.

### 7.2. Exercício adicional 2 — Devolver as arestas de um caminho

Implementem:

```java
List<Edge<Connection, Station>> findPath(
        Station origin, Station destination);
```

O método deve devolver uma **lista de arestas ordenada da origem para o destino**, correspondente a um caminho com o menor número de ligações. Utilizem **BFS**.

**Contrato:**

- A primeira aresta liga a origem à estação seguinte do caminho.
- Cada aresta seguinte dá continuidade ao caminho a partir da estação alcançada pela anterior.
- A última aresta permite alcançar o destino.
- As arestas pertencem à rede e o caminho não repete estações.
- Se não existir caminho, devolvam uma lista vazia.
- Se a origem e o destino forem a mesma estação válida, devolvam uma lista vazia, correspondente ao caminho de comprimento zero.
- Se existirem vários caminhos com o mesmo número mínimo de ligações, podem devolver qualquer um deles.

`hasPath` permite distinguir os dois casos que originam uma lista vazia.

**Porquê uma lista de arestas?** A lista preserva a sequência do percurso. Guardar a aresta concreta permite também identificar a linha e o tempo da ligação escolhida, mesmo quando existem arestas paralelas entre as mesmas estações.

#### A. Adaptar a pesquisa BFS

Ao descobrir uma estação pela primeira vez, guardem num `Map` a **aresta pela qual foi descoberta**. Não substituam esse registo quando voltarem a encontrar a estação.

```pascal
Algorithm FIND_PATH(graph, origin, destination)
    visited <- empty set
    discoveryEdge <- empty map
    pending <- empty queue

    add origin to visited
    enqueue(pending, origin)

    while pending is not empty do
        current <- dequeue(pending)
        if current equals destination then
            return RECONSTRUCT_PATH(graph, origin,
                                    destination, discoveryEdge)
        end if

        for each edge in graph.incidentEdges(current) do
            neighbour <- graph.opposite(current, edge)
            if neighbour is not in visited then
                add neighbour to visited
                discoveryEdge[neighbour] <- edge
                enqueue(pending, neighbour)
            end if
        end for
    end while

    return empty list
```

Neste pseudocódigo, `origin` e `destination` são os vértices obtidos após validar e localizar as estações na rede.

#### B. Reconstruir o caminho

Comecem no destino. A aresta de descoberta identifica a ligação usada para lá chegar. Utilizem `opposite` para regressar ao vértice anterior e repitam até alcançar a origem. Como recolhem as arestas do destino para a origem, invertam a lista no final.

```pascal
Algorithm RECONSTRUCT_PATH(graph, origin, destination, discoveryEdge)
    path <- empty list
    current <- destination

    while current does not equal origin do
        edge <- discoveryEdge[current]
        append edge to path
        current <- graph.opposite(current, edge)
    end while

    reverse(path)
    return path
```

**Verificação:** na rede da secção 5, o caminho de `A` para `D` deve conter, por esta ordem, as arestas `A–C` e `C–D`. De `A` para `E`, a lista deve estar vazia. De `A` para `A`, também deve estar vazia, mas `hasPath(A, A)` deve devolver `true`.

No `Main`, apresentem o percurso começando pela origem e avançando com `opposite`, mostrando em cada ligação a linha e o tempo. Como o grafo é não orientado, a ordem dos extremos guardados numa aresta não determina o sentido em que a percorremos.

O `Main` só conhece a interface `MetroNetwork` e não tem acesso ao grafo. Por isso, **acrescentem à interface** uma operação que devolva a estação no outro extremo de uma ligação, por exemplo:

```java
Station oppositeStation(Station station, Edge<Connection, Station> edge);
```

Definam o contrato e o Javadoc e implementem-na em `MetroNetworkImpl` com `opposite`. Validem a estação e a aresta como nas restantes operações: estações inválidas ou inexistentes e arestas `null`, externas, removidas ou que não liguem à estação são rejeitadas com `InvalidMetroException`. Atenção: `opposite`, em `GraphEdgeList`, devolve `null` quando a aresta não liga ao vértice indicado.

**Questão:** este caminho tem o menor número de ligações. Tem necessariamente o menor tempo total? Justifiquem.

### 7.3. Exercício adicional 3 — Calcular o tempo total

Implementem:

```java
int totalTravelMinutes(List<Edge<Connection, Station>> path);
```

O método deve somar os tempos estimados guardados nos objetos `Connection` das arestas recebidas. Utilizem o método de acesso ao tempo já existente em `Connection`; não acrescentem um segundo atributo para guardar a mesma informação.

**Contrato:**

- A lista vazia tem tempo total `0`.
- Uma lista `null` ou que contenha uma aresta `null` é rejeitada com `InvalidMetroException`.
- As arestas recebidas devem ser referências válidas da rede atual; rejeitem referências externas ou removidas com `InvalidMetroException`, utilizando as operações públicas do ADT.
- Para este exercício, assumam que as arestas já constituem um caminho ordenado, como o devolvido por `findPath`. Não é necessário implementar uma nova verificação de continuidade.
- A operação não modifica a lista nem a rede.
- O cálculo inclui apenas os tempos das ligações. Não acrescentem tempos de espera nem de mudança de linha.

```pascal
Algorithm TOTAL_TRAVEL_MINUTES(path)
    validate path and its edges
    total <- 0
    for each edge in path do
        total <- total + travel minutes stored in edge.element
    end for
    return total
```

**Exemplo de teste:** atribuam `3` minutos à ligação `A–C` e `4` minutos à ligação `C–D`. O caminho devolvido por `findPath(A, D)` deve ter tempo total `7` minutos.

A soma de uma lista vazia é sempre `0`. Esse resultado, por si só, não indica que exista caminho: consultem `hasPath` para distinguir uma viagem de comprimento zero de um destino inalcançável.

### 7.4. Demonstração e critérios de conclusão dos exercícios adicionais

No `Main`, para um par de estações:

1. Indiquem se existe caminho.
2. Se existir, apresentem as arestas pela ordem do percurso, incluindo a linha de cada ligação.
3. Apresentem o tempo total estimado.
4. Repitam para um destino inalcançável e para uma origem igual ao destino.

| Caso | Resultado a verificar |
|---|---|
| Existe caminho, mas não ligação direta | `hasPath` devolve `true` |
| Destino noutra componente | `hasPath` devolve `false`; `findPath` devolve lista vazia |
| Origem igual ao destino | `hasPath` devolve `true`; caminho vazio; tempo `0` |
| Ciclos na rede | A pesquisa termina e o caminho não repete estações |
| Caminhos alternativos | O caminho devolvido tem o menor número de ligações |
| Arestas paralelas | É guardada a aresta concreta escolhida pelo BFS |
| Caminho devolvido | Partindo da origem, percorrer as arestas com `opposite` permite chegar ao destino |
| Lista vazia | Tempo total `0` |
| Lista ou aresta `null`, aresta externa ou removida | `InvalidMetroException` |
| Duas execuções sucessivas | Não partilham o estado da pesquisa |

- [ ] As três operações têm contrato e Javadoc.
- [ ] A interface inclui a operação acrescentada para avançar com `opposite`, com contrato, Javadoc e implementação em `MetroNetworkImpl`.
- [ ] `hasPath` distingue conectividade de ligação direta.
- [ ] `findPath` utiliza BFS e guarda a aresta de descoberta de cada estação.
- [ ] O caminho devolvido é uma lista ordenada de arestas.
- [ ] O tempo total corresponde à soma dos tempos dessas ligações.
- [ ] O grupo explica por que razão BFS não garante o caminho de menor tempo.

**Finalização complementar:** acrescentem as três operações, a operação acrescentada à interface (secção 7.2), a demonstração no `Main` e os testes aos elementos da finalização principal (secção 6). Registem uma situação em que o caminho com menos ligações possa ter um tempo superior ao de um caminho com mais ligações.


## Documentação do ADT Graph

Consultem o [Javadoc da interface Graph](https://javadoc.io/doc/com.brunomnsilva/smartgraph/2.2.0/com/brunomnsilva/smartgraph/graph/Graph.html), em particular `vertices`, `edges`, `incidentEdges` e `opposite`. A dependência SmartGraph 2.2.0 está declarada no `pom.xml`.
