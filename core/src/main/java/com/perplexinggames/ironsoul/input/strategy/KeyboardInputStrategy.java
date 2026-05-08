package com.perplexinggames.ironsoul.input.strategy;

import com.perplexinggames.ironsoul.input.adapter.DeviceInputAdapter;
import com.perplexinggames.ironsoul.input.adapter.KeyboardControl;
import com.perplexinggames.ironsoul.input.command.Command;
import com.perplexinggames.ironsoul.input.command.CommandContext;
import com.perplexinggames.ironsoul.input.command.CommandQueue;
import com.perplexinggames.ironsoul.input.mapper.ContextualInputMapper;

public class KeyboardInputStrategy implements InputStrategy {
    private final DeviceInputAdapter<KeyboardControl> inputAdapter;
    private final ContextualInputMapper<KeyboardControl> mapper;

    public KeyboardInputStrategy(
        DeviceInputAdapter<KeyboardControl> inputAdapter,
        ContextualInputMapper<KeyboardControl> mapper
    ) {
        this.inputAdapter = inputAdapter;
        this.mapper = mapper;
    }

    @Override
    public void collectCommands(CommandQueue queue, float delta, CommandContext context) {
        for (Command command : mapper.map(inputAdapter.poll(), context)) {
            queue.enqueue(command);
        }
    }

    @Override
    public String getName() {
        return "Keyboard";
    }
}
