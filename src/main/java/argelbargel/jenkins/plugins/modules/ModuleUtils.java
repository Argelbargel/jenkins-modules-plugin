package argelbargel.jenkins.plugins.modules;


import hudson.model.Job;

import java.util.HashSet;
import java.util.Set;


final class ModuleUtils {
    static Set<String> allModuleNames() {
        Set<String> names = new HashSet<>();
        for (ModuleAction module : ModuleAction.all()) {
            names.add(module.getModuleName());
            names.addAll(module.getDependencies());
        }

        return names;
    }

    static Set<String> allModuleNamesWithJobs() {
        Set<ModuleAction> all = ModuleAction.all();
        Set<String> names = new HashSet<>(all.size());
        for (ModuleAction module : all) {
            names.add(module.getModuleName());
        }

        return names;
    }

    static String findModule(Job<?, ?> job) {
        ModuleAction module = ModuleAction.get(job);
        return module != null ? module.getModuleName() : null;
    }

    static Job<?, ?> findProject(String name) {
        ModuleAction module = ModuleAction.get(name);
        return module != null ? module.getJob() : null;
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
        if (module != null && !downstream.contains(module.getModuleName()) && module.getDependencies().contains(name)) {
            downstream.add(module.getModuleName());
            buildDownstream(downstream, module.getModuleName());
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
