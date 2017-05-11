package argelbargel.jenkins.plugins.modules;


import hudson.model.AbstractProject;
import hudson.model.Queue.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


class Registry {
    private static final Registry INSTANCE = new Registry();

    static Registry registry() {
        return INSTANCE;
    }

    private final Map<String, AbstractProject> modules;
    private final Map<String, Set<String>> dependencies;
    private final Map<String, AbstractProject> running;
    private final Map<AbstractProject, Collection<Item>> blocked;


    private Registry() {
        modules = new HashMap<>();
        dependencies = new HashMap<>();
        running = new HashMap<>();
        blocked = new HashMap<>();
    }

    void register(String name, AbstractProject project, Set<String> dependencies) {
        this.modules.put(name, project);
        this.dependencies.put(name, dependencies);
    }

    void unregister(String name) {
        modules.remove(name);
        dependencies.remove(name);
    }

    Set<String> modules() {
        return modules.keySet();
    }

    String module(AbstractProject project) {
        for (Map.Entry<String, AbstractProject> entry : modules.entrySet()) {
            if (entry.getValue().equals(project)) {
                return entry.getKey();
            }
        }

        return null;
    }

    AbstractProject project(String name) {
        return modules.get(name);
    }

    boolean moduleExists(String name) {
        return modules.containsKey(name);
    }

    Set<String> names() {
        Set<String> names = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
            names.add(entry.getKey());
            names.addAll(entry.getValue());
        }

        return names;
    }

    Set<String> downstream(String name) {
        Set<String> upstream = new HashSet<>();
        buildDownstream(upstream, name);
        return upstream;
    }

    private void buildDownstream(Set<String> upstream, String name) {
        for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
            String dep = entry.getKey();
            if (!upstream.contains(dep) && entry.getValue().contains(name)) {
                upstream.add(dep);
                buildDownstream(upstream, dep);
            }
        }
    }

    Set<String> upstream(String name) {
        Set<String> downstream = new HashSet<>();
        buildUpstream(downstream, name);
        return downstream;
    }

    private void buildUpstream(Set<String> downstream, String name) {
        if (dependencies.containsKey(name)) {
            for (String dep : dependencies.get(name)) {
                if (!downstream.contains(dep)) {
                    downstream.add(dep);
                    buildUpstream(downstream, dep);
                }
            }
        }
    }

    void start(String name, AbstractProject project) {
        running.put(name, project);
    }

    Collection<AbstractProject> running() {
        return running.values();
    }

    void stop(String name) {
        running.remove(name);
    }

    void block(AbstractProject<?, ?> project, Item item) {
        if (!blocked.containsKey(project)) {
            blocked.put(project, new HashSet<Item>());
        }

        blocked.get(project).add(item);
    }

    boolean isBlocked(AbstractProject project) {
        return blocked.containsKey(project);
    }

    Collection<Item> blocked(AbstractProject project) {
        return blocked.get(project);
    }

    void unblock(AbstractProject project, Item item) {
        Collection<Item> blockedItemsForProject = blocked.get(project);
        blockedItemsForProject.remove(item);
        if (blockedItemsForProject.isEmpty()) {
            blocked.remove(project);
        }
    }
}
