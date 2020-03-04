package com.github.chanwit.xq

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import org.codehaus.groovy.reflection.CachedClass
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.representer.Representer
import picocli.CommandLine

@CompileStatic
@CommandLine.Command(name = "xq",
        mixinStandardHelpOptions = true,
        version = "v0.1.0",
        description = "Configuration Query")
class App implements Runnable {

    enum Output { DEFAULT, JSON, YAML, RAW }

    private CommandLine cmdline

    @CommandLine.Option(names = ["--yaml"], description = "read input as yaml")
    boolean yaml

    @CommandLine.Option(names = ["--json"], description = "read input as json")
    boolean json

    @CommandLine.Option(names = ["--line"], description = "read input line by line")
    boolean line

    @CommandLine.Option(names = ["--txt", "--text"], description = "read input as text")
    boolean text

    @CommandLine.Parameters(arity = "1..1", paramLabel = "QUERY", description = "query or file")
    private String query

    @CommandLine.Option(names = ["-o", "--output"], description = "output format candidates are: default, json, yaml")
    Output output = Output.DEFAULT

    static void main(String[] args) {
        def app = new App()
        def cmdline = new CommandLine(app).setCaseInsensitiveEnumValuesAllowed(true)
        app.cmdline = cmdline
        cmdline.execute(args)
    }

    void printOut(Object output, Output mode) {
        if(mode == Output.JSON) {
            String text = JsonOutput.prettyPrint(JsonOutput.toJson(output))
            print(text)
        } else if(mode == Output.YAML) {
            DumperOptions options = new DumperOptions()
            options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            Representer rep = new Representer() {{ this.multiRepresenters.put(GString, this.representers.get(String)) }}
            Yaml yaml = new Yaml(rep, options)
            print(yaml.dump(output))
        } else if(mode == Output.RAW) {
            print(output)
        }
    }

    @Override
    void run() {
        if(json) {
            def json = new JsonSlurper().parse(System.in.newReader())
            def result
            // if starts with @, it's filename
            if (query.startsWith("@")) {
                def filename = (query - '@') + '.xq'
                Binding b = new Binding()
                b.setVariable("json", json)
                GroovyShell sh = new GroovyShell(b)
                result = sh.evaluate(new File(filename))
            } else {
                // it's an expression
                result = Eval.x(json, "x" + query)
            }

            def outputMode = output
            if(output == Output.DEFAULT) {
                outputMode = Output.JSON
            }
            printOut(result, outputMode)
        } else if(yaml) {
            def root = new Yaml().parse(System.in.newReader())
            def result = Eval.x(root, "x" + query)
            def outputMode = output
            if(output == Output.DEFAULT) {
                outputMode = Output.YAML
            }
            printOut(result, outputMode)
        } else if(line) {
            def r = System.in.newReader()
            def lines = r.lines().collect { line ->
                Eval.x(line, "x" + query).toString()
            }
            def outputMode = output
            if(output == Output.DEFAULT) {
                outputMode = Output.RAW
            }

            if(outputMode == Output.YAML) {
                def result
                // try loading it as YAML
                try {
                    result = new Yaml().load(lines.join(System.lineSeparator()))
                } catch(e) {/* it's not?. do nothing */}
                // try loading it again as JSON
                result = new JsonSlurper().parseText(lines.join(System.lineSeparator()))
                printOut(result, outputMode)
            } else if (outputMode == Output.JSON) {
                def result
                // try loading it as JSON
                try {
                    result = new JsonSlurper().parseText(lines.join(System.lineSeparator()))
                } catch(e) {/* it's not?. do nothing */}
                // try loading it again as YAML
                result = new Yaml().load(lines.join(System.lineSeparator()))
                printOut(result, outputMode)
            } else if (outputMode == Output.RAW) {
                printOut(lines.join(System.lineSeparator()), outputMode)
            }

        }
    }

}
