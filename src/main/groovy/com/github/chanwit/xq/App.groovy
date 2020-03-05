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
        description = "Any Configuration Query")
class App implements Runnable {

    enum Output { DEFAULT, JSON, YAML, RAW }

    private CommandLine cmdline

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "0..1", heading="Input Format:\n")
    InputFormat inputFormat;

    static class InputFormat {
        @CommandLine.Option(names = ["--json"], description = "Read input as a JSON")
        boolean json

        @CommandLine.Option(names = ["--line"], description = "Read input line by line")
        boolean line

        @CommandLine.Option(names = ["--txt", "--text"], description = "Read input as a text")
        boolean text

        @CommandLine.Option(names = ["--yaml"], description = "Read input as a YAML")
        boolean yaml
    }

    @CommandLine.Parameters(index= "0", paramLabel = "<QUERY>", description = "A query string or a @filename.")
    private String query

    @CommandLine.Option(names = ["-o", "--output"], description = "output format candidates are: default, json, yaml, raw")
    Output output = Output.DEFAULT

    static void main(String[] args) {
        def app = new App()
        def cmdline = new CommandLine(app)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setExpandAtFiles(false)
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
        if(inputFormat.json) {
            processJson()
        } else if(inputFormat.yaml) {
            processYaml()
        } else if(inputFormat.line) {
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

    private void processYaml() {
        def root = new Yaml().load(System.in.newReader())
        def result = Eval.x(root, "x" + query)
        def outputMode = output
        if (output == Output.DEFAULT) {
            outputMode = Output.YAML
        }
        printOut(result, outputMode)
    }

    private void processJson() {
        JsonInput input = new JsonInput()
        Reader reader = System.in.newReader()
        Object result
        if (query.startsWith("@")) {
            def filename = (query - '@')
            result = input.query(reader, new File(filename))
        } else {
            result = input.query(reader, query)
        }
        Output outputMode = output
        if (output == Output.DEFAULT) {
            outputMode = Output.JSON
        }
        printOut(result, outputMode)
    }

}
