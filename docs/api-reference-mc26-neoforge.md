# API Reference — MC 26.x / NeoForge 26.x

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
  - drainAccumulator em BloodDrainAction (Map<UUID, Double>)
  - Acumula valor fracionário por tick e só aplica inteiros — evita arredondamento para zero
- Config em disco (.toml) desatualizado causa loop no FileWatcher — deletar o arquivo para regenerar

BUSES (MOD vs NeoForge):
- MOD bus: EntityAttributeCreationEvent, RegisterSpawnPlacementsEvent, RegisterKeyMappingsEvent,
           EntityRenderersEvent, RegisterLayerDefinitions, RegisterPayloadHandlersEvent,
           RegisterParticleProvidersEvent
- NeoForge bus: PlayerTickEvent, LivingDamageEvent, EntityTickEvent, RegisterCommandsEvent,
                PlayerLoggedOutEvent, RenderGuiEvent, ClientTickEvent
