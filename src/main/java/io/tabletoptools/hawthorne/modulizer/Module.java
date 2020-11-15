package io.tabletoptools.hawthorne.modulizer;

import io.tabletoptools.hawthorne.modulizer.annotation.Command;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Module {

    private Collection<Module> submodules = new ArrayList<>();
    private Collection<Class> commandClasses = new ArrayList<>();
    private boolean enabled = false;

    public Module() {

    }

    public void load() {
        onLoad();
    }

    public void unload() {
        onUnload();
        this.submodules.clear();
        this.commandClasses.clear();
    }

    public void enable() {
        if(this.enabled) return;
        onEnable();
        this.enabled = true;
    }

    public void disable() {
        if(!this.enabled) return;
        onDisable();
        this.enabled = false;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public Collection<Class> getCommandClasses() {
        return this.commandClasses;
    }

    public Collection<Module> getSubmodules() {
        return this.submodules;
    }

    protected <T extends CommandClass> void addCommandClasses(Collection<Class<T>> classes) {
        commandClasses.addAll(classes);
    }

    protected <T extends CommandClass> void addCommandClass(Class<T> clazz) {
        commandClasses.add(clazz);
    }

    public Module loadSubmodule(Module module, Boolean enable) {
        this.submodules.add(module);
        module.load();
        if(enable) module.enable();
        return this;
    }

    public Module loadSubmodule(Module module) {
        return this.loadSubmodule(module, true);
    }

    public abstract void onLoad();
    public abstract void onUnload();
    public abstract void onEnable();
    public abstract void onDisable();

    public List<MethodMatch> getMethodMatches(List<String> commands, int depth) {
        List<MethodMatch> matches = new ArrayList<>();
        if(!isEnabled()) return matches;

        this.getCommandClasses()
                .forEach(clazz -> {
                    if(!clazz.isAnnotationPresent(Command.class)) {
                        matches.addAll(getMatchesForClass(depth, clazz, getCommand(commands, depth)));
                    }
                    else if(clazz.isAnnotationPresent(Command.class)
                            && Arrays.stream(((Command) clazz
                            .getAnnotation(Command.class))
                            .value()
                    ).anyMatch(alias -> alias.equals(getCommand(commands, depth)))) {
                        matches.addAll(getMatchesForClass(depth + 1, clazz, getCommand(commands, depth + 1)));
                    }
                });

        if(getCommand(commands, depth) != null) this.getSubmodules().forEach(submodule -> {
            if(!submodule.getClass().isAnnotationPresent(Command.class)) matches.addAll(submodule.getMethodMatches(commands, depth));
            else if (Arrays.stream(submodule.getClass()
                    .getAnnotation(Command.class)
                    .value()
            ).anyMatch(alias -> alias.equals(getCommand(commands, depth)))) matches.addAll(submodule.getMethodMatches(commands, depth+1));
        });
        return matches;
    }

    private List<MethodMatch> getMatchesForClass(int depth, Class clazz, String command) {
        List<MethodMatch> matches = new ArrayList<>();
        Arrays.asList(clazz.getMethods()).forEach(method -> {
            if(method.isAnnotationPresent(Command.class) && Arrays
                    .stream(method.getAnnotation(Command.class).value())
                    .anyMatch(alias -> alias.equals(command) || "/".equals(alias))) {
                matches.add(new MethodMatch(this, method, depth));
            }
        });
        return matches;
    }

    private String getCommand(List<String> commands, Integer depth) {
        return commands.size() > depth ? commands.get(depth) : null;
    }

}
