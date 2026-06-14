# Estrutura de Pacotes

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
    │   ├── BloodDrainAction.java               ← drainAccumulator (fracionário, único — food + essência)
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
