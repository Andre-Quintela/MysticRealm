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
  - onTick(): a cada 5 ticks — partícula customizada MysticParticles.BLOOD_DRAIN no pescoço + SoundEvents.WITCH_DRINK
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

Fase 4d — Obelisco Vampírico (VampireObeliskBlock):
- VampireObeliskBlock (extends BaseEntityBlock): bloco interagível com modelo GeckoLib
  - codec() obrigatório: private static final MapCodec<VampireObeliskBlock> CODEC = simpleCodec(VampireObeliskBlock::new)
  - getRenderShape() → RenderShape.INVISIBLE (GeckoLib renderiza via BlockEntityRenderer)
  - useWithoutItem(): level instanceof ServerLevel → envia OpenObeliskScreenPacket → return SUCCESS_SERVER
    lado cliente: return InteractionResult.SUCCESS (sem servidor)
- VampireObeliskBlockEntity (extends BlockEntity implements GeoBlockEntity): anima o obelisco
- VampireObeliskRenderer (extends GeoBlockRenderer<VampireObeliskBlockEntity, BlockEntityRenderState>)
  - Usa DefaultedBlockGeoModel para resolver paths automaticamente
- OpenObeliskScreenPacket (S→C, sem campos): aciona abertura da GUI no cliente
- VampireObeliskScreen (extends Screen): GUI com progressão vampírica + lore
  - Fundo customizado 600x442 (textures/gui/vampire_obelisk_background.png), desenhado via
    g.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0, 0, W, H, W, H) — textura cheia, fora do atlas de sprites
  - Título escalado: g.pose().pushMatrix() → g.pose().scale(scale, scale) → g.text(...) com coordenadas divididas pela escala → g.pose().popMatrix()
  - Vampiros: "Vampire Level" / "Blood Essence" / "Age" em posições fixas (95,297)/(95,335)/(95,373)
  - Painel de lore em caixa fixa (368,80)-(528,225) com wrap + clipping vertical (LORE_H / LORE_LINE_HEIGHT linhas máx)
    Estrutura preparada para paginação: List<String> LORE_PAGES + campo lorePage + nextLorePage()/previousLorePage() (sem botões ainda)
  - Component.translatable("screen.mysticrealm.obelisk.title") — chave OBRIGATÓRIA no lang file (não tem fallback)
- Registros:
  - MysticBlocks (DeferredRegister.createBlocks) + MysticBlockEntities (DeferredRegister<BlockEntityType<?>>)
  - MysticItems: BlockItem referenciando MysticBlocks.VAMPIRE_OBELISK
  - Ordem em MysticRealm.java: MysticBlocks → MysticBlockEntities → demais registros

Fase 4e — Rework do Sistema de Fome/Saturação Vampírica:
- Removida a drenagem passiva de sangue por tempo (tickBloodDrain deletado de VampireEventHandler)
  - Configs removidas: VAMPIRE_BLOOD_DRAIN_AMOUNT / VAMPIRE_BLOOD_DRAIN_INTERVAL_SECONDS (+ traduções en_us.json)
  - Sangue (foodLevel) não cai mais só por estar parado/passar o tempo
- Saturação vanilla restaurada: removido o setSaturation(0f) forçado a cada tick em onPlayerTick
  - Sangue agora drena via exaustão vanilla (correr, pular, lutar, minerar, levar dano) — drena
    primeiro a saturação, depois o foodLevel quando a saturação chega a 0
- Alimentação/restauração de sangue agora usa FoodData.eat(nutrition, saturationModifier) em vez de setFoodLevel():
  - BloodDrainAction.onTick() (acumulador fracionário) e BloodVialItem.finishUsingItem()
  - eat() já clampa foodLevel em 20 e ajusta saturação proporcionalmente (capada no novo foodLevel)
- BloodBalance.bloodSaturationModifier() → MysticConfig.BLOOD_SATURATION_MODIFIER (DoubleValue, default 0.6, range 0.0-2.0)
  - Mesmo modificador do pão vanilla; com eat() vira +1.2 saturação por ponto de food restaurado
