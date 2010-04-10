package org.test


import java.io.{File,FileOutputStream}
import java.net.URL
import org.opends.server.types.DirectoryEnvironmentConfig
import org.opends.server.util.EmbeddedUtils
import org.opends.messages.Message
import org.apache.commons.io.IOUtils.copy


case class DirTree(root:File,config:File,schema:File,lock:File)

object Main {

  val rootDirPath = "/tmp/opends/"
  val configPath = "config.ldif"
  val schemaPaths = 
    "00-core.ldif" ::
    "01-pwpolicy.ldif" ::
    "02-config.ldif" ::
    "03-changelog.ldif" ::
    "03-rfc2713.ldif" ::
    "03-rfc2714.ldif" ::
    "03-rfc2739.ldif" ::
    "03-rfc2926.ldif" ::
    "03-rfc3112.ldif" ::
    "03-rfc3712.ldif" ::
    "03-uddiv3.ldif" ::
    "04-rfc2307bis.ldif" ::
    "05-rfc4876.ldif" ::
    "05-solaris.ldif" ::
    "06-compat.ldif" ::
    Nil
    
  def main(args:Array[String]) {
    val DirTree(rootDir,configFile, schemaDir,lockDir) = initDirTree
    
    val config = new DirectoryEnvironmentConfig
    config.setServerRoot(rootDir)
    config.setSchemaDirectory(schemaDir)
    config.setLockDirectory(lockDir)
    config.setConfigFile(configFile)
    config.setMaintainConfigArchive(false)
    
    println("Hello world")

    
    EmbeddedUtils.startServer(config)
    if(EmbeddedUtils.isRunning) {
      println("Started")
      EmbeddedUtils.stopServer(this.getClass.getName, Message.fromObject("Stoping server"))
    } else println("Not started correctly")
  }
  
  private def initDirTree : DirTree = {
    val lockPath = "lock"
    val schemaPath = "schema"
  
    def withFOS(file:File)(f:FileOutputStream => Unit) = {
      var fos : FileOutputStream = null
      try {
        fos = new FileOutputStream(file)
        f(fos)
      } finally {
        if(null != fos) { fos.close }
      }
    }
    
    /*
     * copy everything on classpath:opends into
     * root directory
     */
    val rootDir = new File(rootDirPath)
    val lockDir = new File(rootDir,lockPath)
    val schemaDir = new File(rootDir,schemaPath)
    val config = new File(rootDir, configPath)
    
    //lock directory
    lockDir.mkdirs
    //config path
    withFOS(config) { fos =>
      copy(this.getClass.getClassLoader.getResourceAsStream("opends/" + configPath), fos)
    }
    
    //schemas
    schemaDir.mkdirs
    for {
      path <- schemaPaths
    } {
      withFOS(new File(schemaDir, path)) { fos =>
        copy(this.getClass.getClassLoader.getResourceAsStream("opends/schema/" + path), fos)
      }
    }
    
    new File(rootDir, "logs").mkdir
    new File(rootDir, "config").mkdir
    
    DirTree(rootDir,config, schemaDir,lockDir)
  }
}