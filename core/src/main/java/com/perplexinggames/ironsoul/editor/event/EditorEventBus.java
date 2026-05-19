package com.perplexinggames.ironsoul.editor.event;

import java.util.function.Consumer;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class EditorEventBus {
    private final ObjectMap<Class<? extends EditorEvent>, Array<Consumer<? extends EditorEvent>>> listeners;

    public EditorEventBus() {
        this.listeners = new ObjectMap<>();
    }

    public <T extends EditorEvent> void subscribe(Class<T> eventType, Consumer<T> listener) {
        Array<Consumer<? extends EditorEvent>> consumers = listeners.get(eventType);
        if (consumers == null) {
            consumers = new Array<>();
            listeners.put(eventType, consumers);
        }
        consumers.add(listener);
    }

    @SuppressWarnings("unchecked")
    public <T extends EditorEvent> void post(T event) {
        Array<Consumer<? extends EditorEvent>> consumers = listeners.get(event.getClass());
        if (consumers == null) {
            return;
        }

        for (Consumer<? extends EditorEvent> consumer : consumers) {
            ((Consumer<T>) consumer).accept(event);
        }
    }
}
