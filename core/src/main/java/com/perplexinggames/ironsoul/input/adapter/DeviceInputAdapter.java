package com.perplexinggames.ironsoul.input.adapter;

public interface DeviceInputAdapter<C extends Enum<C>> {
    ControlInputFrame<C> poll();
}
