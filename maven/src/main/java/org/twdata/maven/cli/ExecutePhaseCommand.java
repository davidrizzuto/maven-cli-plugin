package org.twdata.maven.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ExecutePhaseCommand implements Command {
    private final Set<String> modules;
    private final CommandCallBuilder commandCallBuilder;
    private final CommandCallRunner runner;
    private final CliConsole console;
    private final SortedSet<String> phasesAndProperties;

    public ExecutePhaseCommand(Set<String> modules, CommandCallBuilder commandCallBuilder,
            CommandCallRunner runner, CliConsole console) {
        this.modules = modules;
        this.commandCallBuilder = commandCallBuilder;
        this.runner = runner;
        this.console = console;

        SortedSet<String> set = new TreeSet<String>();
        set.add("clean");
        set.add("validate");
        set.add("generate-sources");
        set.add("generate-resources");
        set.add("test-compile");
        set.add("test");
        set.add("package");
        set.add("integration-test");
        set.add("install");
        set.add("deploy");
        set.add("site");
        set.add("site-deploy");
        set.add("-o"); // offline mode
        set.add("-N"); // don't recurse
        set.add("-S"); // skip tests
        phasesAndProperties = Collections.unmodifiableSortedSet(set);
    }

    public Set<String> getCommandNames() {
        return phasesAndProperties;
    }

    public boolean matchesRequest(String request) {
        for (String token : request.split(" ")) {
            if (!phasesAndProperties.contains(token) && !token.startsWith("-D")
                    && !token.startsWith("-P") && !matchesModules(token)) {
                return false;
            }
        }

        return true;
    }

    private boolean matchesModules(String token) {
        String regex = token.replaceAll("\\*", ".*");
        for (String module : modules) {
            if (module.matches(regex)) {
                return true;
            }
        }

        return false;
    }

    public boolean run(String request) {
        try {
            List<CommandCall> calls = new ArrayList<CommandCall>();
            calls = commandCallBuilder.parseCommand(request);

            for (CommandCall call : calls) {
                console.writeDebug("Executing: " + call);
                long start = System.currentTimeMillis();
                runner.executeCommand(call);
                long now = System.currentTimeMillis();
                console.writeInfo("Execution time: " + (now - start) + " ms");
            }
        } catch (IllegalArgumentException ex) {
            console.writeError("Invalid command: " + request);
        } finally {
            return true;
        }
    }
}
