### The Movie DB API Key is required.

In order for the Popular Movies app to function properly, an API key for https://www.themoviedb.org/ must be included with the build.

To  review obtain a key via the following [instructions](https://www.themoviedb.org/faq/api), and include the unique key for the build by adding the following line to `[USER_HOME]/.gradle/gradle.properties`:

`MyTheMovieDBApiKey="<UNIQUE_API_KEY>"`

The API key can also be included by directly editing the app's `build.gradle` file and replacing `MyTheMovieDBApiKey` by `'<UNIQUE_API_KEY>'`.
