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
- Data Attachments com MapCodec e copyOnDeath
- Sincronização cliente-servidor (SyncPlayerDataPacket — apenas race)
- Eventos customizados (RaceChangedEvent)
- Sistema de Configuração (MysticConfig → mysticrealm-common.toml)
  - Todas as entradas usam .translation("mysticrealm.configuration.{seção}.{chave}") explícito
  - Traduções em assets/mysticrealm/lang/en_us.json (com tooltips .tooltip)
- Comandos de Debug (/mystic info, /mystic race set)

NOTA: O sistema de level/XP numérico foi removido. Progressão vampírica é feita via VampireRank (Fase 4a).
LevelChangedEvent e ExperienceChangedEvent foram deletados.

Fase 2 — Sistema de Vampirismo:
- RaceResource genérico (ResourceType.BLOOD/RAGE/MANA + RaceResource com MapCodec)
- VampireData attachment (blood como RaceResource, transformed, sunlightBurning, nearDeath)
- VampireService (transform, cure, isVampire, getData)
- Dano solar escalonado por VampireRank (NEWBORN = morte instantânea, BLOOD_SOVEREIGN = máximo configurável)
- Efeitos passivos por nível de sangue (Night Vision sempre, Regeneration ≥75%, Speed ≥50%)
- Penalidades por fome (Weakness/Slowness I e II conforme sangue baixa)
- Sistema de imortalidade (cancela morte exceto por fraquezas sobrenaturais)
- VampireWeaknessRegistry (isLethalToVampire: luz solar + Wooden Stake)
- Eventos vampíricos (VampireTransformEvent, VampireCuredEvent, BloodLevelChangedEvent, VampireNearDeathEvent)
- SyncVampireDataPacket (transformed, sunlightBurning, nearDeath)
- Items: WoodenStakeItem + BloodVialItem (+25 sangue ao consumir)
- VampireHudOverlay (ícones de gota de sangue, mensagem near-death, aviso de luz solar)
- Comandos: /mystic vampire transform|cure, /mystic blood set|add|info
- Config [vampire]: valores configuráveis incluindo sunlightMaxSurvivalSeconds

Fase 3 — Sistema de Alimentação Vampírica (Drenagem de Sangue):
- ChannelAction (interface genérica reutilizável — lobisomens/bruxaria futuramente)
- ChannelState (estado transiente: action, targetEntityId, ticksElapsed)
- ChannelService (Map<UUID,ChannelState> + Map<UUID,Map<String,Integer>> cooldowns)
  - start() / cancel() / interrupt(reason) / tick() / getActive() / getCooldown() / clearOnDisconnect()
  - tick() valida: alvo vivo, distância ≤2.5, dot product ≥0.85 (vampiro olhando para alvo)
- BloodDrainAction (implements ChannelAction — singleton INSTANCE)
  - 60 ticks (3s) de duração, 100 ticks (5s) de cooldown
  - onTick(): a cada 5 ticks — partículas HEART no pescoço + SoundEvents.WITCH_DRINK
  - onComplete(): +4 food units vampiro, Weakness I (200t) vítima,
    aldeões ganham Slowness I + som de sofrimento
  - bloodAccumulator (Map<UUID,Float>): food fracionário acumulado por vampiro
  - essenceAccumulator (Map<UUID,Double>): essência fracionária acumulada — evita arredondamento para zero
  - Dispara BloodDrainStartEvent / BloodDrainCompleteEvent / BloodDrainCancelEvent / BloodDrainInterruptedEvent
- DrainableEntityRegistry:
  - instanceof Animal para animais passivos
  - VALID_HUMANOID_TYPES (EntityType Set): VILLAGER, WANDERING_TRADER, PILLAGER, VINDICATOR, EVOKER, ILLUSIONER, WITCH
  - Player com guards (não vampiros, criativos, espectadores)
