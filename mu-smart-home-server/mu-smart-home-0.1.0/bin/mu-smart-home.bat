@REM mu-smart-home launcher script
@REM
@REM Environment:
@REM JAVA_HOME - location of a JDK home dir (optional if java on path)
@REM CFG_OPTS  - JVM options (optional)
@REM Configuration:
@REM MU_SMART_HOME_config.txt found in the MU_SMART_HOME_HOME.
@setlocal enabledelayedexpansion

@echo off


if "%MU_SMART_HOME_HOME%"=="" (
  set "APP_HOME=%~dp0\\.."

  rem Also set the old env name for backwards compatibility
  set "MU_SMART_HOME_HOME=%~dp0\\.."
) else (
  set "APP_HOME=%MU_SMART_HOME_HOME%"
)

set "APP_LIB_DIR=%APP_HOME%\lib\"

rem Detect if we were double clicked, although theoretically A user could
rem manually run cmd /c
for %%x in (!cmdcmdline!) do if %%~x==/c set DOUBLECLICKED=1

rem FIRST we load the config file of extra options.
set "CFG_FILE=%APP_HOME%\MU_SMART_HOME_config.txt"
set CFG_OPTS=
call :parse_config "%CFG_FILE%" CFG_OPTS

rem We use the value of the JAVACMD environment variable if defined
set _JAVACMD=%JAVACMD%

if "%_JAVACMD%"=="" (
  if not "%JAVA_HOME%"=="" (
    if exist "%JAVA_HOME%\bin\java.exe" set "_JAVACMD=%JAVA_HOME%\bin\java.exe"
  )
)

if "%_JAVACMD%"=="" set _JAVACMD=java

rem Detect if this java is ok to use.
for /F %%j in ('"%_JAVACMD%" -version  2^>^&1') do (
  if %%~j==java set JAVAINSTALLED=1
  if %%~j==openjdk set JAVAINSTALLED=1
)

rem BAT has no logical or, so we do it OLD SCHOOL! Oppan Redmond Style
set JAVAOK=true
if not defined JAVAINSTALLED set JAVAOK=false

if "%JAVAOK%"=="false" (
  echo.
  echo A Java JDK is not installed or can't be found.
  if not "%JAVA_HOME%"=="" (
    echo JAVA_HOME = "%JAVA_HOME%"
  )
  echo.
  echo Please go to
  echo   http://www.oracle.com/technetwork/java/javase/downloads/index.html
  echo and download a valid Java JDK and install before running mu-smart-home.
  echo.
  echo If you think this message is in error, please check
  echo your environment variables to see if "java.exe" and "javac.exe" are
  echo available via JAVA_HOME or PATH.
  echo.
  if defined DOUBLECLICKED pause
  exit /B 1
)


rem We use the value of the JAVA_OPTS environment variable if defined, rather than the config.
set _JAVA_OPTS=%JAVA_OPTS%
if "!_JAVA_OPTS!"=="" set _JAVA_OPTS=!CFG_OPTS!

rem We keep in _JAVA_PARAMS all -J-prefixed and -D-prefixed arguments
rem "-J" is stripped, "-D" is left as is, and everything is appended to JAVA_OPTS
set _JAVA_PARAMS=
set _APP_ARGS=

