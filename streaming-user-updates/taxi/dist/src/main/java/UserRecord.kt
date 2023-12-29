import com.icycube.TypeNames.UserRecord
import com.icycube.users.FirstName
import com.icycube.users.LastName
import com.icycube.users.MediumPictureURL
import com.icycube.users.UUID
import com.icycube.users.Username
import lang.taxi.annotations.DataType

@DataType(
  value = UserRecord,
  imported = true
)
open class UserRecord(
  val id: UUID,
  val firstName: FirstName,
  val lastName: LastName,
  val username: Username,
  val picture: MediumPictureURL
)
