package argelbargel.jenkins.plugins.modules;


import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import jenkins.util.DirectedGraph;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import static java.util.Collections.emptyList;


/**
 * Maintains the build dependencies between {@link Job}s
 * for efficient dependency computation.
 * <p>
 * <p>
 * The "master" data of dependencies are owned/persisted/maintained by
 * individual {@link Job}s, but because of that, it's relatively
 * slow  to compute backward edges.
 * <p>
 * <p>
 * This class builds the complete bi-directional dependency graph
 * by collecting information from all {@link ModuleTrigger}s.
 * <p>
 * This is a simplified and adapted version of Jenkins own DependencyGraph for AbstractProjects
 *
 * @see hudson.model.AbstractProject
 * @see hudson.model.DependencyGraph
 */
public final class ModuleDependencyGraph {
    private static final Comparator<Dependency> COMPARE_DEPENDENCIES_BY_NAME = new Comparator<Dependency>() {
        public int compare(Dependency lhs, Dependency rhs) {
            int cmp = lhs.getUpstreamJob().getName().compareTo(rhs.getUpstreamJob().getName());
            return cmp != 0 ? cmp : lhs.getDownstreamJob().getName().compareTo(rhs.getDownstreamJob().getName());
        }
    };

    private static ModuleDependencyGraph instance = new ModuleDependencyGraph();
    private static final Object LOCK = new Object();

    @Initializer(after = InitMilestone.JOB_LOADED)
    @SuppressWarnings("WeakerAccess") // must be public to be detected as initializer
    public static void rebuild() {
        SecurityContext saveCtx = ACL.impersonate(ACL.SYSTEM);
        try {
            ModuleDependencyGraph graph = build(new ModuleDependencyGraph());

            synchronized (LOCK) {
                instance = graph;
            }
        } finally {
            SecurityContextHolder.setContext(saveCtx);
        }
    }

    private static ModuleDependencyGraph build(ModuleDependencyGraph graph) {
        for (ModuleAction module : ModuleAction.all()) {
            ModuleTrigger trigger = module.getTrigger();
            if (trigger != null) {
                trigger.buildDependencyGraph(module.getJob(), graph);
            }
        }

        graph.topologicalDagSort();

        return graph;
    }

    public static ModuleDependencyGraph get() {
        synchronized (LOCK) {
            return instance;
        }
    }


    private final Map<Job<?, ?>, Set<Dependency>> forward;
    private final Map<Job<?, ?>, Set<Dependency>> backward;
    private Comparator<Job<?, ?>> topologicalOrder;


    private ModuleDependencyGraph() {
        forward = new HashMap<>();
        backward = new HashMap<>();
    }

    public List<Dependency> getDownstreamDependencies(Job<?, ?> job) {
        if (!forward.containsKey(job)) {
            return emptyList();
        }

        List<Dependency> downstream = new ArrayList<>(forward.get(job));
        // Sort topologically
        Collections.sort(downstream, new Comparator<ModuleDependencyGraph.Dependency>() {
            public int compare(ModuleDependencyGraph.Dependency lhs, ModuleDependencyGraph.Dependency rhs) {
                // Swapping lhs/rhs to get reverse sort:
                return topologicalOrder.compare(rhs.getDownstreamJob(), lhs.getDownstreamJob());
            }
        });

        return downstream;
    }

    /**
     * Gets all the immediate downstream projects (IOW forward edges) of the given project.
     *
     * @return can be empty but never null.
     */
    public List<Job<?, ?>> getDownstream(Job<?, ?> job) {
        return get(forward, job, false);
    }

    /**
     * Gets all the immediate upstream projects (IOW backward edges) of the given project.
     *
     * @return can be empty but never null.
     */
    @SuppressWarnings("unused") // part of public API
    public List<Job<?, ?>> getUpstream(Job<?, ?> job) {
        return get(backward, job, true);
    }

    public boolean hasDownstream(Job<?, ?> job) {
        return hasDependencies(forward, job);
    }

    @SuppressWarnings("unused") // part of public API
    public boolean hasUpstream(Job<?, ?> job) {
        return hasDependencies(backward, job);
    }

    /**
     * Gets all the direct and indirect upstream dependencies of the given project.
     */
    public Set<Job<?, ?>> getTransitiveUpstream(Job<?, ?> src) {
        return getTransitive(backward, src, true);
    }

    /**
     * Gets all the direct and indirect downstream dependencies of the given project.
     */
    @SuppressWarnings("unused") // part of public API
    public Set<Job<?, ?>> getTransitiveDownstream(Job<?, ?> src) {
        return getTransitive(forward, src, false);
    }

