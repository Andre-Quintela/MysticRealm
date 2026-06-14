# Fases Concluídas

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

Fase 4f — Unificação do Sistema de "Quantidade de Sangue" das Criaturas:
- BloodEssenceRegistry removido por completo (tabela fixa "1L, 2L, 3L..." por EntityType)
  - Substituído por uma única fórmula em BloodBalance.maxBloodForEntity():
    maxBlood = Math.max(1.0f, entity.getMaxHealth() / 2.0f)
  - Esse valor agora representa, ao mesmo tempo: o tamanho do pool de sangue da vítima
    (EntityBloodData.maxBlood), o total de essência de sangue e o total de food units
    que o vampiro ganha ao drenar o pool inteiro
  - Ex: criatura com 20 HP → pool = 10 → drenar tudo rende 10 food units + 10 de essência
- BloodBalance.foodPerInterval() removido (era fixo, +4 food por drenagem completa
  independente da criatura)
- BloodDrainAction: bloodAccumulator (Float) + essenceAccumulator (Double) fundidos em
  um único drainAccumulator (Map<UUID, Double>) — a cada intervalo de 5 ticks acumula
  actuallyDrained (sangue efetivamente drenado do pool); ao atingir >= 1, concede o
  mesmo valor inteiro via VampireProgressionService.grantEssence() E FoodData.eat()
- MysticNetwork.syncDrainToClient(): no branch "hovering" (sem drenagem ativa), agora
  só calcula/exibe o blood pool se DrainableEntityRegistry.isValidTarget(hovered, player)
  — antes qualquer LivingEntity próximo (zumbi, esqueleto, etc.) aparecia com uma barra
  de sangue na HUD mesmo não sendo drenável
