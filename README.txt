Dependency analyzier plugin for maven similar to maven dependency plugin.

By default the plugin works only with the reactor projects so the dependencies won't be wrote to the dependency graph. So it's possible
to print a dependency graph only between the sub projects. If you would like to print also the extenral dependencies use the -Ddepanal.reactorOnly flag.

Goals:

depanal:graph -- create a dot (graphviz) or a mathxml (with yed namespaces) graph file to create dependency graph
depanal:list -- create a file with the absolute file paths of dependencies