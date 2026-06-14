PROJECT RULES:
Não criar nada específico para vampiros que não possa ser reutilizado por lobisomens e bruxaria depois.
Por exemplo, não crie um atributo blood diretamente no jogador. Crie um sistema genérico de recursos sobrenaturais e depois implemente o sangue como um caso específico.
Lobisomens usarão RaceResource(ResourceType.RAGE, ...) e bruxaria usará RaceResource(ResourceType.MANA, ...).
Sem Mixins — apenas eventos e APIs oficiais do NeoForge.

---

PROJECT CONTEXT:
Mod: Mystic Realm (modid: mysticrealm)
Package raiz: com.nashgoldd.mysticrealm

Stack:
- Minecraft 26.1.2 (MC 26.x)
- NeoForge 26.1.2.70-beta
- Java 25
- ModDevGradle
- GeckoLib 5.5.1 (com.geckolib:geckolib-neoforge-26.1.2:5.5.1)

---

DOCUMENTAÇÃO DE REFERÊNCIA (docs/):
- docs/fases-concluidas.md — histórico detalhado de tudo que já foi implementado (Fase 1 → 4f)
  (consultar antes de alterar/estender uma feature de uma fase anterior)
- docs/estrutura-pacotes.md — árvore completa de pacotes Java + convenções obrigatórias de assets
  (items/, models/item/, mob_effect/, particles/, geckolib/models/, etc.)
  (consultar ao criar novos items, efeitos, partículas, blocos, entidades ou ao navegar a estrutura do projeto)
- docs/api-reference-mc26-neoforge.md — APIs específicas do MC 26.x / NeoForge 26.x
  (Identifier, attachments, rede, dano, screens, renderização de entidades, partículas, mob effects, config screen, buses)
  (consultar ao implementar ou depurar código que usa essas APIs)
- docs/geckolib-reference.md — GeckoLib 5.5.1 (dependência, packages, AnimationController, GeoModel, GeoBlockRenderer)
  (consultar ao trabalhar com modelos/animações GeckoLib)

ESTRUTURA — VISÃO RÁPIDA:
- supernatural/race, supernatural/resource (RaceResource genérico), supernatural/transformation
  (IPendingTransformation), supernatural/channeling (ChannelAction/State/Service) — tudo GENÉRICO e reutilizável
- supernatural/vampire/ — implementação vampírica (attachment, balance, block, client, entity, event, feeding,
  effect, item, network, progression, registry, service)
- Ver docs/estrutura-pacotes.md para a árvore completa de arquivos.

---

PROBLEMAS CONHECIDOS DO IDE (VS CODE):
- Erros "net.minecraft cannot be resolved" são de indexação — NÃO são erros reais
- Erros de tipos GeckoLib (GeoBlockEntity, RawAnimation, etc.) também podem ser falsos positivos de indexação
- Fix: rodar gradlew eclipse
- Verificar erros reais SEMPRE com: .\gradlew.bat build

---

PRÓXIMAS FASES PLANEJADAS:
- Fase 4g: Poderes por Rank (RankBenefitsRegistry — ganchos já existem via VampireRankChangedEvent)
- Fase 5: Lobisomens (RaceResource RAGE, transformação lunar, habilidades físicas)
  - Reutilizar IPendingTransformation para WerewolfCurseEffect (mesmo fluxo de morte/transformação)
  - Reutilizar ChannelAction para habilidades ativas do lobisomem
- Fase 6: Bruxaria (RaceResource MANA, feitiços, crafting sobrenatural)
  - Reutilizar ChannelAction para lançamento de feitiços
