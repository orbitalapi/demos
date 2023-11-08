## Background
 * New social media offering - competing with twitter
 * 2 Kafka topics
   * Tweets
   * Analytics
 * Microservice - User information

Goal: Build a data feed for displaying our timeline on the UI


## Querying a stream

```
stream { UserStatusUpdateMessage }
```

## Enriching a stream against a microservice

```
import com.icycube.users.Username
stream { UserStatusUpdateMessage } as {
    id: MessageId
    message: UserStatus
   
   username: Username
   name : FirstName + " " + LastName
   picture: MediumPictureURL
}[]
```

## Caching HTTP Requests

Add: 

```
@Cache(connection = "localHzc")
```

## Merging streams

  * We have multiple streams that we need to join

## Streaming Analytics
```
stream { MessageAnalytics }

stream { UserStatusUpdateMessage | MessageAnalytics }

import com.icycube.users.Username
stream { UserStatusUpdateMessage | MessageAnalytics } as {
    id: MessageId
    message: UserStatus
    views: ViewCount
   
   username: Username
   name : FirstName + " " + LastName
   picture: MediumPictureURL
}[]
```

## State

 * 

```taxi
import com.icycube.users.Username

@Cache(connection = "localHzc")
@StateStore(connection = "localHzc")
stream { UserStatusUpdateMessage | MessageAnalytics } as {
    id: MessageId
    message: UserStatus
    views: coalesce(ViewCount,0) 
   
   username: Username
   name : FirstName + " " + LastName
   picture: MediumPictureURL
}[]
```

## Why is this valuable?

 * Stitching streams together fast
 * 
