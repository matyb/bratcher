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

  def "curls plain urls too"() {
    def commands = []

    given:
    def bratcher = new Bratcher(){
      def sh(cmd){
        commands += cmd
      }
    }

    when:
    bratcher.curl('https://someurl.com/whatever?youknow')

    then:
    println commands
    commands == [[returnStdout: true, script: "curl -f -X GET 'https://someurl.com/whatever?youknow'"]]
  }

  def "reuses repo, checks out branch, changes wd"() {
    def commands = []
    def responses = [[returnStdout: true, script: 'ls -la']: "drwxrwxr-- 1 meh meh    1234 Jan  3 12:34 git-repo",
                     [returnStatus: true, script: 'git checkout branch-found']: 0]
    def wds = ['git-repo']

    given:
    def bratcher = new Bratcher(){
      def sh(cmd) {
        commands += cmd
        return responses[cmd]
      }
      def echo = { x -> println x }
      def cwd = { wd ->
        assert wd == wds.pop()
      }
    }

    when:
    def response = bratcher.checkout('git-repo', ['branch-found'])

    then:
    commands == responses.keySet() as ArrayList
    wds == []
  }

  def "clones repo, checks out branch, changes wd"() {
    def commands = []
    def responses = [[returnStdout: true, script: 'ls -la']: 
                                                    ["drwxrwxr-- 1 meh meh    1234 Jan  3 12:34 git-repo",
                                                     "lrwxrwxr-- 1 meh meh    1234 Jan  3 12:34 not-git-repo"],
                     [returnStatus: true, script: 'git checkout branch-found']: [0],
                     'git clone https://github.com/git-repo': ['']]
    def wds = ['git-repo']

    given:
    def bratcher = new Bratcher(){
      def sh(cmd) {
        commands += cmd
        return responses[cmd].pop()
      }
      def echo = { x -> println x }
      def cwd = { wd ->
        assert wd == wds.pop()
      }
    }

    when:
    def response = bratcher.checkout('git-repo', ['branch-found'])

    then:
    commands == [[returnStdout: true, script: 'ls -la'], 
                 'git clone https://github.com/git-repo',
                 [returnStdout: true, script: 'ls -la'],
                 [returnStatus: true, script: 'git checkout branch-found']]
    wds == []
  }

  def "checks out branch until it finds one, changes wd"() {
    def commands = []
    def responses = [[returnStdout: true, script: 'ls -la']: "drwxrwxr-- 1 meh meh    1234 Jan  3 12:34 git-repo",
                     [returnStatus: true, script: 'git checkout branch-not-found']: 1,
                     [returnStatus: true, script: 'git checkout branch-found']: 0 ]

    def wds = ['git-repo']

    given:
    def bratcher = new Bratcher(){
      def sh(cmd) {
        commands += cmd
        println "cmd: !!!! $cmd"
        return responses[cmd]
      }
      def echo = { x -> println x }
      def cwd = { wd ->
        assert wd == wds.pop()
      }
    }

    when:
    def response = bratcher.checkout('git-repo', ['branch-not-found', 'branch-found'])

    then:
    commands == [[returnStdout: true, script: 'ls -la'], 
                 [returnStatus: true, script: 'git checkout branch-not-found'],
                 [returnStatus: true, script: 'git checkout branch-found']]
    wds == []
  }

  def "throws exception if no branch found"() {
    def commands = []
    def responses = [[returnStdout: true, script: 'ls -la']: "drwxrwxr-- 1 meh meh    1234 Jan  3 12:34 git-repo",
                     [returnStatus: true, script: 'git checkout branch-not-found']: 1 ]

    def wds = ['git-repo']

    given:
    def bratcher = new Bratcher(){
      def sh(cmd) {
        commands += cmd
        println "cmd: !!!! $cmd"
        return responses[cmd]
      }
      def echo = { x -> println x }
      def cwd = { wd ->
        assert wd == wds.pop()
      }
    }

    when:
    def response = bratcher.checkout('git-repo', ['branch-not-found'])

    then:
    commands == [[returnStdout: true, script: 'ls -la'], 
                 [returnStatus: true, script: 'git checkout branch-not-found']]
    thrown NoSuchElementException
  }

}
