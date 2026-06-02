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

---

FASES CONCLUÍDAS:

Fase 1 — Infraestrutura Base:
- Sistema de Raças (RaceType enum + IRace)
- Sistema de Progressão (level + experience em PlayerSupernaturalData)
- Data Attachments com MapCodec e copyOnDeath
- Sincronização cliente-servidor (SyncPlayerDataPacket)
- Eventos customizados (RaceChangedEvent, LevelChangedEvent, ExperienceChangedEvent)
- Sistema de Configuração (MysticConfig → mysticrealm-common.toml)
- Comandos de Debug (/mystic info, /mystic race set, /mystic level set)

Fase 2 — Sistema de Vampirismo:
- RaceResource genérico (ResourceType.BLOOD/RAGE/MANA + RaceResource com MapCodec)
- VampireData attachment (blood como RaceResource, transformed, sunlightBurning, nearDeath)
- VampireService (transform, cure, isVampire, getData)
- Dano solar escalonado por nível (level 1 = morte instantânea, level max = 10s de sobrevivência)
- Efeitos passivos por nível de sangue (Night Vision sempre, Regeneration ≥75%, Speed ≥50%)
- Penalidades por fome (Weakness/Slowness I e II conforme sangue baixa)
- Sistema de imortalidade (cancela morte exceto por fraquezas sobrenaturais)
- VampireWeaknessRegistry (isLethalToVampire: luz solar + Wooden Stake)
- Eventos vampíricos (VampireTransformEvent, VampireCuredEvent, BloodLevelChangedEvent, VampireNearDeathEvent)
- SyncVampireDataPacket (bloodLevel, maxBlood, transformed, sunlightBurning, nearDeath)
- Items: WoodenStakeItem + BloodVialItem (+25 sangue ao consumir)
- VampireHudOverlay (Blood: X/100, mensagem near-death, aviso de luz solar)
- Comandos: /mystic vampire transform|cure, /mystic blood set|add|info
- Config [vampire]: 10 valores configuráveis incluindo sunlightMaxSurvivalSeconds

---

ESTRUTURA DE PACOTES:

src/main/java/com/nashgoldd/mysticrealm/
├── MysticRealm.java                         ← @Mod principal
├── attachment/PlayerSupernaturalData.java   ← race + level + xp
├── command/MysticCommands.java
├── config/MysticConfig.java
├── event/
│   ├── RaceChangedEvent.java
│   ├── LevelChangedEvent.java
│   └── ExperienceChangedEvent.java
├── event/handler/PlayerEventHandler.java
├── network/
│   ├── MysticNetwork.java
│   ├── MysticDamageTypes.java
│   ├── SyncPlayerDataPacket.java
│   └── ClientPacketHandlers.java
├── registry/
│   ├── MysticAttachments.java              ← SUPERNATURAL_DATA + VAMPIRE_DATA
│   └── MysticItems.java                    ← DeferredRegister.Items (createItems)
├── supernatural/
│   ├── race/RaceType.java
│   └── resource/
│       ├── ResourceType.java               ← BLOOD, RAGE, MANA
│       └── RaceResource.java               ← genérico com MapCodec
└── supernatural/vampire/
    ├── VampireWeaknessType.java
    ├── attachment/VampireData.java
    ├── client/VampireHudOverlay.java
    ├── event/BloodLevelChangedEvent.java
    ├── event/VampireCuredEvent.java
    ├── event/VampireNearDeathEvent.java
    ├── event/VampireTransformEvent.java
    ├── event/handler/VampireEventHandler.java
    ├── item/BloodVialItem.java
    ├── item/WoodenStakeItem.java
    ├── network/SyncVampireDataPacket.java
    ├── registry/VampireWeaknessRegistry.java
    └── service/VampireService.java

---

APIs CRÍTICAS DO MC 26.x / NEOFORGE 26.x:

IDENTIFICADORES:
- Identifier.fromNamespaceAndPath(ns, path)  ← não ResourceLocation

ATTACHMENTS:
- AttachmentType.builder(Supplier).serialize(MapCodec).copyOnDeath().build()
- MapCodec<T> obrigatório (não Codec) via RecordCodecBuilder.mapCodec()
- player.getData(MysticAttachments.SUPERNATURAL_DATA)  ← Supplier<AttachmentType<T>>

REDE:
- RegisterPayloadHandlersEvent → event.registrar("1.0").playToClient(TYPE, STREAM_CODEC, handler)
- PacketDistributor.sendToPlayer(player, payload)
- Handlers no cliente: ctx.enqueueWork(() -> { ... })

PERMISSÕES:
- src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)  ← não hasPermission(2)

DANO:
- player.hurtServer(ServerLevel, DamageSource, float)  ← não hurt()
- DamageSource customizado: level.damageSources().source(ResourceKey<DamageType>)
- source.is(ResourceKey<DamageType>) para verificar tipo
- Aplica dano uma vez por segundo (tickCount % 20 == 0) para evitar invulnerability frames

EFEITOS:
- MobEffects.SPEED, SLOWNESS, WEAKNESS, NIGHT_VISION, REGENERATION são Holder<MobEffect>
- addEffect(), removeEffect(), hasEffect(), getEffect() — todos aceitam Holder<MobEffect>

VERIFICAÇÕES DE MUNDO:
- level.isBrightOutside()  ← substitui isNight() (MC 26.x)
- level.dimensionType().hasSkyLight() para verificar dimensão com céu

RENDERIZAÇÃO (HUD):
- GuiGraphicsExtractor (não GuiGraphics — renomeado no MC 26.x)
- Método: g.text(Font, String, int x, int y, int color, boolean dropShadow)
- Cor em formato ARGB obrigatório: 0xFFAA0000 (não 0xAA0000 — alpha 0 = invisível)
- event.getGuiGraphics() retorna GuiGraphicsExtractor

ITENS:
- DeferredRegister.createItems(MODID)  ← não DeferredRegister.create(Registries.ITEM, ...)
- ITEMS.registerItem("id", MyItem::new, p -> p.stacksTo(1))  ← terceiro arg é UnaryOperator<Properties>

EVENTOS CLIENT-ONLY:
- @EventBusSubscriber(modid = MODID, value = Dist.CLIENT) para classes cliente
- RenderGuiEvent.Post para HUD overlays

---

PROBLEMAS CONHECIDOS DO IDE (VS CODE):
- Erros "net.minecraft cannot be resolved" são de indexação — NÃO são erros reais
- Fix: rodar gradlew eclipse
- Verificar erros reais SEMPRE com: .\gradlew.bat build

---

PRÓXIMAS FASES PLANEJADAS:
- Fase 3: Lobisomens (RaceResource RAGE, transformação lunar, habilidades físicas)
- Fase 4: Bruxaria (RaceResource MANA, feitiços, crafting sobrenatural)