- Pool de sangue por entidade (EntityBloodData attachment):
  - Inicializado na primeira drenagem com maxBlood = maxHealth
  - Regenera via EntityTickEvent (fração configurável por intervalo configurável)
  - Eventos: BloodPoolChangedEvent, BloodRegeneratedEvent, EntityExsanguinatedEvent
- 3 pacotes de rede:
  - RequestBloodDrainPacket (C→S, int entityId)
  - CancelBloodDrainPacket (C→S, sem campos)
  - SyncDrainStatePacket (S→C, draining, ticksElapsed, totalTicks, cooldownTicks, targetBloodCurrent, targetBloodMax)
- ServerPacketHandlers (handleRequestDrain + handleCancelDrain)
- ClientDrainState (campos estáticos: isDraining, ticksElapsed, totalTicks, cooldownTicks, targetBloodCurrent, targetBloodMax)
- VampireKeyBindings: tecla V — categoria "Mystic Realm", KeyMapping.Category(Identifier)
  - Registrado via modEventBus.addListener() em MysticRealmClient
- VampireClientInputHandler: ClientTickEvent.Post — detecta crosshairPickEntity, envia pacotes
- VampireHudOverlay: barra de progresso vermelha + barra de sangue da entidade alvo
- VampireEventHandler: ChannelService.tick() + sync a cada 5t + LivingDamageEvent.Pre interrupt + PlayerLoggedOutEvent cleanup

Fase 4a — Sistema de Progressão Vampírica:
- VampireRank enum (7 estágios): NEWBORN → NEOPHYTE → VAMPIRE → ELDER → VAMPIRE_LORD → PRINCE_OF_NIGHT → BLOOD_SOVEREIGN
  - next() retorna Optional<VampireRank>, isMax(), displayName()
- VampireData expandido (+4 campos com optionalFieldOf para retrocompatibilidade):
  - VampireRank rank (padrão NEWBORN)
  - long bloodEssence (permanente, não consumida)
  - long vampireAgeTicks (ticks vividos como vampiro)
  - int ascensionCount
  - Setters raw para cliente (setRankRaw, setBloodEssenceRaw, etc.)
- BloodEssenceRegistry (Map<EntityType<?>, Long>):
  - Chicken=1, Cod/Salmon/Rabbit=1, Pig/Sheep/Goat=2, Cow/Mooshroom/Donkey/Mule/Llama=3,
    Horse/Camel=4, Villager=10, WanderingTrader/Evoker=12, Witch=9, Pillager/Vindicator=8, Illusioner=11, Player=15
  - getProportionalEssence usa essenceAccumulator fracionário em BloodDrainAction (evita arredondamento)
- VampireProgressionService:
  - canAscend() / ascend() / grantEssence(player, amount, source) / tickAge(player)
  - getRequiredEssence(rank) / getRequiredAgeHours(rank) — lidos de MysticConfig
  - MILESTONE_HOURS = {1, 5, 15, 50, 100, 250} → VampireAgeMilestoneEvent
- 4 novos eventos: BloodEssenceGainedEvent, VampireRankChangedEvent, VampireAscensionEvent, VampireAgeMilestoneEvent
- SyncVampireProgressionPacket (S→C): rank, bloodEssence, vampireAgeTicks, ascensionCount
  - Enviado no login, respawn, mudança de dimensão e a cada grantEssence
- Comando: /mystic vampire ascend (valida essência + idade, mensagens de erro específicas)
- VampireHudOverlay expandido: bloco rank/essência/idade no canto inferior esquerdo (y=screenH-70)
  - formatAge(ticks) → "Xh Ym" | formatLong(value) → "250,000" ou "1M"
- Config [vampire.progression]: enableVampireProgression, trackVampireAge,
  6×essence (NEWBORN→NEOPHYTE=100 ... PRINCE→SOVEREIGN=250000),
  6×ageHours (1h ... 250h)
