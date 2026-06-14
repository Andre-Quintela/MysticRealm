# GeckoLib 5.5.1 Reference

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
