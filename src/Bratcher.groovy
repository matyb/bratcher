def String curl(url, branches, curlArgs = '-f -X GET', continueFn = { branch, exception -> true}){
  if(!branches?.empty){
    try {
      subUrl = url.replace('$branch', branches.head())
      def val = sh(returnStdout: true, script: "curl " + curlArgs + " '$subUrl'")
      return val
    } catch (Exception x) {
      if(continueFn(branches.head(), x)){
        echo """file not found on branch '${branches.head()}', 
               |trying next branch '${branches.tail().size() > 0 ? branches.tail().head() : null}'""".stripMargin().toString()
        curl(url, branches.tail(), curlArgs, continueFn)
      }
    }
  }
}