    /**
     * Returns true if a project has a non-direct dependency to another project.
     * <p>
     * A non-direct dependency is a path of dependency "edge"s from the source to the destination,
     * where the length is greater than 1.
     */
    @SuppressWarnings("unused") // part of public API
    public boolean hasIndirectDependencies(Job<?, ?> src, Job<?, ?> dst) {
        Set<Job<?, ?>> visited = new HashSet<>();
        Stack<Job<?, ?>> queue = new Stack<>();

        queue.addAll(getDownstream(src));
        queue.remove(dst);

        while (!queue.isEmpty()) {
            Job<?, ?> job = queue.pop();
            if (job == dst) {
                return true;
            }
            if (visited.add(job)) {
                queue.addAll(getDownstream(job));
            }
        }

        return false;
    }

    /**
     * Called during the dependency graph build phase to add a dependency edge.
     */
    void addDependency(Dependency dep) {
        add(forward, dep.getUpstreamJob(), dep);
        add(backward, dep.getDownstreamJob(), dep);
    }

    private boolean hasDependencies(Map<Job<?, ?>, Set<Dependency>> map, Job<?, ?> job) {
        return map.containsKey(job);
    }

    private List<Job<?, ?>> get(Map<Job<?, ?>, Set<Dependency>> map, Job<?, ?> src, boolean up) {
        if (!map.containsKey(src)) {
            return emptyList();
        }

        Set<Dependency> v = map.get(src);
        List<Job<?, ?>> result = new ArrayList<>(v.size());
        for (Dependency d : v) {
            result.add(up ? d.getUpstreamJob() : d.getDownstreamJob());
        }

        return result;
    }


    private Set<Job<?, ?>> getTransitive(Map<Job<?, ?>, Set<Dependency>> direction, Job<?, ?> src, boolean up) {
        Set<Job<?, ?>> visited = new HashSet<>();
        Stack<Job<?, ?>> queue = new Stack<>();

        queue.add(src);

        while (!queue.isEmpty()) {
            Job<?, ?> job = queue.pop();

            for (Job<?, ?> child : get(direction, job, up)) {
                if (visited.add(child)) {
                    queue.add(child);
                }
            }
        }

        return visited;
    }

    private void add(Map<Job<?, ?>, Set<Dependency>> map, Job<?, ?> key, Dependency dep) {
        Set<Dependency> set = map.get(key);
        if (set == null) {
            set = new TreeSet<>(COMPARE_DEPENDENCIES_BY_NAME);
            map.put(key, set);
        }

        set.add(dep);
    }


    private void topologicalDagSort() {
        DirectedGraph<Job<?, ?>> g = new DirectedGraph<Job<?, ?>>() {
            @Override
            protected Collection<Job<?, ?>> nodes() {
                final Set<Job<?, ?>> nodes = new HashSet<>();
                nodes.addAll(forward.keySet());
                nodes.addAll(backward.keySet());
                return nodes;
            }

            @Override
            protected Collection<Job<?, ?>> forward(Job<?, ?> node) {
                return getDownstream(node);
            }
        };

        List<DirectedGraph.SCC<Job<?, ?>>> sccs = g.getStronglyConnectedComponents();

        final Map<Job<?, ?>, Integer> topoOrder = new HashMap<>();
        int idx = 0;
        for (DirectedGraph.SCC<Job<?, ?>> scc : sccs) {
            for (Job<?, ?> n : scc) {
                topoOrder.put(n, idx++);
            }
        }

        topologicalOrder = new Comparator<Job<?, ?>>() {
            @Override
            public int compare(Job<?, ?> o1, Job<?, ?> o2) {
                return topoOrder.get(o1) - topoOrder.get(o2);
            }
        };
    }


    /**
     * Represents an edge in the dependency graph.
     *
     * @since 1.341
     */
    public static abstract class Dependency {
        private final Job<?, ?> upstream;
        private final Job<?, ?> downstream;

        Dependency(Job<?, ?> upstream, Job<?, ?> downstream) {
            this.upstream = upstream;
            this.downstream = downstream;
        }

        @SuppressWarnings("WeakerAccess") // part of public API
        public Job<?, ?> getUpstreamJob() {
            return upstream;
        }

        public Job<?, ?> getDownstreamJob() {
            return downstream;
        }

        /**
         * Decide whether build should be triggered and provide any Actions for the build.
         * Subclasses must override to control how/if the build is triggered.
         * <p>The authentication in effect ({@link Jenkins#getAuthentication}) will be that of the upstream build.
         * An implementation is expected to perform any relevant access control checks:
         * that an upstream project can both see and build a downstream project,
         * or that a downstream project can see an upstream project.
         *
         * @param run      run of upstream project that just completed
         * @param listener For any error/log output
         * @param actions  Add Actions for the triggered build to this list; never null
         * @return True to trigger a build of the downstream project
         */
        public abstract boolean shouldTriggerBuild(Run<?, ?> run, TaskListener listener, List<Action> actions);

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }

            final Dependency that = (Dependency) obj;
            return this.upstream == that.upstream || this.downstream == that.downstream;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + this.upstream.hashCode();
            hash = 23 * hash + this.downstream.hashCode();
            return hash;
        }

        @Override
        public String toString() {
            return super.toString() + "[" + upstream + "->" + downstream + "]";
        }
    }
}