set "APP_CLASSPATH=%APP_LIB_DIR%\com.47deg.mu-smart-home-server-0.1.0.jar;%APP_LIB_DIR%\com.47deg.mu-smart-home-protocol-0.1.0.jar;%APP_LIB_DIR%\com.47deg.mu-smart-home-shared-0.1.0.jar;%APP_LIB_DIR%\org.scala-lang.scala-library-2.12.8.jar;%APP_LIB_DIR%\org.typelevel.cats-effect_2.12-1.2.0.jar;%APP_LIB_DIR%\org.typelevel.machinist_2.12-0.6.6.jar;%APP_LIB_DIR%\org.scala-lang.scala-reflect-2.12.8.jar;%APP_LIB_DIR%\com.github.pureconfig.pureconfig_2.12-0.10.2.jar;%APP_LIB_DIR%\com.github.pureconfig.pureconfig-core_2.12-0.10.2.jar;%APP_LIB_DIR%\com.github.pureconfig.pureconfig-macros_2.12-0.10.2.jar;%APP_LIB_DIR%\com.typesafe.config-1.3.3.jar;%APP_LIB_DIR%\com.github.pureconfig.pureconfig-generic_2.12-0.10.2.jar;%APP_LIB_DIR%\com.chuusai.shapeless_2.12-2.3.3.jar;%APP_LIB_DIR%\org.typelevel.macro-compat_2.12-1.1.1.jar;%APP_LIB_DIR%\ch.qos.logback.logback-classic-1.2.3.jar;%APP_LIB_DIR%\ch.qos.logback.logback-core-1.2.3.jar;%APP_LIB_DIR%\org.slf4j.slf4j-api-1.7.25.jar;%APP_LIB_DIR%\io.chrisdavenport.log4cats-core_2.12-0.3.0.jar;%APP_LIB_DIR%\io.chrisdavenport.log4cats-slf4j_2.12-0.3.0.jar;%APP_LIB_DIR%\org.typelevel.cats-core_2.12-1.6.0.jar;%APP_LIB_DIR%\org.typelevel.cats-macros_2.12-1.6.0.jar;%APP_LIB_DIR%\org.typelevel.cats-kernel_2.12-1.6.0.jar;%APP_LIB_DIR%\com.permutive.fs2-google-pubsub-grpc_2.12-0.12.0.jar;%APP_LIB_DIR%\co.fs2.fs2-core_2.12-1.0.4.jar;%APP_LIB_DIR%\org.scala-lang.modules.scala-collection-compat_2.12-0.3.0.jar;%APP_LIB_DIR%\org.scodec.scodec-bits_2.12-1.1.9.jar;%APP_LIB_DIR%\com.google.cloud.google-cloud-pubsub-1.71.0.jar;%APP_LIB_DIR%\com.google.cloud.google-cloud-core-1.71.0.jar;%APP_LIB_DIR%\com.google.guava.guava-27.1-android.jar;%APP_LIB_DIR%\com.google.guava.failureaccess-1.0.1.jar;%APP_LIB_DIR%\com.google.guava.listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_LIB_DIR%\com.google.code.findbugs.jsr305-3.0.2.jar;%APP_LIB_DIR%\org.checkerframework.checker-compat-qual-2.5.2.jar;%APP_LIB_DIR%\com.google.j2objc.j2objc-annotations-1.1.jar;%APP_LIB_DIR%\org.codehaus.mojo.animal-sniffer-annotations-1.17.jar;%APP_LIB_DIR%\com.google.http-client.google-http-client-1.29.0.jar;%APP_LIB_DIR%\org.apache.httpcomponents.httpclient-4.5.5.jar;%APP_LIB_DIR%\org.apache.httpcomponents.httpcore-4.4.9.jar;%APP_LIB_DIR%\commons-logging.commons-logging-1.2.jar;%APP_LIB_DIR%\commons-codec.commons-codec-1.10.jar;%APP_LIB_DIR%\io.opencensus.opencensus-contrib-http-util-0.18.0.jar;%APP_LIB_DIR%\com.google.api.api-common-1.7.0.jar;%APP_LIB_DIR%\com.google.api.gax-1.44.0.jar;%APP_LIB_DIR%\org.threeten.threetenbp-1.3.3.jar;%APP_LIB_DIR%\com.google.auth.google-auth-library-oauth2-http-0.15.0.jar;%APP_LIB_DIR%\com.google.auth.google-auth-library-credentials-0.15.0.jar;%APP_LIB_DIR%\com.google.http-client.google-http-client-jackson2-1.29.0.jar;%APP_LIB_DIR%\com.fasterxml.jackson.core.jackson-core-2.9.6.jar;%APP_LIB_DIR%\io.opencensus.opencensus-api-0.19.2.jar;%APP_LIB_DIR%\com.google.protobuf.protobuf-java-util-3.7.0.jar;%APP_LIB_DIR%\com.google.protobuf.protobuf-java-3.7.0.jar;%APP_LIB_DIR%\com.google.errorprone.error_prone_annotations-2.3.2.jar;%APP_LIB_DIR%\com.google.code.gson.gson-2.7.jar;%APP_LIB_DIR%\com.google.api.grpc.proto-google-common-protos-1.15.0.jar;%APP_LIB_DIR%\com.google.api.grpc.proto-google-iam-v1-0.12.0.jar;%APP_LIB_DIR%\javax.annotation.javax.annotation-api-1.3.2.jar;%APP_LIB_DIR%\com.google.cloud.google-cloud-core-grpc-1.71.0.jar;%APP_LIB_DIR%\io.grpc.grpc-protobuf-1.19.0.jar;%APP_LIB_DIR%\io.grpc.grpc-core-1.19.0.jar;%APP_LIB_DIR%\io.grpc.grpc-context-1.19.0.jar;%APP_LIB_DIR%\io.opencensus.opencensus-contrib-grpc-metrics-0.19.2.jar;%APP_LIB_DIR%\io.grpc.grpc-protobuf-lite-1.19.0.jar;%APP_LIB_DIR%\io.grpc.grpc-netty-shaded-1.19.0.jar;%APP_LIB_DIR%\io.grpc.grpc-stub-1.19.0.jar;%APP_LIB_DIR%\io.grpc.grpc-auth-1.19.0.jar;%APP_LIB_DIR%\com.google.api.gax-grpc-1.44.0.jar;%APP_LIB_DIR%\io.grpc.grpc-alts-1.19.0.jar;%APP_LIB_DIR%\io.grpc.grpc-grpclb-1.19.0.jar;%APP_LIB_DIR%\org.apache.commons.commons-lang3-3.5.jar;%APP_LIB_DIR%\com.google.api.grpc.proto-google-cloud-pubsub-v1-1.53.0.jar;%APP_LIB_DIR%\com.google.api.grpc.grpc-google-cloud-pubsub-v1-1.53.0.jar;%APP_LIB_DIR%\com.permutive.fs2-google-pubsub_2.12-0.12.0.jar;%APP_LIB_DIR%\io.higherkindness.mu-rpc-server_2.12-0.18.0.jar;%APP_LIB_DIR%\io.higherkindness.mu-common_2.12-0.18.0.jar;%APP_LIB_DIR%\io.higherkindness.mu-rpc-internal-core_2.12-0.18.0.jar;%APP_LIB_DIR%\org.scala-lang.scala-compiler-2.12.8.jar;%APP_LIB_DIR%\org.scala-lang.modules.scala-xml_2.12-1.0.6.jar;%APP_LIB_DIR%\com.47deg.pbdirect_2.12-0.2.1.jar;%APP_LIB_DIR%\com.sksamuel.avro4s.avro4s-core_2.12-1.8.4.jar;%APP_LIB_DIR%\com.sksamuel.avro4s.avro4s-macros_2.12-1.8.4.jar;%APP_LIB_DIR%\org.apache.avro.avro-1.8.2.jar;%APP_LIB_DIR%\org.codehaus.jackson.jackson-core-asl-1.9.13.jar;%APP_LIB_DIR%\org.codehaus.jackson.jackson-mapper-asl-1.9.13.jar;%APP_LIB_DIR%\com.thoughtworks.paranamer.paranamer-2.7.jar;%APP_LIB_DIR%\org.xerial.snappy.snappy-java-1.1.1.3.jar;%APP_LIB_DIR%\org.apache.commons.commons-compress-1.8.1.jar;%APP_LIB_DIR%\org.tukaani.xz-1.5.jar;%APP_LIB_DIR%\org.log4s.log4s_2.12-1.7.0.jar;%APP_LIB_DIR%\io.grpc.grpc-netty-1.18.0.jar;%APP_LIB_DIR%\io.netty.netty-codec-http2-4.1.32.Final.jar;%APP_LIB_DIR%\io.netty.netty-codec-http-4.1.32.Final.jar;%APP_LIB_DIR%\io.netty.netty-codec-4.1.32.Final.jar;%APP_LIB_DIR%\io.netty.netty-transport-4.1.32.Final.jar;%APP_LIB_DIR%\io.netty.netty-buffer-4.1.32.Final.jar;%APP_LIB_DIR%\io.netty.netty-common-4.1.32.Final.jar;%APP_LIB_DIR%\io.netty.netty-resolver-4.1.32.Final.jar;%APP_LIB_DIR%\io.netty.netty-handler-4.1.32.Final.jar;%APP_LIB_DIR%\io.netty.netty-handler-proxy-4.1.32.Final.jar;%APP_LIB_DIR%\io.netty.netty-codec-socks-4.1.32.Final.jar;%APP_LIB_DIR%\io.higherkindness.mu-rpc-fs2_2.12-0.18.0.jar;%APP_LIB_DIR%\io.higherkindness.mu-rpc-channel_2.12-0.18.0.jar;%APP_LIB_DIR%\io.higherkindness.mu-rpc-internal-fs2_2.12-0.18.0.jar;%APP_LIB_DIR%\org.lyranthe.fs2-grpc.java-runtime_2.12-0.4.0-M6.jar;%APP_LIB_DIR%\io.circe.circe-core_2.12-0.11.1.jar;%APP_LIB_DIR%\io.circe.circe-numbers_2.12-0.11.1.jar;%APP_LIB_DIR%\io.circe.circe-generic_2.12-0.11.1.jar;%APP_LIB_DIR%\io.circe.circe-parser_2.12-0.11.1.jar;%APP_LIB_DIR%\io.circe.circe-jawn_2.12-0.11.1.jar;%APP_LIB_DIR%\org.typelevel.jawn-parser_2.12-0.14.1.jar"
set "APP_MAIN_CLASS=com.fortysevendeg.smarthome.server.app.ServerApp"
set "SCRIPT_CONF_FILE=%APP_HOME%\conf\application.ini"

