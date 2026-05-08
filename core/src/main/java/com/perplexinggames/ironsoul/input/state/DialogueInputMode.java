package com.perplexinggames.ironsoul.input.state;

import com.perplexinggames.ironsoul.input.binding.InputAction;
import com.perplexinggames.ironsoul.input.binding.InputActionEvent;
import com.perplexinggames.ironsoul.input.binding.InputPhase;
import com.perplexinggames.ironsoul.input.command.Command;
import com.perplexinggames.ironsoul.input.command.CommandContext;
import com.perplexinggames.ironsoul.input.command.InputCommandFactory;
import com.perplexinggames.ironsoul.input.command.commands.AdvanceDialogueCommand;

import java.util.Optional;

public class DialogueInputMode implements InputMode {
    @Override
    public Optional<Command> map(InputActionEvent event, InputCommandFactory factory, CommandContext context) {
        if (event.action() == InputAction.INTERACT && event.phase() == InputPhase.START) {
            return Optional.of(new AdvanceDialogueCommand(
                context.getDialogueSystem(),
                context.getGameStateManager()
            ));
        }
        if (event.phase() == InputPhase.END || event.action() == InputAction.PAUSE) {
            return factory.create(event, context);
        }
        return Optional.empty();
    }
}
