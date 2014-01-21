import play.api.libs.json._

case class User(name: String, age: Int)
implicit val userFormat = Json.format[User]

val userList = List(User("Example 1", 20), User("Example 2", 42))
val users = Json.obj("users" -> userList)