rem if configuration files exist, prepend their contents to the script arguments so it can be processed by this runner
call :parse_config "%SCRIPT_CONF_FILE%" SCRIPT_CONF_ARGS

call :process_args %SCRIPT_CONF_ARGS% %%*

set _JAVA_OPTS=!_JAVA_OPTS! !_JAVA_PARAMS!

if defined CUSTOM_MAIN_CLASS (
    set MAIN_CLASS=!CUSTOM_MAIN_CLASS!
) else (
    set MAIN_CLASS=!APP_MAIN_CLASS!
)

rem Call the application and pass all arguments unchanged.
"%_JAVACMD%" !_JAVA_OPTS! !MU_SMART_HOME_OPTS! -cp "%APP_CLASSPATH%" %MAIN_CLASS% !_APP_ARGS!

@endlocal

exit /B %ERRORLEVEL%


rem Loads a configuration file full of default command line options for this script.
rem First argument is the path to the config file.
rem Second argument is the name of the environment variable to write to.
:parse_config
  set _PARSE_FILE=%~1
  set _PARSE_OUT=
  if exist "%_PARSE_FILE%" (
    FOR /F "tokens=* eol=# usebackq delims=" %%i IN ("%_PARSE_FILE%") DO (
      set _PARSE_OUT=!_PARSE_OUT! %%i
    )
  )
  set %2=!_PARSE_OUT!
