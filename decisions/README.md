# Registro de Decisões de Arquitetura e Design (ADRs)

Este diretório documenta a evolução técnica, padrões arquiteturais e decisões de design tomadas no desenvolvimento do encurtador de URLs.

## Índice de Decisões

| Versão | Título | Status | Data |
| :--- | :--- | :--- | :--- |
| [**v1**](./v1-arquitetura-em-camadas-e-ddd-modular.md) | Arquitetura em Camadas e Isolamento do Domínio | **Aceito** | 2026-09-20 |
| [**v2**](./v2-algoritmo-de-codificacao-base62.md) | Codificação de IDs com Base62 | **Aceito** | 2026-09-20 |
| [**v3**](./v3-persistencia-relacional-e-integridade.md) | Persistência Relacional e Mapeamento de Entidades | **Aceito** | 2026-09-20 |
| [**v4**](./v4-geracao-distribuida-de-ids-tsid-pre-persistencia.md) | Geração de ID Distribuído Pré-Persistência (TSID) e Eliminação do Duplo Round-Trip | **Aceito** | 2026-09-20 |

---

## Estrutura dos Documentos

Cada arquivo de decisão segue o formato padrão:
1. **Contexto**: O problema ou motivação inicial.
2. **Decisão**: A abordagem técnica escolhida.
3. **Alternativas Consideradas**: Outras opções avaliadas e os motivos de descarte.
4. **Consequências e Trade-offs**: Benefícios e impactos no projeto.
5. **Status**: Situação atual da decisão (`Proposto`, `Aceito`, `Deprecado` ou `Substituído`).
