package controllers

import play.api._
import play.api.mvc.{Action, Controller}
import play.api.libs.ws.{WS, Response}
import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future}

import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import com.tinkerpop.rexster.client.{RexsterClient, RexsterClientFactory}
import scala.collection.immutable.HashMap
import com.tinkerpop.blueprints.{Direction, Edge, Vertex, Graph}
import com.thinkaurelius.titan.core.{TitanFactory, TitanGraph}
import play.api.libs.json.{Format, Reads, JsValue}
import com.typesafe.config.{Config, ConfigFactory}
import java.io.File

object Facebook extends Controller {

  val conf: Config = ConfigFactory.parseFile(new File("/etc/fifa14/config.properties"));
  val APP_ID = conf.getString("FB_APP_ID")
  val CLIENT_SECRET = conf.getString("FB_CLIENT_SECRET")
  val REDIRECT_URL = conf.getString("FB_REDIRECT_URL")
  val ACCESS_TOKEN = APP_ID + "|" + CLIENT_SECRET
  val FB_SCOPE="read_friendlists"
  val IS_FRIENDS_WITH = "is friends with"

  // def
  def fbLogin = Action {
    Ok(views.html.fbLogin())
  }

  def asynTest = Action.async {
    def feedUrl = "http://www.example.com"
    WS.url(feedUrl).get().map {
      response =>
        Ok("Feed title: " + (response.body))
    }
  }

  def facebookRedirect = Action {
    Redirect(s"https://www.facebook.com/dialog/oauth?client_id=$APP_ID&redirect_uri=$REDIRECT_URL&state=testvalue&scope=$FB_SCOPE")
  }

  def paramsFromValidationResponse(response: Response): Map[String, String] = {
    response.body.split("&")
      .toSeq.map {
      pair => {
        val lst = pair.split("=")
        (lst(0), lst(1))
      }
    }.toMap
  }

  def fbResponse = Action.async {
    request =>
      Logger.info("fbResponse - start");
      def code = request.getQueryString("code")
    Logger.info("code: "+code)

      // validate code and receive access token
      //GET https://graph.facebook.com/oauth/access_token?client_id={app-id}&redirect_uri={redirect-uri}&client_secret={app-secret}&code={code-parameter}
      // returns access_token={access-token}&expires={seconds-til-expiration}
      val validationResponse: Future[Response] =
        WS.url("https://graph.facebook.com/oauth/access_token")
          .withQueryString("client_id" -> APP_ID)
          .withQueryString("redirect_uri" -> REDIRECT_URL)
          .withQueryString("client_secret" -> CLIENT_SECRET)
          .withQueryString("code" -> code.get)
          .get()

      validationResponse.flatMap {
        response =>
          def paramsFromValidation = paramsFromValidationResponse(response)
        Logger.info("paramsFromValidation:"+paramsFromValidation)
          //GET graph.facebook.com/debug_token?input_token={token-to-inspect}&access_token={app-token-or-admin-token}
          val validationResponse2: Future[Response] = WS.url("https://graph.facebook.com/debug_token")
            .withQueryString("input_token" -> paramsFromValidation.get("access_token").get)
            .withQueryString("access_token" -> ACCESS_TOKEN)
            .get()

          validationResponse2.flatMap {
            response2 =>
              def userId = response2.json \ "data" \ "user_id"
            Logger.info(Json.stringify(response2.json))
              def getFriendsURL = "https://graph.facebook.com/" + userId + "/friends"
              WS.url(getFriendsURL)
                .withQueryString("access_token" -> ACCESS_TOKEN)
                .get().map {
                response =>
                  Logger.info("before Ok()")
                  val friendsJson = response.json
                  // TODO: blocking?
                  storeFriendsInTitan(Json.stringify(userId), friendsJson)
                  Ok(userId + "\n" + friendsJson)
              }
          }
      }
  }

  case class FBUser(name: String, id: String)
  object FBUser {
    implicit val fbUserFormat:Format[FBUser] = Json.format[FBUser]
  }


  def storeFriendsInTitan(userId: String, fbFriendsJson: JsValue) {
    val graph = TitanFactory.open("/tmp/my_graph");

    var userVertex = graph.getVertex(userId)
    // first check if userId exists
    if (userVertex==null) userVertex = graph.addVertex(userId)
    val friends:java.lang.Iterable[Vertex]  = userVertex.getVertices(Direction.OUT, IS_FRIENDS_WITH)
    // get friends list and verify with other list



    val people = (fbFriendsJson \ "data").validate[List[FBUser]].get
    people.foreach(person => {
      val v = graph.addVertex(person.id)
      v.setProperty("name", person.name)
      graph.addEdge(null, userVertex, v, IS_FRIENDS_WITH)
    })
    //    val a = graph.addVertex(null);
    //    val b = graph.addVertex(null);
    //    a.setProperty("name", "marko");
    //    b.setProperty("name", "peter");
    //    val e = graph.addEdge(null, a, b, "knows");
    //    e.setProperty("since", 2006);
    //    val result = e.getVertex(Direction.OUT).getProperty("name").toString + "--" + e.getLabel() + "-->" + e.getVertex(Direction.IN).getProperty("name")
    //
    graph.shutdown();
  }

  def testRexster = Action {
    //    val client= RexsterClientFactory.open("localhost", "graph")
    //    val result = client.execute("g.V.name").toArray()

    val graph = TitanFactory.open("/tmp/my_graph");
    val a = graph.addVertex(null);
    val b = graph.addVertex(null);
    a.setProperty("name", "marko");
    b.setProperty("name", "peter");
    val e = graph.addEdge(null, a, b, "knows");
    e.setProperty("since", 2006);
    val result = e.getVertex(Direction.OUT).getProperty("name").toString + "--" + e.getLabel() + "-->" + e.getVertex(Direction.IN).getProperty("name")

    graph.shutdown();

    //      def client = RexsterClientFactory.open("localhost", "graph");
    //      def results = client.execute("g=rexster.getGraph(\"graph\");g.v(4).map");
    //      def mapA = results.get(0);
    //      Logger.info("fbResponse - end");

    Ok("rexster test: " + result)
  }

}