- Dano solar agora escala por rank.ordinal() em vez de level numérico:
  - NEWBORN (ordinal=0) = morte instantânea
  - BLOOD_SOVEREIGN (ordinal=6) = sunlightMaxSurvivalSeconds configurável

Fase 4b — Sistema de Infecção Vampírica (VampireBloodVial):
- IPendingTransformation (interface genérica em supernatural/transformation/)
  - applyTransformation(ServerPlayer, DamageSource) + onExpire(ServerPlayer)
  - Qualquer MobEffect que implemente essa interface é interceptado no LivingDeathEvent
  - Reutilizável futuramente para WerewolfCurseEffect, DemonicCorruptionEffect, etc.
- MysticEffects (DeferredRegister<MobEffect>, registry/MysticEffects.java)
  - Registrar em MysticRealm.java via MysticEffects.register(modEventBus)
- VampireInfectionEffect (extends MobEffect implements IPendingTransformation)
  - supernatural/vampire/effect/VampireInfectionEffect.java
  - Categoria HARMFUL, cor 0x8B0000
  - applyTransformation(): dispara PlayerVampireTransformationEvent (cancellable via ICancellableEvent),
    chama VampireService.transform(), remove efeito, seta saúde mínima, exibe mensagem, sincroniza tudo
  - onExpire(): dispara VampireInfectionExpireEvent
- VampireBloodVialItem (supernatural/vampire/item/VampireBloodVialItem.java)
  - Consumível com ItemUseAnimation.DRINK, 32 ticks
  - Bloqueado para vampiros e para quem já tem o efeito ativo
  - Aplica VampireInfectionEffect com duração de MysticConfig.INFECTION_DURATION_SECONDS * 20 ticks
- 3 novos eventos: VampireInfectionStartEvent, VampireInfectionExpireEvent, PlayerVampireTransformationEvent
- VampireEventHandler expandido:
  - onLivingDeath(): verifica IPendingTransformation ANTES da lógica vampírica (jogador ainda não é vampiro)
    Cancela morte (funciona em Hardcore), chama applyTransformation
  - tickInfection(): aviso "fading" quando duration ≤ 600 ticks (30s), detecta expiração natural via Set<UUID>
  - onPlayerLogout(): limpa o Set<UUID> infectedPlayers
- Config [vampire.infection]: infectionDurationSeconds(180), enableHardcoreTransformation(true)
- Textura do item: textures/item/vampire_blood_vial.png (16x16)
- Ícone do efeito: textures/mob_effect/vampire_infection.png (18x18 — obrigatório)

Fase 4c — Mob Hostil: HostileVampireEntity:
- HostileVampireEntity (extends Monster): predador noturno raro do Overworld
  - Atributos: 50HP, 5 dano, velocidade 0.32, follow range 40
  - Goals: Float(0), MeleeAttack(2), WaterAvoidingRandomStroll(5), LookAtPlayer(6), RandomLookAround(7)
  - Targets: HurtByTarget(1), NearestAttackableTarget<Player>(2)
  - Spawn: noturno (!isBrightOutside()), controlado por MysticConfig.ENABLE_VAMPIRE_SPAWN
  - Queima solar via tag data/minecraft/tags/entity_type/burn_in_daylight.json (não código Java)
  - Animações: idle (asas), walk (braços/pernas), meleeAttack — via AnimationState + KeyframeAnimation
  - doHurtTarget() → DATA_IS_ATTACKING synched, attackAnimationTimeout = 20 ticks
- Rendering (MC 26.x):
  - HostileVampireRenderState extends LivingEntityRenderState (3 AnimationState)
  - VampireEntityModel extends EntityModel<HostileVampireRenderState> (KeyframeAnimation baked)
  - VampireEntityRenderer extends MobRenderer<Entity, RenderState, Model> (3 type params)
    - createRenderState() + extractRenderState() copiando AnimationStates via .copyFrom()
    - getTextureLocation(RenderState) retorna Identifier (não ResourceLocation)