- VampireService.transform(): saturação inicial agora 5f (igual cure()), em vez de 0f —
  evita que o vampiro recém-transformado já comece perdendo sangue por exaustão
- NOTA: regenerationThreshold/speedThreshold (escala 0-20, defaults 15/10) — se o
  run/config/mysticrealm-common.toml tiver valores antigos/incorretos (ex: 20/20),
  deletar o arquivo para regenerar com os defaults atuais

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
│   ├── ClientPacketHandlers.java               ← handleSyncVampireData + handleSyncDrainState + handleSyncVampireProgression + handleOpenObeliskScreen
│   └── ServerPacketHandlers.java               ← handleRequestDrain + handleCancelDrain
├── registry/
│   ├── MysticAttachments.java                  ← SUPERNATURAL_DATA + VAMPIRE_DATA + ENTITY_BLOOD
│   ├── MysticBlocks.java                       ← DeferredRegister.createBlocks + VAMPIRE_OBELISK
│   ├── MysticBlockEntities.java                ← DeferredRegister<BlockEntityType<?>> + VAMPIRE_OBELISK
│   ├── MysticEffects.java                      ← DeferredRegister<MobEffect> + VAMPIRE_INFECTION
│   ├── MysticItems.java                        ← inclui BlockItem do VAMPIRE_OBELISK
│   ├── MysticParticles.java                    ← DeferredRegister<ParticleType<?>> + BLOOD_DRAIN
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
    ├── block/
    │   ├── VampireObeliskBlock.java            ← BaseEntityBlock + GeckoLib + GUI via packet
    │   └── entity/VampireObeliskBlockEntity.java ← GeoBlockEntity + AnimationController
    ├── client/
    │   ├── VampireHudOverlay.java              ← ícones de sangue + rank/essência/idade + barra de alvo
    │   ├── VampireKeyBindings.java
    │   ├── VampireClientInputHandler.java
    │   ├── ClientDrainState.java
    │   ├── animation/VampireEntityAnimations.java  ← idle, walk, meleeAttack, rangedAttack
    │   ├── model/VampireEntityModel.java           ← EntityModel<RenderState> + KeyframeAnimation
    │   ├── particle/BloodDrainParticle.java        ← SingleQuadParticle + Provider (drenagem de sangue)
    │   ├── renderer/
    │   │   ├── HostileVampireRenderState.java       ← extends LivingEntityRenderState
    │   │   ├── VampireEntityRenderer.java           ← MobRenderer 3 type params
    │   │   └── VampireObeliskRenderer.java          ← GeoBlockRenderer (GeckoLib 5.x)
    │   └── screen/VampireObeliskScreen.java         ← GUI de progressão + lore
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
    │   ├── SyncDrainStatePacket.java
    │   └── OpenObeliskScreenPacket.java        ← S→C, sem campos, StreamCodec.unit()
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
  ← Arquivos: blood_vial.json, wooden_stake.json, vampire_blood_vial.json, hostile_vampire_spawn_egg.json, vampire_obelisk.json

src/main/resources/assets/mysticrealm/models/item/
  ← Define geometria/textura do item (ainda necessário, referenciado pelo arquivo em items/)
  ← Formato igual às versões anteriores: { "parent": "item/generated", "textures": { "layer0": "..." } }

src/main/resources/assets/mysticrealm/textures/mob_effect/
  ← Ícones de efeito de poção — DEVE ser 18x18 px (obrigatório, valores menores não funcionam)
  ← Nome do arquivo = nome do efeito no registro (ex: vampire_infection.png)

src/main/resources/assets/mysticrealm/particles/particle_id.json
  ← descriptor de partícula customizada — lista as texturas usadas pelo SpriteSet
  ← Formato: { "textures": ["namespace:sprite_name"] }
  ← Sprite é resolvida em textures/particle/sprite_name.png do atlas de partículas

src/main/resources/assets/mysticrealm/textures/particle/
  ← Texturas de partículas customizadas (PNG, qualquer tamanho — recomendado 16x16 ou 32x32)

