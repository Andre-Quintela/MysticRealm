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
- Dano solar escalonado por nível (level 1 = morte instantânea, level max = sobrevivência configurável)
- Efeitos passivos por nível de sangue (Night Vision sempre, Regeneration ≥75%, Speed ≥50%)
- Penalidades por fome (Weakness/Slowness I e II conforme sangue baixa)
- Sistema de imortalidade (cancela morte exceto por fraquezas sobrenaturais)
- VampireWeaknessRegistry (isLethalToVampire: luz solar + Wooden Stake)
- Eventos vampíricos (VampireTransformEvent, VampireCuredEvent, BloodLevelChangedEvent, VampireNearDeathEvent)
- SyncVampireDataPacket (transformed, sunlightBurning, nearDeath)
- Items: WoodenStakeItem + BloodVialItem (+25 sangue ao consumir)
- VampireHudOverlay (ícones de gota de sangue, mensagem near-death, aviso de luz solar)
- Comandos: /mystic vampire transform|cure, /mystic blood set|add|info
- Config [vampire]: 10 valores configuráveis incluindo sunlightMaxSurvivalSeconds

Fase 3 — Sistema de Alimentação Vampírica (Drenagem de Sangue):
- ChannelAction (interface genérica reutilizável — lobisomens/bruxaria futuramente)
- ChannelState (estado transiente: action, targetEntityId, ticksElapsed)
- ChannelService (Map<UUID,ChannelState> + Map<UUID,Map<String,Integer>> cooldowns)
  - start() / cancel() / interrupt(reason) / tick() / getActive() / getCooldown() / clearOnDisconnect()
  - tick() valida: alvo vivo, distância ≤2.5, dot product ≥0.85 (vampiro olhando para alvo)
- BloodDrainAction (implements ChannelAction — singleton INSTANCE)
  - 60 ticks (3s) de duração, 100 ticks (5s) de cooldown
  - onTick(): a cada 5 ticks — partículas HEART no pescoço + SoundEvents.WITCH_DRINK
  - onComplete(): +4 food units vampiro, 2 dano + Weakness I (200t) vítima,
    aldeões ganham Slowness I + som de sofrimento
  - Dispara BloodDrainStartEvent / BloodDrainCompleteEvent / BloodDrainCancelEvent / BloodDrainInterruptedEvent
- DrainableEntityRegistry (instanceof Animal para animais, EntityType para aldeões, Player com guards)
  - Inválidos: vampiros, criativos, espectadores, golems, armor stands
- 3 pacotes de rede:
  - RequestBloodDrainPacket (C→S, int entityId)
  - CancelBloodDrainPacket (C→S, sem campos)
  - SyncDrainStatePacket (S→C, boolean draining, int ticksElapsed, totalTicks, cooldownTicks)
- ServerPacketHandlers (handleRequestDrain + handleCancelDrain)
- ClientDrainState (campos estáticos: isDraining, ticksElapsed, totalTicks, cooldownTicks)
- VampireKeyBindings: tecla V — categoria "Mystic Realm", KeyMapping.Category(Identifier)
  - Registrado via modEventBus.addListener() em MysticRealmClient
- VampireClientInputHandler: ClientTickEvent.Post — detecta crosshairPickEntity, envia pacotes
- VampireHudOverlay: barra de progresso vermelha na base da tela durante drenagem ativa
- VampireEventHandler: ChannelService.tick() + sync a cada 5t + LivingDamageEvent.Pre interrupt + PlayerLoggedOutEvent cleanup

Config [geral]:
- maxLevel: padrão 10 (válido para todas as raças)

---

ESTRUTURA DE PACOTES:

src/main/java/com/nashgoldd/mysticrealm/
├── MysticRealm.java
├── MysticRealmClient.java                      ← @Mod(dist=CLIENT) + registro de keybinds
├── attachment/PlayerSupernaturalData.java      ← race + level + xp
├── command/MysticCommands.java
├── config/MysticConfig.java
├── event/
│   ├── RaceChangedEvent.java
│   ├── LevelChangedEvent.java
│   └── ExperienceChangedEvent.java
├── event/handler/PlayerEventHandler.java
├── network/
│   ├── MysticNetwork.java                      ← playToClient + playToServer + syncDrainToClient()
│   ├── MysticDamageTypes.java
│   ├── SyncPlayerDataPacket.java
│   ├── ClientPacketHandlers.java               ← handleSyncVampireData + handleSyncDrainState
│   └── ServerPacketHandlers.java               ← handleRequestDrain + handleCancelDrain
├── registry/
│   ├── MysticAttachments.java                  ← SUPERNATURAL_DATA + VAMPIRE_DATA
│   └── MysticItems.java
├── supernatural/
│   ├── race/RaceType.java
│   ├── resource/
│   │   ├── ResourceType.java                   ← BLOOD, RAGE, MANA
│   │   └── RaceResource.java
│   └── channeling/                             ← GENÉRICO (reutilizável)
│       ├── ChannelAction.java
│       ├── ChannelState.java
│       └── ChannelService.java
└── supernatural/vampire/
    ├── VampireWeaknessType.java
    ├── attachment/VampireData.java
    ├── client/
    │   ├── VampireHudOverlay.java
    │   ├── VampireKeyBindings.java              ← KeyMapping.Category(Identifier) + register()
    │   ├── VampireClientInputHandler.java       ← ClientTickEvent, ClientPacketDistributor
    │   └── ClientDrainState.java               ← campos estáticos de estado cliente
    ├── event/
    │   ├── BloodLevelChangedEvent.java
    │   ├── BloodDrainStartEvent.java
    │   ├── BloodDrainCompleteEvent.java
    │   ├── BloodDrainCancelEvent.java
    │   ├── BloodDrainInterruptedEvent.java      ← campo extra: String reason
    │   ├── VampireCuredEvent.java
    │   ├── VampireNearDeathEvent.java
    │   └── VampireTransformEvent.java
    ├── event/handler/VampireEventHandler.java
    ├── feeding/
    │   ├── BloodDrainAction.java               ← implements ChannelAction (singleton)
    │   └── DrainableEntityRegistry.java
    ├── item/
    │   ├── BloodVialItem.java
    │   └── WoodenStakeItem.java
    ├── network/
    │   ├── SyncVampireDataPacket.java
    │   ├── RequestBloodDrainPacket.java
    │   ├── CancelBloodDrainPacket.java
    │   └── SyncDrainStatePacket.java
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
- RegisterPayloadHandlersEvent → event.registrar("1.0").playToClient/playToServer(TYPE, STREAM_CODEC, handler)
- PacketDistributor.sendToPlayer(player, payload)          ← servidor → cliente
- ClientPacketDistributor.sendToServer(payload)            ← cliente → servidor
  (net.neoforged.neoforge.client.network.ClientPacketDistributor)
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
- ServerLevel via cast: ((ServerLevel) player.level()) — NÃO player.serverLevel() (não existe)

RENDERIZAÇÃO (HUD):
- GuiGraphicsExtractor (não GuiGraphics — renomeado no MC 26.x)
- Método: g.text(Font, String, int x, int y, int color, boolean dropShadow)
- Cor em formato ARGB obrigatório: 0xFFAA0000 (não 0xAA0000 — alpha 0 = invisível)
- event.getGuiGraphics() retorna GuiGraphicsExtractor
- Sprites GUI: blitSprite(RenderPipelines.GUI_TEXTURED, spriteId, x, y, w, h)
  - spriteId via Identifier.fromNamespaceAndPath(MODID, "nome") — sem prefixo de pasta
  - Arquivos em textures/gui/sprites/nome.png

ITENS:
- DeferredRegister.createItems(MODID)
- ITEMS.registerItem("id", MyItem::new, p -> p.stacksTo(1))

SONS:
- SoundEvents com tipo SoundEvent: usar diretamente em playSound()
- SoundEvents com tipo Holder.Reference<SoundEvent>: usar .value() ou trocar por SoundEvent puro
- Ex: SoundEvents.WITCH_DRINK (SoundEvent) ✓ | SoundEvents.HONEY_DRINK (Holder) — precisa .value()

KEYBINDS:
- KeyMapping.Category é um record: new KeyMapping.Category(Identifier)
- Construtor NeoForge: new KeyMapping(String, IKeyConflictContext, InputConstants.Type, int, KeyMapping.Category)
- Registrar categoria: event.registerCategory(category) via RegisterKeyMappingsEvent
- Chave de tradução da categoria: id.toLanguageKey("key.category") → "key.category.{namespace}.{path}"
  ex: Identifier("mysticrealm","mysticrealm") → "key.category.mysticrealm.mysticrealm" (sem 's' em category)
- RegisterKeyMappingsEvent é MOD bus — registrar via modEventBus.addListener() no @Mod(dist=CLIENT)
- @Mod(dist=CLIENT) pode receber IEventBus no construtor igual ao @Mod principal

EVENTOS CLIENT-ONLY:
- @EventBusSubscriber(modid = MODID, value = Dist.CLIENT) para NeoForge bus (jogo)
- @Mod(dist=CLIENT) + modEventBus.addListener() para MOD bus (lifecycle/registro)
- RenderGuiEvent.Post para HUD overlays
- ClientTickEvent.Post para tick de input no cliente

---

PROBLEMAS CONHECIDOS DO IDE (VS CODE):
- Erros "net.minecraft cannot be resolved" são de indexação — NÃO são erros reais
- Fix: rodar gradlew eclipse
- Verificar erros reais SEMPRE com: .\gradlew.bat build

---

PRÓXIMAS FASES PLANEJADAS:
- Fase 4: Lobisomens (RaceResource RAGE, transformação lunar, habilidades físicas)
  - Reutilizar ChannelAction para habilidades ativas do lobisomem
- Fase 5: Bruxaria (RaceResource MANA, feitiços, crafting sobrenatural)
  - Reutilizar ChannelAction para lançamento de feitiços
