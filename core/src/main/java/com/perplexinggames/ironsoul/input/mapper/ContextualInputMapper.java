package com.perplexinggames.ironsoul.input.mapper;

import com.perplexinggames.ironsoul.core.gamestate.GameState;
import com.perplexinggames.ironsoul.input.adapter.ControlInputFrame;
import com.perplexinggames.ironsoul.input.binding.ActionBindingProfile;
import com.perplexinggames.ironsoul.input.binding.InputActionEvent;
import com.perplexinggames.ironsoul.input.command.Command;
import com.perplexinggames.ironsoul.input.command.CommandContext;
import com.perplexinggames.ironsoul.input.command.InputCommandFactory;

import java.util.ArrayList;
import java.util.List;

public class ContextualInputMapper<C extends Enum<C>> {
    private final ActionBindingProfile<C> bindings;
    private final InputCommandFactory commandFactory;

    public ContextualInputMapper(ActionBindingProfile<C> bindings, InputCommandFactory commandFactory) {
        this.bindings = bindings;
        this.commandFactory = commandFactory;
    }

    public List<Command> map(ControlInputFrame<C> frame, CommandContext context) {
        GameState state = context.getGameStateManager().getCurrentState();
        List<Command> commands = new ArrayList<>();
        for (InputActionEvent event : bindings.resolve(frame)) {
            state.getInputMode().map(event, commandFactory, context).ifPresent(commands::add);
        }
        return commands;
    }
}