src/main/resources/assets/mysticrealm/geckolib/models/block/
  ← Modelos GeckoLib (.geo.json) de blocos — GeckoLib 5.x lê APENAS dessa pasta
  ← NÃO usar geo/ (caminho legado ignorado em 5.x sem aviso claro)

src/main/resources/assets/mysticrealm/geckolib/animations/block/
  ← Animações GeckoLib (.animation.json) de blocos
  ← Padrão: vampire_obelisk.animation.json com { "format_version": "1.8.0", "animations": {...} }

src/main/resources/assets/mysticrealm/textures/block/
  ← Texturas de blocos (incluindo a textura do modelo GeckoLib — caminho normal do MC)

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
- Pacote sem campos: StreamCodec.unit(new MeuPacket()) com tipo RegistryFriendlyByteBuf (não ByteBuf)

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
- level.isClientSide tem acesso PRIVADO em Level — usar level instanceof ServerLevel em vez disso
  ex: if (level instanceof ServerLevel serverLevel) { ... } // lado servidor
      else { ... } // lado cliente

INTERAÇÃO COM BLOCOS (MC 26.x):
- InteractionResult.sidedSuccess() NÃO EXISTE — foi removido
- Substituto:
  - Lado servidor: return InteractionResult.SUCCESS_SERVER  (SwingSource.SERVER)
  - Lado cliente:  return InteractionResult.SUCCESS         (SwingSource.CLIENT)
- InteractionResult é uma sealed interface com records: Success, Fail, Pass, TryEmptyHandInteraction
- useWithoutItem() recebe (BlockState, Level, BlockPos, Player, BlockHitResult)

BLOCOS COM BLOCK ENTITY (MC 26.x):
- BaseEntityBlock.codec() é abstract — OBRIGATÓRIO implementar:
  private static final MapCodec<MeuBlock> CODEC = simpleCodec(MeuBlock::new);
  @Override public MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
- BlockEntityType.Builder NÃO EXISTE em MC 26.x — usar construtor direto:
  new BlockEntityType<>(MinhaBlockEntity::new, Set.of(MeuBlock.get()))
- RenderShape possui apenas INVISIBLE e MODEL (ENTITYBLOCK_ANIMATED foi removido)
  → usar RenderShape.INVISIBLE para blocos renderizados por GeckoLib/custom renderer
- registerBlockEntityRenderer requer dois type params:
  <T extends BlockEntity, S extends BlockEntityRenderState>
  A factory é BlockEntityRendererProvider<T, S> (functional interface: context -> renderer)
  ex: event.registerBlockEntityRenderer(MysticBlockEntities.VAMPIRE_OBELISK.get(), VampireObeliskRenderer::new)

RENDERIZAÇÃO (HUD):
- GuiGraphicsExtractor (não GuiGraphics — renomeado no MC 26.x)
- Método: g.text(Font, String, int x, int y, int color, boolean dropShadow)
- Cor em formato ARGB obrigatório: 0xFFAA0000 (não 0xAA0000 — alpha 0 = invisível)
- event.getGuiGraphics() retorna GuiGraphicsExtractor
- Sprites GUI: blitSprite(RenderPipelines.GUI_TEXTURED, spriteId, x, y, w, h)
  - spriteId via Identifier.fromNamespaceAndPath(MODID, "nome") — sem prefixo de pasta
  - Arquivos em textures/gui/sprites/nome.png

SCREENS (MC 26.x — método renomeado):
- Screen.render() NÃO EXISTE em MC 26.x — foi renomeado para extractRenderState()
- SEMPRE usar @Override — se o compilador rejeitar o @Override, a assinatura está errada
- Sintoma do erro: tela abre e escurece (super é chamado), mas conteúdo não aparece
- Assinatura correta:
  @Override
  public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(g, mouseX, mouseY, partialTick); // itera renderables
      // ... desenhar conteúdo customizado aqui
  }
