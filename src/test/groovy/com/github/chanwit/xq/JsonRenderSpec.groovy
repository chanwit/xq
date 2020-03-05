package com.github.chanwit.xq

import spock.lang.Specification

class JsonRenderSpec extends Specification {

    def "test case 0001"() {
        given:
        def cmd = "bash -c test_cases/test_case_0001.sh"
        when:
        def result = cmd.execute().text
        then:
        assert result == '''
[settings.kubernetes]
api-server="https://endpoint"
cluster-certificate="cert_data"
cluster-name="cluster"
'''.strip()
    }

    def "issue 1"() {
        given:
        def cmd = "bash -c test_cases/issue_1.sh"
        when:
        def result = cmd.execute().text
        then:
        assert result == """
- command:
  - k8s
  - '1'
  - '2'
""".stripLeading()
    }

}
