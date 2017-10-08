import spock.lang.*

class BratcherSpec extends Specification {
  def "curling file that exists"() {
    def commands = []

    given:
    def bratcher = new Bratcher(){
      def sh(cmd) {
        commands += cmd
        return "{}"
      }
    }

    when:
    def response = bratcher.curl("",['branch'])

    then:
    [[returnStdout:true, script: 'curl -f -X GET \'\'']] == commands
    '{}' == response
  }

  def "curling file that exists on head of branches"() {
    def commands = []

    given:
    def bratcher = new Bratcher(){
      def sh(cmd) {
        commands += cmd
        return "{}"
      }
    }

    when:
    def response = bratcher.curl('https://github.com/name/repo/tree/$branch/file.txt', ['develop'])

    then:
    [[returnStdout: true, script: "curl -f -X GET 'https://github.com/name/repo/tree/develop/file.txt'"]] == commands
    "{}" == response
  }

  def "curling file that exists in tail of branches"() {
    def commands = []
    def commandResponse = [[returnStdout: true, script: "curl -f -X GET 'https://github.com/name/repo/tree/develop/file.txt'"]: null,
                           [returnStdout: true, script: "curl -f -X GET 'https://github.com/name/repo/tree/master/file.txt'"]: "{}"]

    given:
    def bratcher = new Bratcher(){
      def sh(cmd) {
        commands += cmd
        return commandResponses[cmd].toString()
      }
      def echo = { x -> println x }
    }

    when:
    def response = bratcher.curl('https://github.com/name/repo/tree/$branch/file.txt', ['develop', 'master'])

    then:
    [[returnStdout: true, script: "curl -f -X GET 'https://github.com/name/repo/tree/develop/file.txt'"],
     [returnStdout: true, script: "curl -f -X GET 'https://github.com/name/repo/tree/master/file.txt'"]] == commands
  }

  def "curling file that exists in head of branches does not evaluate tail"() {
    def commands = []

    given:
    def bratcher = new Bratcher(){
      def sh(cmd) {
        commands += cmd
        return "{}"
      }
    }

    when:
    def response = bratcher.curl('https://github.com/name/repo/tree/$branch/file.txt', ['develop', 'master'])

    then:
    [[returnStdout: true, script: "curl -f -X GET 'https://github.com/name/repo/tree/develop/file.txt'"]] == commands
    "{}" == response
  }

  def "curling file that can't be found returns null"() {
    def commands = []

    given:
    def bratcher = new Bratcher(){
      def sh(cmd) {
        commands += cmd
        throw new RuntimeException()
      }
      def echo = { x -> println x }
    }

    when:
    def response = bratcher.curl('https://github.com/name/repo/tree/$branch/file.txt', ['develop'])

    then:
    [[returnStdout: true, script: "curl -f -X GET 'https://github.com/name/repo/tree/develop/file.txt'"]] == commands
    null == response
  }
}