- MysticEntityTypes: DeferredRegister.Entities + registerEntityType() (não DeferredRegister<EntityType<?>>)
- MysticItems: SpawnEggItem + Item.Properties.spawnEgg(EntityType) (DeferredSpawnEggItem não existe)
- Config [vampire.spawn]: enableVampireSpawn, vampireSpawnWeight(5), min/maxGroup(1/2)
- Spawn natural: data/mysticrealm/neoforge/biome_modifier/hostile_vampire_spawns.json
  - type neoforge:add_spawns, biomas #minecraft:is_overworld, weight 5, minCount 1, maxCount 2
- Loot: data/mysticrealm/loot_table/entities/hostile_vampire.json
  - Blood Vial (w3), Rotten Flesh (w5), Empty (w2)
- RegisterSpawnPlacementsEvent → MOD bus (não NeoForge.EVENT_BUS)
- SpawnPlacementTypes.ON_GROUND (não SpawnPlacements.ON_GROUND)

---

ESTRUTURA DE PACOTES:

src/main/java/com/nashgoldd/mysticrealm/
├── MysticRealm.java
├── MysticRealmClient.java                      ← @Mod(dist=CLIENT) + registro de keybinds
├── attachment/PlayerSupernaturalData.java      ← apenas race (level/XP removidos)
├── command/MysticCommands.java
├── config/MysticConfig.java                   ← todas as entries com .translation() explícito
├── event/
│   └── RaceChangedEvent.java
├── event/handler/PlayerEventHandler.java
├── network/
│   ├── MysticNetwork.java                      ← playToClient + playToServer + syncDrainToClient() + syncVampireProgressionToClient()
│   ├── MysticDamageTypes.java
│   ├── SyncPlayerDataPacket.java               ← apenas race
│   ├── ClientPacketHandlers.java               ← handleSyncVampireData + handleSyncDrainState + handleSyncVampireProgression
│   └── ServerPacketHandlers.java               ← handleRequestDrain + handleCancelDrain
├── registry/
│   ├── MysticAttachments.java                  ← SUPERNATURAL_DATA + VAMPIRE_DATA + ENTITY_BLOOD
│   ├── MysticEffects.java                      ← DeferredRegister<MobEffect> + VAMPIRE_INFECTION
│   ├── MysticItems.java
│   └── MysticEntityTypes.java                  ← DeferredRegister.Entities + HOSTILE_VAMPIRE
├── supernatural/
│   ├── race/RaceType.java
│   ├── resource/
│   │   ├── ResourceType.java                   ← BLOOD, RAGE, MANA
│   │   └── RaceResource.java
│   ├── transformation/
│   │   └── IPendingTransformation.java         ← GENÉRICO: applyTransformation + onExpire
│   └── channeling/                             ← GENÉRICO (reutilizável)
│       ├── ChannelAction.java
│       ├── ChannelState.java
│       └── ChannelService.java
└── supernatural/vampire/
    ├── VampireWeaknessType.java
    ├── attachment/
    │   ├── VampireData.java                    ← +rank, bloodEssence, vampireAgeTicks, ascensionCount
    │   └── EntityBloodData.java                ← pool de sangue por entidade
    ├── balance/BloodBalance.java               ← constantes de balanço de drenagem
    ├── client/
    │   ├── VampireHudOverlay.java              ← ícones de sangue + rank/essência/idade + barra de alvo
    │   ├── VampireKeyBindings.java
    │   ├── VampireClientInputHandler.java
    │   ├── ClientDrainState.java
    │   ├── animation/VampireEntityAnimations.java  ← idle, walk, meleeAttack, rangedAttack
    │   ├── model/VampireEntityModel.java           ← EntityModel<RenderState> + KeyframeAnimation
    │   └── renderer/
    │       ├── HostileVampireRenderState.java       ← extends LivingEntityRenderState
    │       └── VampireEntityRenderer.java           ← MobRenderer 3 type params
    ├── entity/HostileVampireEntity.java        ← mob hostil, AnimationState, spawn rules
    ├── essence/BloodEssenceRegistry.java       ← essência base por EntityType
    ├── event/
    │   ├── BloodDrainStartEvent.java
    │   ├── BloodDrainTickEvent.java
    │   ├── BloodDrainCompleteEvent.java
    │   ├── BloodDrainCancelEvent.java
    │   ├── BloodDrainInterruptedEvent.java
    │   ├── BloodEssenceGainedEvent.java
    │   ├── BloodPoolChangedEvent.java
    │   ├── BloodRegeneratedEvent.java
    │   ├── EntityExsanguinatedEvent.java
    │   ├── VampireCuredEvent.java
    │   ├── VampireNearDeathEvent.java
    │   ├── VampireTransformEvent.java
    │   ├── VampireRankChangedEvent.java
    │   ├── VampireAscensionEvent.java
    │   ├── VampireAgeMilestoneEvent.java
    │   ├── VampireInfectionStartEvent.java     ← ao beber o VampireBloodVial
    │   ├── VampireInfectionExpireEvent.java    ← ao expirar sem morrer
    │   └── PlayerVampireTransformationEvent.java ← ao morrer com infecção (cancellable)
    ├── event/handler/VampireEventHandler.java
    ├── feeding/
    │   ├── BloodDrainAction.java               ← bloodAccumulator + essenceAccumulator (fracionários)
    │   └── DrainableEntityRegistry.java
    ├── effect/
    │   └── VampireInfectionEffect.java         ← MobEffect + IPendingTransformation
    ├── item/
    │   ├── BloodVialItem.java
    │   ├── VampireBloodVialItem.java           ← item de infecção (transforma ao morrer)
    │   └── WoodenStakeItem.java
    ├── network/
    │   ├── SyncVampireDataPacket.java
    │   ├── SyncVampireProgressionPacket.java   ← rank, bloodEssence, vampireAgeTicks, ascensionCount
    │   ├── RequestBloodDrainPacket.java
    │   ├── CancelBloodDrainPacket.java
    │   └── SyncDrainStatePacket.java
    ├── progression/
    │   ├── VampireRank.java                    ← enum 7 estágios
    │   └── VampireProgressionService.java
    ├── registry/VampireWeaknessRegistry.java
    └── service/VampireService.java

