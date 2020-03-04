package com.github.chanwit.xq

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

@CompileStatic
class JsonInput {

    def query(Reader reader, String query) {
        def json = new JsonSlurper().parse(reader)
        return Eval.x(json, "x" + query)
    }

    def query(Reader reader, File file) {
        def json = new JsonSlurper().parse(reader)
        Binding b = new Binding()
        b.setVariable("json", json)
        GroovyShell sh = new GroovyShell(b)
        return sh.evaluate(file)
    }

}
