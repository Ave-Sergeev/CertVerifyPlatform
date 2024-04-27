package auth

import config.AppConfig
import zio.http.Header.Authorization
import zio.http.{Credentials, Request, Response}
import zio.{IO, TaskLayer, ZIO, ZLayer}

case class AuthServiceLive(
    config: AppConfig
) extends AuthService {

  override def validateAuth(request: Request): IO[Response, Boolean] = {

    val token = request.header(Authorization)

    token match {
      case Some(Authorization.Basic(userName, password)) => validateBasicAuth(Credentials(userName, password))
      case Some(Authorization.Bearer(token))             => validateBearerAuth(token.value.mkString)
      case _                                             => ZIO.succeed(false)
    }
  }

  private def validateBasicAuth(credentials: Credentials): IO[Response, Boolean] = {
    import util._

    val secretLogin    = config.basicAuth.login.secretToString
    val secretPassword = config.basicAuth.password.secretToString

    if (secretLogin.contains(credentials.uname) && secretPassword.contains(credentials.upassword.secretToString)) ZIO.succeed(true)
    else ZIO.succeed(false)
  }

  private def validateBearerAuth(token: String): IO[Response, Nothing] = {
    ZIO.fail(Response.notImplemented("Not implemented in the current version"))
  }
}

object AuthServiceLive {
  lazy val layer: TaskLayer[AuthService] = ZLayer {
    for {
      config <- AppConfig.get
      client = AuthServiceLive(config)
    } yield client
  }
}
