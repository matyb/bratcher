package org.bratcher
import spock.lang.*

class BratcherSpec extends Specification {
  def "curling file that exists"() {
    def commands = []

    given:
    def bratcher = new Bratcher(){
      def sh(cmd) { commands += cmd }
    }

    when:
    def response = bratcher.curl("",['branch'])

    then:
    [[returnStdout:true, script: 'curl -X GET \'\'']] == commands
  }

  def "curling file that exists on branch"() {
    def commands = []

    given:
    def bratcher = new Bratcher(){
      def sh(cmd) { commands += cmd }
    }

    when:
    bratcher.curl('https://github.com/name/repo/tree/$branch/file.txt', ['develop'])

    then:
    [[returnStdout: true, script: "curl -X GET 'https://github.com/name/repo/tree/develop/file.txt'"]] == commands
  }

}
