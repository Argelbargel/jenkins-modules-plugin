package argelbargel.jenkins.plugins.modules;


import hudson.model.AbstractProject;

import java.util.HashSet;
import java.util.Set;


class ModuleUtils {
    static Set<String> allNames() {
        Set<String> names = new HashSet<>();
        for (ModuleAction module : ModuleAction.all()) {
            names.add(module.getName());
            names.addAll(module.getDependencies());
        }

        return names;
    }

    static Set<String> allModules() {
        Set<ModuleAction> all = ModuleAction.all();
        Set<String> names = new HashSet<>(all.size());
        for (ModuleAction module : all) {
            names.add(module.getName());
        }

        return names;
    }

    static String findModule(AbstractProject project) {
        ModuleAction module = ModuleAction.get(project);
        return module != null ? module.getName() : null;
    }

    static AbstractProject findProject(String name) {
        ModuleAction module = ModuleAction.get(name);
        return module != null ? module.getProject() : null;
    }

    static boolean moduleExists(String name) {
        return ModuleAction.get(name) != null;
    }

    static Set<String> buildDownstream(String name) {
        Set<String> upstream = new HashSet<>();
        buildDownstream(upstream, name);
        return upstream;
    }

    private static void buildDownstream(Set<String> downstream, String name) {
        ModuleAction module = ModuleAction.get(name);
        if (module != null && !downstream.contains(module.getName()) && module.getDependencies().contains(name)) {
            downstream.add(module.getName());
            buildDownstream(downstream, module.getName());
        }
    }

    static Set<String> buildUpstream(String name) {
        Set<String> upstream = new HashSet<>();
        buildUpstream(upstream, name);
        return upstream;
    }

    private static void buildUpstream(Set<String> upstream, String name) {
        ModuleAction module = ModuleAction.get(name);
        if (module != null) {
            for (String dep : module.getDependencies()) {
                if (!upstream.contains(dep)) {
                    upstream.add(dep);
                    buildUpstream(upstream, dep);
                }
            }
        }
    }

    private ModuleUtils() { /* no instances allowed */ }
}
