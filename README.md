# Análise de Desempenho de Tabelas Hash em Java

Este projeto implementa e analisa diferentes estratégias de tabelas hash em **Java**, considerando funções de hash e tamanhos de tabela variados para diferentes tamanhos de conjuntos de dados. Foram medidas métricas de **tempo, colisões, distribuição e memória**, com o objetivo de comparar o desempenho de **encadeamento separado, sondagem linear e hash duplo** em múltiplos cenários experimentais.

---

## 1. Objetivos

- Implementar ao menos uma estratégia de **encadeamento** e uma de **endereçamento aberto (rehashing)**.  
- Testar diferentes **funções de hash** e **tamanhos de tabela** com conjuntos grandes de dados.  
- Medir **tempos de inserção e busca**, número de **colisões**, **tamanho das listas encadeadas** e **gaps**.  
- Comparar os resultados em **tabelas e gráficos**.  
- *(Bônus)* Analisar **overhead de memória** em cenários com alta carga.

---

## 2. Tecnologias Utilizadas

- **Java 23** — Implementação das tabelas hash e gerador de dados.  
- **Python (pandas + matplotlib)** — Análise estatística e geração de gráficos a partir dos arquivos CSV.  

obs: Nenhuma estrutura de dados pronta foi utilizada além de vetores (`int[]`, `String[]`).

---


### Funções de Hash
- **mod**: método modular clássico (`key % m`)  
- **mul**: método multiplicativo de Knuth (`A * key % 1`)  
- **mix**: combinação de *xorshift* + *mod* para dispersão aprimorada  

### Estratégias Avaliadas
- Encadeamento separado *(chaining)*  
- Sondagem linear *(linear probing)*  
- Hash duplo *(double hashing)*  

### Conjuntos de Dados
- Tamanhos: `100 000`, `1 000 000` e `10 000 000` registros  
- Cada registro é um número inteiro de 9 dígitos (simulando IDs).  
- Seed fixa (`42`) para reprodutibilidade total.

### Métricas Medidas
- Tempo de inserção e busca (ms)  
- Número de colisões  
- Top-3 maiores listas encadeadas  
- Gap médio, máximo e mínimo  
- *(Bônus)* Uso de memória

---


## 5. Análise e Geração de Gráficos

Após rodar os experimentos, execute:

* python analyze_metrics_all.py


Isso gera:

* results/summary/metrics_unificado.csv (dados agregados)

* results/summary/*.csv (métricas por combinação)

* results/summary/*.png (gráficos)

---
## 6. Resultados
## 6.1 Tempo de Inserção
Figura 1 — Tempo de inserção para encadeamento separado, n=100000.

![img.png](img.png)

Figura 2 — Tempo de inserção para sondagem linear, n=1000000.

![img.png](img_2.png)

Figura 3 — Tempo de inserção para hash duplo, n=1000000.

![img.png](img_3.png)


## 6.2 Tempo de Busca
Figura 4 — Tempo de busca para encadeamento, n=1000000.

![img.png](img_4.png)

Figura 5 — Tempo de busca para hash duplo, n=1000000.

![img.png](img_5.png)


## 6.3 Colisões
Figura 6 — Número de colisões por função hash, sondagem linear, n=1000000.

![img.png](img6.png)


### 6.4 Top-3 Listas Encadeadas

| m | n | hash | Top1 | Top2 | Top3 |
|---|---|---|---|---|---|
| 200 003 | 100 000 | mod | 8 | 6 | 6 |
| 200 003 | 100 000 | mul | 6 | 6 | 6 |
| 200 003 | 100 000 | mix | 5 | 5 | 5 |
| 200 003 | 1 000 000 | mod | 19 | 19 | 17 |
| 200 003 | 1 000 000 | mul | 16 | 16 | 16 |
| 200 003 | 1 000 000 | mix | 17 | 17 | 17 |
| 200 003 | 10 000 000 | mod | 84 | 83 | 83 |
| 200 003 | 10 000 000 | mul | 85 | 84 | 83 |
| 200 003 | 10 000 000 | mix | 89 | 87 | 84 |

*Tabela 1 — Top-3 listas encadeadas para m=200 003 (valores médios).*

---

### 6.5 Gaps

| m | n | hash | média | máx | mín |
|---|---|---|---|---|---|
| 200 003 | 100 000 | mod | 1.55 | 22 | 0 |
| 200 003 | 1 000 000 | mod | 0.0068 | 2 | 0 |
| 2 000 003 | 100 000 | mod | 19.51 | 238 | 0 |
| 2 000 003 | 1 000 000 | mod | 1.54 | 26 | 0 |
| 20 000 027 | 100 000 | mod | 199.46 | 2469 | 0 |
| 20 000 027 | 1 000 000 | mod | 19.51 | 284 | 0 |
| 20 000 027 | 10 000 000 | mod | 1.55 | 31 | 0 |

*Tabela 2 — Estatísticas de gaps para encadeamento e função mod.*

## 6.6 Uso de Memória (Bônus)

![img.png](img_7.png)

Figura 7 — Consumo de memória durante inserção para encadeamento.

---
## 7. Discussão

Encadeamento separado foi mais robusto para altas cargas (n ≫ m), mantendo tempos proporcionais ao tamanho das listas encadeadas.

Sondagem linear degradou significativamente à medida que o fator de carga se aproximou de 1.

Hash duplo manteve tempos mais estáveis em cargas médias-altas.

A função mix apresentou melhor dispersão que mod e mul em cenários pequenos, com menos colisões.

Tabelas grandes e n pequeno resultaram em gaps enormes, mas com impacto pequeno no encadeamento.

---

## 8. Conclusão

O projeto confirmou os comportamentos clássicos das estratégias de hashing:

Encadeamento é versátil e eficiente mesmo quando n ≫ m.

Hash duplo combinado com funções de dispersão adequadas oferece melhor performance em endereçamento aberto.

Funções modulares simples são significativamente inferiores.

O uso de números primos para m e seed fixa foi essencial para resultados estáveis.
