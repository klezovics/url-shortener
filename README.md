# Overview

This is a URL shortening application.
It is created with the use of `Java/SpringBoot` technology stack.

The solution implements the task + variation.
Which means that access statistics are collected for each unique user.

Specifically it uses ...
1) Spring Core 
2) Spring MVC + Thymeleaf
3) SpringData + Hibernate
4) Embedded H2 database
5) Spring security is used for authenticating users + control access to admin only area
6) Apache commons validation is used for URL validation 

**Please read the Notes section below to see the assumptions used during development**

# How to run the application
To run the application run the following command in the terminal: 

Linux & MacOS: `./mvnw spring-boot:run`

Windows: `./mvnw.cmd spring-boot:run`


To run the test suite:

Linux & MacOS: `./mvnw test`

Windows: `./mvnw.cmd test`


# How to use the application
Once the application is started it runs on an embedded Tomcat server.

In order to navigate to the main page go to: `www.localhost:8080/`
The UI is pretty intuitive from then on.

The app comes with two pre-configured users
1) user: spring password: spring
2) user: admin password: admin 

Non-authenticated users are not able to view the statistics
Authenticated users are able to view some of the statistics
Admin is able to view all the statistics

There is a special "Statistics" item on the navbar, which is only
visible to the admin. This can be accessed via URL `http://www.localhost:8080/admin/statistics`

To log in as a specific user, press "Login" on the navbar.
To log out press the "Logout" button on the navbar

For ease of testing (in particular of the admin panel) the 
database is populated with some data on startup via the `DemoDataStartupInitializer`
class.

The application comes with an absolute minimal tests suite, which covers
the most common uses cases and a minimal UI sufficient to satisfy the 
requirements.

# Data model 
The key aspect to understanding the application is the data model.
The data model is chosen in a way, which enables us to easily 
record and fetch the necessary statistics.

There are 3 main entities:

### FullUrl

This entity represents a full URL, which we want to shorten
i.e. `www.google.com`.

Since each logged in user gets their own shortened URL, there are 
many ShortenedUrl entities attached to a single FullUrl entity.

The total number of times a given full-URL was shortened is stored on this entity

### ShortenedUrl.

This entity represents a shortened URL. It is unique for each user.
This entity has a field called `ownerName`, which is set to the ID
of the user, which created it.

All anonymous users have a single shortened URL shared between them and 
the `ownerName` is set to `anonymous`

### ShortenedUrlAccessStats
This entity stores the access counts for a particular user and a shortened URL.
If an anonymous user uses a shortened URL a visit is recorded against 
a user with name "anonymous"

Many `ShortenedUrlAccessStats` relate to a single `ShortenedUrl`

To better demonstrate the data model demo data is injected in the 
`DemoDataStartupInitializer` class.

There is one full URL: www.google.com

It was shortened two times, once by user "admin" and once by user "spring".
Therefore, we have two shortened URLs.

For each of the shortened URLs there are different access statistics.
Shortened URL visits are counted per user. If a non-logged in user 
uses the URL, then the visit is counted towards the "anonymous" user.


# Architecture and design
This is a standard 3 layered application.

Controllers are used to process requests from users.

All the business logic is contained in the service class
"UrlShorteningService".

SpringData repositories + Hibernate are used to implement
the persistance layers. For easy of implementation and 
testing the H2 database is used. 

# Notes

The requirements for this challenge come with a specific time frame, 
therefore the app is somewhat minimalistic and contains some things 
which could be improved. 

Specifically these are ... 

1) The test coverage is very minimal and should be expanded. 
Specifically, the security configuration should be tested via the 
usage of MockMvc

2) The UI can be made better by simply applying some classes from the 
bootstrap library 

3) The Hibernate entity model could be improved by trying to take into 
account the expected load and potentially adding indexes to the key field

4) The shortened URL has the following format http://www.localhost:8080/e/{shortened-URL-key}
Potentially, there is a way to get rid of this /e/ infix and make the URL
look cleaner.

5) CSS can be refactored to follow the BEM model.