src/main/resources/assets/mysticrealm/lang/en_us.json
  ← traduções de items, keybinds, death messages E config screen (todas as chaves config)

src/main/resources/assets/mysticrealm/items/
  ← OBRIGATÓRIO no MC 26.1.2 — cada item precisa de um arquivo item_id.json aqui
  ← Formato: { "model": { "type": "minecraft:model", "model": "mysticrealm:item/item_id" } }
  ← SEM esse arquivo o item não renderiza (sem fallback para models/item/ nesta versão)
  ← Arquivos: blood_vial.json, wooden_stake.json, vampire_blood_vial.json, hostile_vampire_spawn_egg.json

src/main/resources/assets/mysticrealm/models/item/
  ← Define geometria/textura do item (ainda necessário, referenciado pelo arquivo em items/)
  ← Formato igual às versões anteriores: { "parent": "item/generated", "textures": { "layer0": "..." } }

src/main/resources/assets/mysticrealm/textures/mob_effect/
  ← Ícones de efeito de poção — DEVE ser 18x18 px (obrigatório, valores menores não funcionam)
  ← Nome do arquivo = nome do efeito no registro (ex: vampire_infection.png)

src/main/resources/data/minecraft/tags/entity_type/burn_in_daylight.json
  ← adiciona mysticrealm:hostile_vampire à tag vanilla (replace: false)

---