- extractBackground() → renderiza fundo escuro/panorama (chamado antes do extractRenderState)
- extractRenderStateWithTooltipAndSubtitles() → método final, chamado pelo engine, chama extractBackground + extractRenderState
- Texto/UI escalado: g.text() não tem parâmetro de escala — usar g.pose().pushMatrix() / g.pose().scale(sx, sy) / g.pose().popMatrix()
  e dividir as coordenadas x/y pela escala (o matrix stack escala o espaço de desenho inteiro)
- Fundo de imagem cheia (fora do atlas de sprites, ex. telas grandes): g.blit(RenderPipelines.GUI_TEXTURED, Identifier, x, y, u, v, width, height, textureWidth, textureHeight)
  com Identifier apontando para o caminho completo da textura (ex. "textures/gui/nome.png")
- GUI responsiva (telas grandes com tamanho fixo, ex. 600x442): em vez de centralizar com x=(width-W)/2 (estoura
  quando width/height < W/H), envolver TODO o desenho em um único bloco escalado:
  ```java
  float scale = Math.min(1.0f, Math.min((width - MARGIN) / (float) W, (height - MARGIN) / (float) H));
  float offsetX = (width - W * scale) / 2f;
  float offsetY = (height - H * scale) / 2f;
  g.pose().pushMatrix();
  g.pose().translate(offsetX, offsetY);
  g.pose().scale(scale, scale);
  // desenhar tudo em coordenadas locais 0..W / 0..H (sem somar offsetX/offsetY)
  g.pose().popMatrix();
  ```
  pose() é Matrix3x2fStack — translate() é aplicado antes do scale() na composição (screenPos = offset + local*scale),
  então as constantes de layout originais continuam válidas sem alteração. scale nunca passa de 1.0 (não amplia em
  telas grandes). Para escalas aninhadas (ex. título maior), empilhar outro pushMatrix/scale dentro do bloco.
  Limitação: sem transformar mouseX/mouseY, hit-testing de botões dentro da área escalada não funciona — se a tela
  ganhar elementos clicáveis, converter com (mouseX - offsetX) / scale antes de testar.
  Exemplo: VampireObeliskScreen.java

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

PARTÍCULAS CUSTOMIZADAS (MC 26.x — API completamente diferente de versões anteriores):
- TextureSheetParticle NÃO EXISTE — substituto é SingleQuadParticle (net.minecraft.client.particle)
- SingleQuadParticle é abstract — deve implementar getLayer() retornando SingleQuadParticle.Layer
  Layers disponíveis: OPAQUE, TRANSLUCENT, OPAQUE_TERRAIN, TRANSLUCENT_TERRAIN, OPAQUE_ITEMS, TRANSLUCENT_ITEMS
  → usar TRANSLUCENT para partículas com transparência (PNG com canal alpha)
- Construtor: super(level, x, y, z, xa, ya, za, sprite) — recebe TextureAtlasSprite diretamente
  NÃO existe pickSprite(SpriteSet) — o sprite vem do SpriteSet.get(random) no Provider
- Campos herdados de Particle: lifetime, gravity, friction, hasPhysics, age, xd, yd, zd
  gravity negativo = flutua para cima (yd += 0.04 * |gravity| por tick)
- Métodos herdados de SingleQuadParticle: setAlpha(float), setColor(r,g,b), scale(float)
  NÃO existe getRenderType() — usar getLayer() obrigatoriamente
  getGroup() em SingleQuadParticle retorna ParticleRenderType.SINGLE_QUADS automaticamente (não sobrescrever)
- ParticleRenderType em MC 26.x é um record (não enum) — constantes: SINGLE_QUADS, ITEM_PICKUP, etc.
- Provider pattern correto:
  ```java
  public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;
      public Provider(SpriteSet sprites) { this.sprites = sprites; }
      @Override
      public Particle createParticle(SimpleParticleType type, ClientLevel level,
          double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
          return new MinhaParticle(level, x, y, z, sprites.get(random));
      }
  }
  ```
  ATENÇÃO: createParticle tem RandomSource random como ÚLTIMO parâmetro (adicionado no MC 26.x)
