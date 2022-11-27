package slimeknights.tconstruct.library.modifiers.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import lombok.RequiredArgsConstructor;
import slimeknights.tconstruct.library.modifiers.ModifierHook;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/** Map working with modifier hooks that automatically maps the objects to the correct generics. */
@RequiredArgsConstructor
public class ModifierHookMap {
  /** Instance with no modifiers */
  public static final ModifierHookMap EMPTY = new ModifierHookMap(Collections.emptyMap());

  /** Internal map of modifier hook to object. It's the caller's responsibility to make sure the object is valid for the hook */
  private final Map<ModifierHook<?>,Object> modules;

  /** Checks if a module is registered for the given hook */
  public boolean hasHook(ModifierHook<?> hook) {
    return modules.containsKey(hook);
  }

  /** Gets the module matching the given hook */
  @SuppressWarnings("unchecked")
  public <T> T getHook(ModifierHook<T> hook) {
    T object = (T)modules.get(hook);
    if (object != null) {
      return object;
    }
    return hook.getDefaultInstance();
  }

  @SuppressWarnings("UnusedReturnValue")
  public static class Builder {
    /** Builder for the final map */
    private final LinkedHashMultimap<ModifierHook<?>,Object> modules = LinkedHashMultimap.create();

    /** Adds a module to the builder */
    @SuppressWarnings("UnusedReturnValue")
    public <H, T extends H> Builder addHook(T object, ModifierHook<H> hook) {
      modules.put(hook, object);
      return this;
    }

    /** Adds a module to the builder that implements multiple hooks */
    public <T> Builder addHook(T object, ModifierHook<? super T> hook1, ModifierHook<? super T> hook2) {
      addHook(object, hook1);
      addHook(object, hook2);
      return this;
    }

    /** Adds a module to the builder that implements multiple hooks */
    public <T> Builder addHook(T object, ModifierHook<? super T> hook1, ModifierHook<? super T> hook2, ModifierHook<? super T> hook3) {
      addHook(object, hook1);
      addHook(object, hook2);
      addHook(object, hook3);
      return this;
    }

    /** Adds a module to the builder that implements multiple hooks */
    @SafeVarargs
    public final <T> Builder addHook(T object, ModifierHook<? super T>... hooks) {
      for (ModifierHook<? super T> hook : hooks) {
        addHook(object, hook);
      }
      return this;
    }

    /** Helper to deal with generics */
    @SuppressWarnings("unchecked")
    private static <T> void insert(ImmutableMap.Builder<ModifierHook<?>,Object> builder, ModifierHook<T> hook, Collection<Object> objects) {
      if (objects.size() == 1) {
        builder.put(hook, objects.iterator().next());
      } else if (!objects.isEmpty()) {
        builder.put(hook, hook.merge((Collection<T>)objects));
      }
    }

    /** Builds the final map */
    public ModifierHookMap build() {
      if (modules.isEmpty()) {
        return EMPTY;
      }
      ImmutableMap.Builder<ModifierHook<?>,Object> builder = ImmutableMap.builder();
      for (Entry<ModifierHook<?>,Collection<Object>> entry : modules.asMap().entrySet()) {
        insert(builder, entry.getKey(), entry.getValue());
      }
      return new ModifierHookMap(builder.build());
    }
  }
}