APIs CRÍTICAS DO MC 26.x / NEOFORGE 26.x:

IDENTIFICADORES:
- Identifier.fromNamespaceAndPath(ns, path)  ← não ResourceLocation
- A classe é net.minecraft.resources.Identifier (renomeada de ResourceLocation no mapping transformado)
- Métodos getTextureLocation(), ModelLayerLocation(), etc. também usam Identifier

ATTACHMENTS:
- AttachmentType.builder(Supplier).serialize(MapCodec).copyOnDeath().build()
- MapCodec<T> obrigatório (não Codec) via RecordCodecBuilder.mapCodec()
- player.getData(MysticAttachments.SUPERNATURAL_DATA)  ← Supplier<AttachmentType<T>>
- Se a classe tem múltiplos construtores, usar lambda em vez de method reference:
  AttachmentType.builder(() -> new MinhaClasse()) — evita ambiguidade entre Supplier e Function<IAttachmentHolder,T>

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
- level.isBrightOutside()  ← substitui isDay()/isNight() (MC 26.x)
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

RENDERIZAÇÃO DE ENTIDADES (MC 26.x — sistema completamente novo):
- EntityModel<T extends EntityRenderState> — model parametrizado por RenderState, NÃO pela entidade
  - HierarchicalModel NÃO EXISTE mais — usar EntityModel diretamente
  - renderToBuffer() é final em Model — NÃO sobrescrever
  - setupAnim(RenderState) — parâmetro único (não 6 floats + entidade)
  - resetPose() chamado automaticamente pelo super.setupAnim()
- MobRenderer<T extends Mob, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
  - 3 type parameters obrigatórios
  - getTextureLocation(RenderState) — parâmetro é RenderState, não Entity
  - createRenderState() → new MinhaRenderState()
  - extractRenderState(Entity, RenderState, float) → copiar AnimationStates via .copyFrom()
- RenderState pattern: criar classe extends LivingEntityRenderState com campos AnimationState
  - AnimationState é net.minecraft.world.entity.AnimationState (NÃO client package)
- Animações com KeyframeAnimation:
  - AnimationDefinition.bake(ModelPart root) → KeyframeAnimation (baked no construtor do model)
  - keyframeAnim.apply(AnimationState, float ageInTicks) — aplica a animação
  - AnimationDefinition.Builder.withLength().looping().addAnimation()...build() — API inalterada
- @OnlyIn(Dist.CLIENT) NÃO usar em classes — annotation não faz mais member-stripping em NeoForge 26.x

REGISTRO DE ENTITY TYPES:
- DeferredRegister.Entities (não DeferredRegister<EntityType<?>>)
  - DeferredRegister.createEntities(MODID)
  - ENTITY_TYPES.registerEntityType("id", Factory::new, MobCategory, builder -> builder.sized(...))
  - O ResourceKey é gerado automaticamente — não chamar .build() manualmente
- EntityAttributeCreationEvent → MOD bus (modEventBus.addListener)
- RegisterSpawnPlacementsEvent → MOD bus (modEventBus.addListener) — NÃO NeoForge.EVENT_BUS
- SpawnPlacementTypes.ON_GROUND (não SpawnPlacements.ON_GROUND)

SPAWN EGGS:
- DeferredSpawnEggItem NÃO EXISTE em NeoForge 26.x
- Usar vanilla: SpawnEggItem + Item.Properties.spawnEgg(EntityType)
  ex: ITEMS.registerItem("id", SpawnEggItem::new, p -> p.stacksTo(64).spawnEgg(MY_ENTITY.get()))

QUEIMA SOLAR DE MOBS:
- isSunBurnTick() é private em Mob — NÃO acessível de subclasses
- Adicionar à tag data/minecraft/tags/entity_type/burn_in_daylight.json com "replace": false
- Mob.aiStep() checa this.getType().is(EntityTypeTags.BURN_IN_DAYLIGHT) automaticamente
- burnUndead() (private em Mob) gerencia capacete e igniteForSeconds(8.0F)