exit /B 0


:add_java
  set _JAVA_PARAMS=!_JAVA_PARAMS! %*
exit /B 0


:add_app
  set _APP_ARGS=!_APP_ARGS! %*
exit /B 0


rem Processes incoming arguments and places them in appropriate global variables
:process_args
  :param_loop
  call set _PARAM1=%%1
  set "_TEST_PARAM=%~1"

  if ["!_PARAM1!"]==[""] goto param_afterloop


  rem ignore arguments that do not start with '-'
  if "%_TEST_PARAM:~0,1%"=="-" goto param_java_check
  set _APP_ARGS=!_APP_ARGS! !_PARAM1!
  shift
  goto param_loop

  :param_java_check
  if "!_TEST_PARAM:~0,2!"=="-J" (
    rem strip -J prefix
    set _JAVA_PARAMS=!_JAVA_PARAMS! !_TEST_PARAM:~2!
    shift
    goto param_loop
  )

  if "!_TEST_PARAM:~0,2!"=="-D" (
    rem test if this was double-quoted property "-Dprop=42"
    for /F "delims== tokens=1,*" %%G in ("!_TEST_PARAM!") DO (
      if not ["%%H"] == [""] (
        set _JAVA_PARAMS=!_JAVA_PARAMS! !_PARAM1!
      ) else if [%2] neq [] (
        rem it was a normal property: -Dprop=42 or -Drop="42"
        call set _PARAM1=%%1=%%2
        set _JAVA_PARAMS=!_JAVA_PARAMS! !_PARAM1!
        shift
      )
    )
  ) else (
    if "!_TEST_PARAM!"=="-main" (
      call set CUSTOM_MAIN_CLASS=%%2
      shift
    ) else (
      set _APP_ARGS=!_APP_ARGS! !_PARAM1!
    )
  )
  shift
  goto param_loop
  :param_afterloop

exit /B 0