- Registro (server-side): DeferredRegister.create(Registries.PARTICLE_TYPE, MODID)
  PARTICLE_TYPES.register("id", () -> new SimpleParticleType(false))
  Registrar no MysticRealm.java via MysticParticles.register(modEventBus)
- Registro (client-side): RegisterParticleProvidersEvent → MOD bus
  event.registerSpriteSet(MysticParticles.BLOOD_DRAIN.get(), BloodDrainParticle.Provider::new)
  Registrar via modEventBus.addListener() no @Mod(dist=CLIENT)
- Assets obrigatórios:
  1. assets/namespace/particles/particle_id.json → { "textures": ["namespace:sprite_name"] }
  2. assets/namespace/textures/particle/sprite_name.png → textura da partícula

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
           EntityRenderersEvent, RegisterLayerDefinitions, RegisterPayloadHandlersEvent,
           RegisterParticleProvidersEvent
- NeoForge bus: PlayerTickEvent, LivingDamageEvent, EntityTickEvent, RegisterCommandsEvent,
                PlayerLoggedOutEvent, RenderGuiEvent, ClientTickEvent

---

GECKOLIB 5.5.1 (com.geckolib — API completamente diferente das versões 4.x):

DEPENDÊNCIA (build.gradle):
```groovy
repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = 'GeckoLib'
                url = 'https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/'
            }
        }
        filter { includeGroupAndSubgroups('com.geckolib') }
    }
}
dependencies {
    implementation "com.geckolib:geckolib-neoforge-${minecraft_version}:${geckolib_version}"
}
```
- GroupId mudou de software.bernie.geckolib para com.geckolib
- exclusiveContent obrigatório para evitar conflito com outros repositórios Maven

ESTRUTURA DE PACKAGES (GeckoLib 5.x):
- com.geckolib.animatable.GeoBlockEntity          ← interface para BlockEntity animável
- com.geckolib.animatable.manager.AnimatableManager ← NÃO com.geckolib.animation.AnimatableManager
- com.geckolib.animatable.instance.AnimatableInstanceCache
- com.geckolib.animation.AnimationController      ← mesmo package que antes
- com.geckolib.animation.RawAnimation             ← mesmo package que antes
- com.geckolib.animation.object.PlayState         ← NÃO com.geckolib.animation.PlayState
- com.geckolib.model.DefaultedBlockGeoModel       ← resolve paths automaticamente por convenção
- com.geckolib.model.DefaultedGeoModel            ← base de DefaultedBlockGeoModel
- com.geckolib.renderer.GeoBlockRenderer          ← NÃO GeoBlockEntityRenderer (não existe em 5.x)
- com.geckolib.util.GeckoLibUtil                  ← createInstanceCache(this)

ANIMATIONCONTROLLER (GeckoLib 5.x — construtor mudou):
- GeckoLib 4.x: new AnimationController<>(this, "nome", ticks, state -> {...})
- GeckoLib 5.x: new AnimationController<MinhaClasse>("nome", ticks, animTest -> {...})
  → NÃO recebe o animatable (this) como primeiro argumento
  → Java não consegue inferir <> sozinho — sempre declarar o tipo: new AnimationController<MinhaBlockEntity>(...)
- AnimationStateHandler é @FunctionalInterface aninhada: PlayState handle(AnimationTest<A> animatable)
- AnimationTest.setAndContinue(RawAnimation) → define animação e retorna PlayState.CONTINUE automaticamente
  → substitui o padrão antigo de state.setAnimation() + return PlayState.CONTINUE separados
- Construtores disponíveis (sem this):
  new AnimationController<T>(AnimationStateHandler<T> handler)
  new AnimationController<T>(String name, AnimationStateHandler<T> handler)
  new AnimationController<T>(String name, int transitionTicks, AnimationStateHandler<T> handler)

GEOMODEL — SISTEMA DE PATHS (GeckoLib 5.x):
- GeckoLib 5.x lê arquivos de:
  - Modelos: assets/{namespace}/geckolib/models/{subtype}/{nome}.geo.json
  - Animações: assets/{namespace}/geckolib/animations/{subtype}/{nome}.animation.json
  - Texturas: assets/{namespace}/textures/{subtype}/{nome}.png  (caminho normal do MC)
