package org.test


import java.io.{File,FileOutputStream}
import java.net.URL
import org.opends.server.types.DirectoryEnvironmentConfig
import org.opends.server.util.EmbeddedUtils
import org.opends.messages.Message
import org.apache.commons.io.IOUtils.copy
import org.joda.time.{DateTime,Duration}

case class DirTree(root:File,config:File,schema:File,lock:File)

object Main {

  val rootDirPath = "/tmp/opends/"
  val masterConfig = "config.ldif"
  val configNames = "admin-backend.ldif" :: Nil
  val schemaNames = 
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
    val startTime = new DateTime
    
    EmbeddedUtils.startServer(config)
    
    println("post startServer")
    if(EmbeddedUtils.isRunning) {
      println("Started")
      EmbeddedUtils.stopServer(this.getClass.getName, Message.fromObject("Stoping server"))
      if(EmbeddedUtils.isRunning) println("Not stopped correctly")
      else println("Stopped")
    } else println("Not started correctly")
    
    println("Elapsed: " + (new Duration(startTime,new DateTime)))
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
    val masterConfigFile = new File(rootDir,masterConfig)
    
    //lock directory
    lockDir.mkdirs
    //config path
    for(name <- masterConfig :: configNames) {
      withFOS(new File(rootDir,name)) { fos =>
        copy(this.getClass.getClassLoader.getResourceAsStream("opends/" + name), fos)
      }
    }
    
    //schemas
    schemaDir.mkdirs
    for(name <- schemaNames) {
      withFOS(new File(schemaDir, name)) { fos =>
        copy(this.getClass.getClassLoader.getResourceAsStream("opends/schema/" + name), fos)
      }
    }
    
    new File(rootDir, "logs").mkdir
    new File(rootDir, "config").mkdir
    
    DirTree(rootDir,masterConfigFile, schemaDir,lockDir)
  }
}