package com.nashgoldd.mysticrealm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nashgoldd.mysticrealm.attachment.PlayerSupernaturalData;
import com.nashgoldd.mysticrealm.network.MysticNetwork;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.race.RaceType;
import com.nashgoldd.mysticrealm.config.MysticConfig;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import com.nashgoldd.mysticrealm.supernatural.vampire.progression.VampireProgressionService;
import com.nashgoldd.mysticrealm.supernatural.vampire.progression.VampireRank;
import com.nashgoldd.mysticrealm.supernatural.vampire.service.VampireService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public final class MysticCommands {

    private MysticCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("mystic")
                .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))

                // ── Raça ─────────────────────────────────────────────────────
                .then(Commands.literal("race")
                    .then(Commands.argument("race", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (RaceType r : RaceType.values()) builder.suggest(r.name().toLowerCase());
                            return builder.buildFuture();
                        })
                        .executes(ctx -> cmdSetRace(ctx.getSource(), StringArgumentType.getString(ctx, "race")))))

                .then(Commands.literal("info")
                    .executes(ctx -> cmdInfo(ctx.getSource())))

                // ── Sangue (0-20, escala nativa do FoodData) ─────────────────
                .then(Commands.literal("blood")
                    .then(Commands.literal("set")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 20))
                            .executes(ctx -> cmdBloodSet(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value")))))
                    .then(Commands.literal("add")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 20))
                            .executes(ctx -> cmdBloodAdd(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value")))))
                    .then(Commands.literal("info")
                        .executes(ctx -> cmdBloodInfo(ctx.getSource()))))

                // ── Vampiro ──────────────────────────────────────────────────
                .then(Commands.literal("vampire")
                    .then(Commands.literal("transform")
                        .executes(ctx -> cmdVampireTransform(ctx.getSource())))
                    .then(Commands.literal("cure")
                        .executes(ctx -> cmdVampireCure(ctx.getSource())))
                    .then(Commands.literal("ascend")
                        .executes(ctx -> cmdVampireAscend(ctx.getSource()))))
        );
    }

    // ── Handlers de raça ─────────────────────────────────────────────────────

    private static int cmdSetRace(CommandSourceStack source, String raceName)
            throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        RaceType race;
        try {
            race = RaceType.valueOf(raceName.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Raça inválida: '" + raceName + "'. Opções: human, vampire, werewolf, witch"));
            return 0;
        }
        player.getData(MysticAttachments.SUPERNATURAL_DATA).setRace(race, player);
        MysticNetwork.syncToClient(player);
        source.sendSuccess(() -> Component.literal("Race changed to " + race.name()), false);
        return 1;
    }

    private static int cmdInfo(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PlayerSupernaturalData data = player.getData(MysticAttachments.SUPERNATURAL_DATA);
        source.sendSuccess(() -> Component.literal(
            "§6[MysticRealm]§r Race: §b" + data.getRace().name()
        ), false);
        return 1;
    }

    // ── Handlers de sangue (0-20, escala nativa) ──────────────────────────────

    private static int cmdBloodSet(CommandSourceStack source, int value)
            throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!VampireService.isVampire(player)) {
            source.sendFailure(Component.literal("Apenas vampiros têm sangue."));
            return 0;
        }
        player.getFoodData().setFoodLevel(value);
        MysticNetwork.syncVampireToClient(player);
        source.sendSuccess(() -> Component.literal("Blood set to " + value + "/20"), false);
        return 1;
    }

    private static int cmdBloodAdd(CommandSourceStack source, int amount)
            throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!VampireService.isVampire(player)) {
            source.sendFailure(Component.literal("Apenas vampiros têm sangue."));
            return 0;
        }
        int current = player.getFoodData().getFoodLevel();
        int newLevel = Math.min(20, current + amount);
        player.getFoodData().setFoodLevel(newLevel);
        MysticNetwork.syncVampireToClient(player);
        source.sendSuccess(() -> Component.literal("Blood +" + amount + " → " + newLevel + "/20"), false);
        return 1;
    }

    private static int cmdBloodInfo(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!VampireService.isVampire(player)) {
            source.sendFailure(Component.literal("Apenas vampiros têm sangue."));
            return 0;
        }
        VampireData data = VampireService.getData(player);
        int blood = player.getFoodData().getFoodLevel();
        source.sendSuccess(() -> Component.literal(
            "§6[Vampiro]§r Blood: §4" + blood + "§r/20" +
            " | Rank: §5" + data.getRank().displayName() +
            " | Essence: §e" + data.getBloodEssence() +
            " | Transformed: " + data.isTransformed() +
            " | Sunlight: " + data.isSunlightBurning() +
            " | NearDeath: " + data.isNearDeath()
        ), false);
        return 1;
    }

    // ── Handlers de transformação ─────────────────────────────────────────────

    private static int cmdVampireTransform(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        VampireService.transform(player);
        int blood = player.getFoodData().getFoodLevel();
        source.sendSuccess(() -> Component.literal("§4Transformado em Vampiro.§r Blood: " + blood + "/20"), false);
        return 1;
    }

    private static int cmdVampireAscend(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        if (!VampireService.isVampire(player)) {
            source.sendFailure(Component.literal("§4Apenas vampiros podem ascender."));
            return 0;
        }
        if (!MysticConfig.ENABLE_VAMPIRE_PROGRESSION.get()) {
            source.sendFailure(Component.literal("O sistema de progressão vampírica está desativado."));
            return 0;
        }

        VampireData data = VampireService.getData(player);
        VampireRank current = data.getRank();

        if (current.isMax()) {
            source.sendFailure(Component.literal("§4Você já atingiu o estágio máximo: §6" + current.displayName()));
            return 0;
        }

        long requiredEssence = VampireProgressionService.getRequiredEssence(current);
        long requiredAgeTicks = VampireProgressionService.getRequiredAgeHours(current) * 72000L;
        long currentEssence = data.getBloodEssence();
        long currentAgeTicks = data.getVampireAgeTicks();

        if (currentEssence < requiredEssence) {
            source.sendFailure(Component.literal(
                "§4Essência insuficiente. Necessário: §6" + requiredEssence + "§4, atual: §c" + currentEssence));
            return 0;
        }
        if (currentAgeTicks < requiredAgeTicks) {
            long requiredHours = VampireProgressionService.getRequiredAgeHours(current);
            long currentHours = currentAgeTicks / 72000L;
            source.sendFailure(Component.literal(
                "§4Idade insuficiente. Necessário: §6" + requiredHours + "h§4, atual: §c" + currentHours + "h"));
            return 0;
        }

        VampireProgressionService.ascend(player);
        VampireRank newRank = VampireService.getData(player).getRank();
        source.sendSuccess(() -> Component.literal(
            "§6✦ Ascensão concluída! §rNovo estágio: §4" + newRank.displayName()), true);
        return 1;
    }

    private static int cmdVampireCure(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        VampireService.cure(player);
        source.sendSuccess(() -> Component.literal("Curado. Race: HUMAN"), false);
        return 1;
    }
}