ITENS:
- DeferredRegister.createItems(MODID)
- ITEMS.registerItem("id", MyItem::new, p -> p.stacksTo(1))
- ASSETS OBRIGATÓRIOS (MC 26.1.2):
  1. assets/mysticrealm/items/item_id.json        ← binding item → model (NOVO, obrigatório)
  2. assets/mysticrealm/models/item/item_id.json  ← geometria/textura (igual antes)
  3. assets/mysticrealm/textures/item/item_id.png ← textura 16x16
  - Sem o arquivo em items/, o item fica invisível/sem modelo no jogo

MOB EFFECTS (MobEffect customizado):
- DeferredRegister.create(Registries.MOB_EFFECT, MODID)
- EFFECTS.register("id", MinhaEffect::new) → DeferredHolder<MobEffect, MinhaEffect>
- Registrar no MysticRealm.java via EFFECTS.register(modEventBus)
- Ícone: assets/mysticrealm/textures/mob_effect/id.png — DEVE ser 18x18 px
- MobEffect(MobEffectCategory, int color) — construtor
- Para efeitos passivos (sem tick behavior): não sobrescrever shouldApplyEffectTickThisIteration
  (método não existe nesta versão — NÃO usar @Override)
- Para transformações pendentes: implementar IPendingTransformation

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

CONFIG SCREEN (NeoForge):
- .translation("chave") OBRIGATÓRIO em cada entry do builder — lang file sozinho não funciona
- Chave de entry: "mysticrealm.configuration.{seção}.{chave}" (ex: "mysticrealm.configuration.vampire.bloodDrainAmount")
- Chave de seção (cabeçalho): NeoForge usa apenas o nome do último push(), sem os pais
  ex: BUILDER.push("vampire").push("progression") → chave da seção = "mysticrealm.configuration.progression"
  → adicionar AMBAS as chaves no lang (com e sem o pai) por segurança
- Tooltip: "mysticrealm.configuration.{seção}.{chave}.tooltip" no lang file
- Formato de acumuladores fracionários (padrão do projeto):
  - bloodAccumulator / essenceAccumulator em BloodDrainAction (Map<UUID, Float/Double>)
  - Acumula valor fracionário por tick e só aplica inteiros — evita arredondamento para zero
- Config em disco (.toml) desatualizado causa loop no FileWatcher — deletar o arquivo para regenerar

BUSES (MOD vs NeoForge):
- MOD bus: EntityAttributeCreationEvent, RegisterSpawnPlacementsEvent, RegisterKeyMappingsEvent,
           EntityRenderersEvent, RegisterLayerDefinitions, RegisterPayloadHandlersEvent
- NeoForge bus: PlayerTickEvent, LivingDamageEvent, EntityTickEvent, RegisterCommandsEvent,
                PlayerLoggedOutEvent, RenderGuiEvent, ClientTickEvent

---

PROBLEMAS CONHECIDOS DO IDE (VS CODE):
- Erros "net.minecraft cannot be resolved" são de indexação — NÃO são erros reais
- Fix: rodar gradlew eclipse
- Verificar erros reais SEMPRE com: .\gradlew.bat build

---

PRÓXIMAS FASES PLANEJADAS:
- Fase 4d: Poderes por Rank (RankBenefitsRegistry — ganchos já existem via VampireRankChangedEvent)
- Fase 5: Lobisomens (RaceResource RAGE, transformação lunar, habilidades físicas)
  - Reutilizar IPendingTransformation para WerewolfCurseEffect (mesmo fluxo de morte/transformação)
  - Reutilizar ChannelAction para habilidades ativas do lobisomem
- Fase 6: Bruxaria (RaceResource MANA, feitiços, crafting sobrenatural)
  - Reutilizar ChannelAction para lançamento de feitiços