- NÃO usar geo/ nem animations/ da raiz — esses eram caminhos de GeckoLib 4.x e são silenciosamente ignorados
- A chave do cache é o Identifier após stripPrefixAndSuffix():
  arquivo geckolib/models/block/vampire_obelisk.geo.json → chave mysticrealm:block/vampire_obelisk
- DefaultedBlockGeoModel(Identifier assetSubpath):
  - subtype = "block"
  - modelPath      = assetSubpath.withPrefix("block/")         → mysticrealm:block/vampire_obelisk
  - texturePath    = "textures/block/" + path + ".png"         → mysticrealm:textures/block/vampire_obelisk.png
  - animationsPath = assetSubpath.withPrefix("block/")         → mysticrealm:block/vampire_obelisk
  ex: new DefaultedBlockGeoModel<>(Identifier.fromNamespaceAndPath(MODID, "vampire_obelisk"))
    → modelo em geckolib/models/block/vampire_obelisk.geo.json
    → animação em geckolib/animations/block/vampire_obelisk.animation.json
    → textura em textures/block/vampire_obelisk.png
- withAltModel(id) / withAltAnimations(id) / withAltTexture(id) — override de path individual

GEORENDERER DE BLOCOS (GeckoLib 5.x):
- GeoBlockEntityRenderer NÃO EXISTE — usar GeoBlockRenderer
- GeoBlockRenderer<T extends BlockEntity & GeoAnimatable, R extends BlockEntityRenderState>
  - implements GeoRenderer<T, Void, R> e BlockEntityRenderer<T, R>
  - Construtor com GeoModel: GeoBlockRenderer(Context context, GeoModel<T> model)
  - Construtor com BlockEntityType: GeoBlockRenderer(Context context, BlockEntityType<? extends T> type)
    → usa DefaultedBlockGeoModel internamente com o ID registrado da BlockEntity
- NÃO sobrescrever getModelResource(), getTextureResource(), getAnimationResource() (não existem como @Override)
  → toda a configuração é passada via GeoModel no construtor
- Registrar via EntityRenderersEvent.RegisterRenderers (MOD bus):
  event.registerBlockEntityRenderer(MysticBlockEntities.VAMPIRE_OBELISK.get(), VampireObeliskRenderer::new)
  → O tipo correto é BlockEntityRendererProvider<T, S> (functional interface)

GEOBLOCKENITY — REGISTERCONTROLLERS (GeckoLib 5.x):
- AnimatableManager.ControllerRegistrar.add(AnimationController<?>... controllers)
- Exemplo completo de um bloco com idle:
  ```java
  @Override
  public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
      controllers.add(new AnimationController<VampireObeliskBlockEntity>("idle_controller", 0,
          animTest -> animTest.setAndContinue(IDLE)));
  }
  ```

---

PROBLEMAS CONHECIDOS DO IDE (VS CODE):
- Erros "net.minecraft cannot be resolved" são de indexação — NÃO são erros reais
- Erros de tipos GeckoLib (GeoBlockEntity, RawAnimation, etc.) também podem ser falsos positivos de indexação
- Fix: rodar gradlew eclipse
- Verificar erros reais SEMPRE com: .\gradlew.bat build

---

PRÓXIMAS FASES PLANEJADAS:
- Fase 4f: Poderes por Rank (RankBenefitsRegistry — ganchos já existem via VampireRankChangedEvent)
- Fase 5: Lobisomens (RaceResource RAGE, transformação lunar, habilidades físicas)
  - Reutilizar IPendingTransformation para WerewolfCurseEffect (mesmo fluxo de morte/transformação)
  - Reutilizar ChannelAction para habilidades ativas do lobisomem
- Fase 6: Bruxaria (RaceResource MANA, feitiços, crafting sobrenatural)
  - Reutilizar ChannelAction para lançamento de feitiços
