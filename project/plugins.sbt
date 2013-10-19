libraryDependencies <+= sbtVersion(v => v match {
  case "0.11.0" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.0-0.2.8"
  case "0.11.1" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.1-0.2.10"
  case "0.11.2" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.2-0.2.10"
  case "0.11.3" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.3-0.2.11.1"
  case "0.12.0" => "com.github.siasia" %% "xsbt-web-plugin" % "0.12.0-0.2.11.1"
  case "0.12.1" => "com.github.siasia" %% "xsbt-web-plugin" % "0.12.0-0.2.11.1"
})

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

//Enable the sbt idea plugin
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")

// GPG Sbt Plugin
addSbtPlugin("com.jsuereth" % "xsbt-gpg-plugin" % "0.6")
