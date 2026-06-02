package com.nashgoldd.mysticrealm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nashgoldd.mysticrealm.attachment.PlayerSupernaturalData;
import com.nashgoldd.mysticrealm.network.MysticNetwork;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.race.RaceType;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
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

                // ── Progressão ───────────────────────────────────────────────
                .then(Commands.literal("level")
                    .then(Commands.argument("value", IntegerArgumentType.integer(1))
                        .executes(ctx -> cmdSetLevel(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value")))))

                .then(Commands.literal("xp")
                    .then(Commands.argument("value", LongArgumentType.longArg(0))
                        .executes(ctx -> cmdSetXp(ctx.getSource(), LongArgumentType.getLong(ctx, "value")))))

                .then(Commands.literal("info")
                    .executes(ctx -> cmdInfo(ctx.getSource())))

                // ── Sangue ───────────────────────────────────────────────────
                .then(Commands.literal("blood")
                    .then(Commands.literal("set")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                            .executes(ctx -> cmdBloodSet(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value")))))
                    .then(Commands.literal("add")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                            .executes(ctx -> cmdBloodAdd(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value")))))
                    .then(Commands.literal("info")
                        .executes(ctx -> cmdBloodInfo(ctx.getSource()))))

                // ── Vampiro ──────────────────────────────────────────────────
                .then(Commands.literal("vampire")
                    .then(Commands.literal("transform")
                        .executes(ctx -> cmdVampireTransform(ctx.getSource())))
                    .then(Commands.literal("cure")
                        .executes(ctx -> cmdVampireCure(ctx.getSource()))))
        );
    }

    // ── Handlers de raça / progressão ────────────────────────────────────────

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

    private static int cmdSetLevel(CommandSourceStack source, int level)
            throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PlayerSupernaturalData data = player.getData(MysticAttachments.SUPERNATURAL_DATA);
        data.setLevel(level, player);
        MysticNetwork.syncToClient(player);
        source.sendSuccess(() -> Component.literal("Level changed to " + data.getLevel()), false);
        return 1;
    }

    private static int cmdSetXp(CommandSourceStack source, long xp)
            throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PlayerSupernaturalData data = player.getData(MysticAttachments.SUPERNATURAL_DATA);
        data.setExperience(xp, player);
        MysticNetwork.syncToClient(player);
        source.sendSuccess(() -> Component.literal("Experience changed to " + data.getExperience()), false);
        return 1;
    }

    private static int cmdInfo(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PlayerSupernaturalData data = player.getData(MysticAttachments.SUPERNATURAL_DATA);
        source.sendSuccess(() -> Component.literal(
            "§6[MysticRealm]§r Race: §b" + data.getRace().name() +
            " §r| Level: §a" + data.getLevel() +
            " §r| XP: §e" + data.getExperience()
        ), false);
        return 1;
    }

    // ── Handlers de sangue ────────────────────────────────────────────────────

    private static int cmdBloodSet(CommandSourceStack source, int value)
            throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!VampireService.isVampire(player)) {
            source.sendFailure(Component.literal("Apenas vampiros têm sangue."));
            return 0;
        }
        VampireData data = VampireService.getData(player);
        data.setBloodLevel(value, player);
        MysticNetwork.syncVampireToClient(player);
        source.sendSuccess(() -> Component.literal("Blood set to " + data.getBloodLevel() + "/" + data.getMaxBlood()), false);
        return 1;
    }

    private static int cmdBloodAdd(CommandSourceStack source, int amount)
            throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!VampireService.isVampire(player)) {
            source.sendFailure(Component.literal("Apenas vampiros têm sangue."));
            return 0;
        }
        VampireData data = VampireService.getData(player);
        data.addBlood(amount, player);
        MysticNetwork.syncVampireToClient(player);
        source.sendSuccess(() -> Component.literal("Blood +" + amount + " → " + data.getBloodLevel() + "/" + data.getMaxBlood()), false);
        return 1;
    }

    private static int cmdBloodInfo(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!VampireService.isVampire(player)) {
            source.sendFailure(Component.literal("Apenas vampiros têm sangue."));
            return 0;
        }
        VampireData data = VampireService.getData(player);
        source.sendSuccess(() -> Component.literal(
            "§6[Vampiro]§r Blood: §4" + data.getBloodLevel() + "§r/" + data.getMaxBlood() +
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
        source.sendSuccess(() -> Component.literal("§4Transformado em Vampiro.§r Blood: " +
            VampireService.getData(player).getBloodLevel()), false);
        return 1;
    }

    private static int cmdVampireCure(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        VampireService.cure(player);
        source.sendSuccess(() -> Component.literal("Curado. Race: HUMAN"), false);
        return 1;
    }
}
