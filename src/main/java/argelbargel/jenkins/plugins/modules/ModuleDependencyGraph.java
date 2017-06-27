package argelbargel.jenkins.plugins.modules;


import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Job;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import jenkins.util.DirectedGraph;

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
import static java.util.Collections.singleton;


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
    private static final Comparator<ModuleDependency> COMPARE_DEPENDENCIES_BY_NAME = new Comparator<ModuleDependency>() {
        public int compare(ModuleDependency lhs, ModuleDependency rhs) {
            int cmp = compare(lhs.getUpstreamJob(), rhs.getUpstreamJob());
            return cmp != 0 ? cmp : compare(lhs.getDownstreamJob(), rhs.getDownstreamJob());
        }

        private int compare(Job lhs, Job rhs) {
            if (lhs == null) {
                return rhs == null ? 0 : -1;
            } else if (rhs == null) {
                return 1;
            }

            return lhs.getName().compareTo(rhs.getName());
        }
    };

    private static ModuleDependencyGraph instance = new ModuleDependencyGraph();
    private static final Object LOCK = new Object();

    @Initializer(after = InitMilestone.JOB_LOADED)
    @SuppressWarnings("WeakerAccess") // must be public to be detected as initializer
    public static void rebuild() {
        ACL.impersonate(ACL.SYSTEM, new Runnable() {
            @Override
            public void run() {
                ModuleDependencyGraph graph = build(new ModuleDependencyGraph());

                synchronized (LOCK) {
                    instance = graph;
                }
            }
        });
    }

    private static ModuleDependencyGraph build(ModuleDependencyGraph graph) {
        for (Job job : Jenkins.getInstance().getAllItems(Job.class)) {
            ModuleTrigger trigger = ModuleTrigger.get(job);
            if (trigger != null) {
                trigger.addUpstreamDependencies(graph, job);
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


    private final Map<Job, Set<ModuleDependency>> forward;
    private final Map<Job, Set<ModuleDependency>> backward;
    private Comparator<Job<?, ?>> topologicalOrder;


    private ModuleDependencyGraph() {
        forward = new HashMap<>();
        backward = new HashMap<>();
    }

    public Collection<Job> getRoots(Job job) {
        List<Job> roots = new ArrayList<>();
        for (Job<?, ?> upstream : getTransitiveUpstream(job)) {
            if (!backward.containsKey(upstream)) {
                roots.add(upstream);
            }
        }

        if (roots.isEmpty()) {
            return singleton(job);
        }


        Collections.sort(roots, new Comparator<Job>() {
            public int compare(Job lhs, Job rhs) {
                return getTransitiveDownstream(rhs).size() - getTransitiveDownstream(lhs).size();
            }
        });

        return roots;
    }

    public List<ModuleDependency> getDownstreamDependencies(Job<?, ?> job) {
        if (!forward.containsKey(job)) {
            return emptyList();
        }

        List<ModuleDependency> downstream = new ArrayList<>(forward.get(job));
        // Sort topologically
        Collections.sort(downstream, new Comparator<ModuleDependency>() {
            public int compare(ModuleDependency lhs, ModuleDependency rhs) {
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
    public List<Job> getDownstream(Job job) {
        return get(forward, job, false);
    }

    /**
     * Gets all the immediate upstream projects (IOW backward edges) of the given project.
     *
     * @return can be empty but never null.
     */
    @SuppressWarnings({"unused", "WeakerAccess"}) // part of public API
    public List<Job> getUpstream(Job job) {
        return get(backward, job, true);
    }

    @SuppressWarnings("unused") // part of public API
    public boolean hasDownstream(Job job) {
        return hasDependencies(forward, job);
    }

    @SuppressWarnings("unused") // part of public API
    public boolean hasUpstream(Job job) {
        return hasDependencies(backward, job);
    }

    /**
     * Gets all the direct and indirect upstream dependencies of the given project.
     */
    public Set<Job> getTransitiveUpstream(Job src) {
        return getTransitive(backward, src, true);
    }

    /**
     * Gets all the direct and indirect downstream dependencies of the given project.
     */
    @SuppressWarnings({"unused", "WeakerAccess"}) // part of public API
    public Set<Job> getTransitiveDownstream(Job src) {
        return getTransitive(forward, src, false);
    }

    /**
     * returns {@code true} when the given jobs have a dependency, whether direct or indirect
     */
    public boolean hasDependency(Job src, Job dst) {
        return getTransitiveUpstream(dst).contains(src) || getTransitiveDownstream(dst).contains(src);
    }

    /**
     * Returns true if a project has a non-direct dependency to another project.
     * <p>
     * A non-direct dependency is a path of dependency "edge"s from the source to the destination,
     * where the length is greater than 1.
     */
    @SuppressWarnings("unused") // part of public API
    public boolean hasIndirectDependencies(Job src, Job dst) {
        Set<Job> visited = new HashSet<>();
        Stack<Job> queue = new Stack<>();

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
    void addDependency(ModuleDependency dep) {
        add(forward, dep.getUpstreamJob(), dep);
        add(backward, dep.getDownstreamJob(), dep);
    }

    private boolean hasDependencies(Map<Job, Set<ModuleDependency>> map, Job job) {
        return map.containsKey(job);
    }

    private List<Job> get(Map<Job, Set<ModuleDependency>> map, Job src, boolean up) {
        if (!map.containsKey(src)) {
            return emptyList();
        }

        Set<ModuleDependency> v = map.get(src);
        List<Job> result = new ArrayList<>(v.size());
        for (ModuleDependency d : v) {
            result.add(up ? d.getUpstreamJob() : d.getDownstreamJob());
        }

        return result;
    }


    private Set<Job> getTransitive(Map<Job, Set<ModuleDependency>> direction, Job src, boolean up) {
        Set<Job> visited = new HashSet<>();
        Stack<Job> queue = new Stack<>();

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

    private void add(Map<Job, Set<ModuleDependency>> map, Job key, ModuleDependency dep) {
        Set<ModuleDependency> set = map.get(key);
        if (set == null) {
            set = new TreeSet<>(COMPARE_DEPENDENCIES_BY_NAME);
            map.put(key, set);
        }

        set.add(dep);
    }


    private void topologicalDagSort() {
        DirectedGraph<Job> g = new DirectedGraph<Job>() {
            @Override
            protected Collection<Job> nodes() {
                final Set<Job> nodes = new HashSet<>();
                nodes.addAll(forward.keySet());
                nodes.addAll(backward.keySet());
                return nodes;
            }

            @Override
            protected Collection<Job> forward(Job node) {
                return getDownstream(node);
            }
        };

        List<DirectedGraph.SCC<Job>> sccs = g.getStronglyConnectedComponents();

        final Map<Job, Integer> topoOrder = new HashMap<>();
        int idx = 0;
        for (DirectedGraph.SCC<Job> scc : sccs) {
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
}
